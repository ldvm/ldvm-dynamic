package model.discovery

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

class Discovery {

  val MAX_ITERATIONS = 10

  def discover(input: DiscoveryInput): Future[Seq[Pipeline]] = {
    createInitialPipelines(input.dataSources).flatMap { initialPipelines =>
      val possibleComponents = input.processors ++ input.visualizers
      iterate(0, IterationData(initialPipelines, Seq(), possibleComponents))
    }
  }

  private def iterate(iterationNumber: Int, iterationData: IterationData): Future[Seq[Pipeline]] = {
    iterationBody(iterationData).flatMap { nextIterationData =>
      val discoveredNewPipeline = nextIterationData.givenPipelines.size > iterationData.givenPipelines.size

      if (!discoveredNewPipeline || iterationNumber == MAX_ITERATIONS) {
        Future.successful(nextIterationData.completedPipelines)
      } else {
        iterate(iterationNumber + 1, nextIterationData)
      }
    }
  }

  private def iterationBody(iterationData: IterationData): Future[IterationData] = {
    Future.sequence {
      iterationData.possibleComponents.map { c =>
        val ports = c.getInputPorts.sortBy(_.priority)
        tryMatchPorts(ports, iterationData.givenPipelines, Map(), Seq(None), c)
      }
    }.map { pipelines =>
      val (completePipelines, incompletePipelines) = pipelines.flatten.partition(_.isComplete)
      IterationData(iterationData.givenPipelines ++ incompletePipelines, iterationData.completedPipelines ++ completePipelines, iterationData.possibleComponents)
    }
  }

  private def tryMatchPorts(remainingPorts: Seq[Port], givenPipelines: Seq[Pipeline], portMatches: Map[Port, Seq[PortMatch]], lastStates: Seq[Option[State]], componentInstance: ComponentInstanceWithInputs): Future[Seq[Pipeline]] = {
    remainingPorts match {
      case Nil => buildPipelines(componentInstance, portMatches) //TODO check all ports have > 0 matches
      case head :: tail => {
        tryMatchPipelines(head, givenPipelines, lastStates, componentInstance).flatMap { matches =>
          tryMatchPorts(tail, givenPipelines, portMatches + (head -> matches), matches.map(_.maybeState), componentInstance)
        }
      }
    }
  }

  private def tryMatchPipelines(port: Port, givenPipelines: Seq[Pipeline], lastStates: Seq[Option[State]], componentInstance: ComponentInstanceWithInputs): Future[Seq[PortMatch]] = {
    Future.sequence {
      givenPipelines.flatMap { pipeline =>
        lastStates.map { state =>
          val eventualCheckResult = componentInstance.checkPort(port, state, pipeline.lastOutputDataSample)
          eventualCheckResult.collect {
            case c: CheckResult if c.status == Status.Success => PortMatch(port, pipeline, c.maybeState)
          }
        }
      }
    }
  }

  private def buildPipelines(componentInstance: ComponentInstance, portMatches: Map[Port, Seq[PortMatch]]): Future[Seq[Pipeline]] = {
    val allCombinations = combine(portMatches.values)
    Future.sequence(
      allCombinations.map { portSolutions => buildPipeline(componentInstance, portSolutions) }
    )
  }

  private def buildPipeline(componentInstance: ComponentInstance, portMatches: Seq[PortMatch]): Future[Pipeline] = {
    val pipelineComponent = PipelineComponent("A", componentInstance)
    val components = portMatches.flatMap(_.startPipeline.components) :+ pipelineComponent

    val newBindings = portMatches.map { portMatch =>
      Binding(portMatch.startPipeline.lastComponent, portMatch.port, pipelineComponent)
    }
    val bindings = portMatches.flatMap(_.startPipeline.bindings) ++ newBindings


    val dataSamples = portMatches.map { pm => pm.port -> pm.startPipeline.lastOutputDataSample }.toMap
    val eventuallyDataSample = componentInstance match {
      case c: ProcessorInstance => c.getOutputDataSample(portMatches.last.maybeState, dataSamples)
      case _ => Future.successful(DataSample())
    }
    eventuallyDataSample.map { dataSample => // TODO: Future data sample to pipeline
      Pipeline(components, bindings, pipelineComponent, dataSample)
    }
  }

  private def createInitialPipelines(dataSources: Seq[DataSourceInstance]): Future[Seq[Pipeline]] = {
    Future.sequence(dataSources.map { dataSource =>
      val pipelineComponent = PipelineComponent("A", dataSource)
      dataSource.getOutputDataSample(None, Map()).map { outputDataSample =>
        Pipeline(Seq(pipelineComponent), Seq(), pipelineComponent, outputDataSample)
      }
    })
  }

  private def combine[A](xs: Traversable[Traversable[A]]): Seq[Seq[A]] = {
    xs.foldLeft(Seq(Seq.empty[A])) {
      (x, y) => for (a <- x.view; b <- y) yield a :+ b
    }.toList
  }

}
