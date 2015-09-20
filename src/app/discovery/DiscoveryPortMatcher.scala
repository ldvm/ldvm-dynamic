package discovery

import discovery.model.PortCheckResult.Status
import discovery.model._
import discovery.model.components.{ComponentInstance, ComponentInstanceWithInputs, ProcessorInstance}
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

class DiscoveryPortMatcher {

  def tryMatchPorts(componentInstance: ComponentInstanceWithInputs, givenPipelines: Seq[Pipeline]): Future[Seq[Pipeline]] = {
    val ports = componentInstance.getInputPorts.sortBy(_.priority)
    tryMatchPorts(ports, givenPipelines, Map(), Seq(None), componentInstance)
  }

  private def tryMatchPorts(remainingPorts: Seq[Port], givenPipelines: Seq[Pipeline], portMatches: Map[Port, Seq[PortMatch]], lastStates: Seq[Option[ComponentState]], componentInstance: ComponentInstanceWithInputs): Future[Seq[Pipeline]] = {
    remainingPorts match {
      case Nil => buildPipelines(componentInstance, portMatches) //TODO check all ports have > 0 matches
      case head :: tail =>
        tryMatchPipelines(head, givenPipelines, lastStates, componentInstance).flatMap { matches =>
          tryMatchPorts(tail, givenPipelines, portMatches + (head -> matches), matches.map(_.maybeState), componentInstance)
        }
    }
  }

  private def tryMatchPipelines(port: Port, givenPipelines: Seq[Pipeline], lastStates: Seq[Option[ComponentState]], componentInstance: ComponentInstanceWithInputs): Future[Seq[PortMatch]] = {
    Future.sequence {
      givenPipelines.flatMap { pipeline =>
        lastStates.map { state =>
          val eventualCheckResult = componentInstance.checkPort(port, state, pipeline.lastOutputDataSample)
          eventualCheckResult.collect {
            case c: PortCheckResult if c.status == Status.Success => Some(PortMatch(port, pipeline, c.maybeState))
            case _ => None
          }
        }
      }
    }.map {_.flatten}
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
      PortBinding(portMatch.startPipeline.lastComponent, portMatch.port, pipelineComponent)
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

  private def combine[A](xs: Traversable[Traversable[A]]): Seq[Seq[A]] = {
    xs.foldLeft(Seq(Seq.empty[A])) {
      (x, y) => for (a <- x.view; b <- y) yield a :+ b
    }.toList
  }
}
