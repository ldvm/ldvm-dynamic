package discovery

import discovery.model.PortCheckResult.Status
import discovery.model._
import discovery.model.components.{ComponentInstance, ComponentInstanceWithInputs, ProcessorInstance, VisualizerInstance}

import scala.concurrent.{ExecutionContext, Future}

class DiscoveryPortMatcher(pipelineBuilder: PipelineBuilder)(implicit executor: ExecutionContext) {

  def tryMatchPorts(componentInstance: ComponentInstanceWithInputs, givenPipelines: Seq[PartialPipeline]): Future[Seq[Pipeline]] = {
    val ports = componentInstance.getInputPorts.sortBy(_.priority)
    tryMatchPorts(ports, givenPipelines, portMatches = Map(), lastStates = Seq(None), componentInstance)
  }

  private def tryMatchPorts(remainingPorts: Seq[Port], givenPipelines: Seq[PartialPipeline], portMatches: Map[Port, Seq[PortMatch]], lastStates: Seq[Option[ComponentState]], componentInstance: ComponentInstanceWithInputs): Future[Seq[Pipeline]] = {
    remainingPorts match {
      case Nil =>
        if (portMatches.values.forall(_.nonEmpty)) {
          buildPipelines(componentInstance, portMatches)
        } else {
          Future.successful(Seq())
        }
      case headPort :: tail =>
        tryMatchGivenPipelinesWithPort(headPort, givenPipelines, lastStates, componentInstance).flatMap { matches =>
          tryMatchPorts(tail, givenPipelines, portMatches + (headPort -> matches), matches.map(_.maybeState), componentInstance)
        }
    }
  }

  private def tryMatchGivenPipelinesWithPort(port: Port, givenPipelines: Seq[PartialPipeline], lastStates: Seq[Option[ComponentState]], componentInstance: ComponentInstanceWithInputs): Future[Seq[PortMatch]] = {
    val eventualMaybeMatches = Future.sequence {
      for (
        pipeline <- givenPipelines;
        state <- lastStates
      ) yield {
        val eventualCheckResult = componentInstance.checkPort(port, state, pipeline.lastOutputDataSample)
        eventualCheckResult.map {
          case c: PortCheckResult if c.status == Status.Success => Some(PortMatch(port, pipeline, c.maybeState))
          case _ => None
        }
      }
    }
    eventualMaybeMatches.map(_.flatten)
  }

  private def buildPipelines(componentInstance: ComponentInstance, portMatches: Map[Port, Seq[PortMatch]]): Future[Seq[Pipeline]] = {
    val allCombinations = combine(portMatches.values)
    Future.sequence(
      allCombinations.map { portSolutions =>
        componentInstance match {
          case visualizer: VisualizerInstance => Future.successful(pipelineBuilder.buildCompletePipeline(visualizer, portSolutions))
          case processor: ProcessorInstance => pipelineBuilder.buildPartialPipeline(processor, portSolutions)
        }
      }
    )
  }

  private def combine[A](xs: Traversable[Traversable[A]]): Seq[Seq[A]] = {
    xs.foldLeft(Seq(Seq.empty[A])) {
      (x, y) => for (a <- x.view; b <- y) yield a :+ b
    }
  }
}
