package discovery

import discovery.model.PortCheckResult.Status
import discovery.model._
import discovery.model.components.{ComponentInstance, ComponentInstanceWithInputs, ProcessorInstance}
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

class DiscoveryPortMatcher(pipelineBuilder: PipelineBuilder) {

  def tryMatchPorts(componentInstance: ComponentInstanceWithInputs, givenPipelines: Seq[Pipeline]): Future[Seq[Pipeline]] = {
    val ports = componentInstance.getInputPorts.sortBy(_.priority)
    tryMatchPorts(ports, givenPipelines, portMatches = Map(), lastStates = Seq(None), componentInstance)
  }

  private def tryMatchPorts(remainingPorts: Seq[Port], givenPipelines: Seq[Pipeline], portMatches: Map[Port, Seq[PortMatch]], lastStates: Seq[Option[ComponentState]], componentInstance: ComponentInstanceWithInputs): Future[Seq[Pipeline]] = {
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

  private def tryMatchGivenPipelinesWithPort(port: Port, givenPipelines: Seq[Pipeline], lastStates: Seq[Option[ComponentState]], componentInstance: ComponentInstanceWithInputs): Future[Seq[PortMatch]] = {
    Future.sequence {
      for (
        pipeline <- givenPipelines;
        state <- lastStates
      ) yield {
        val eventualCheckResult = componentInstance.checkPort(port, state, pipeline.lastOutputDataSample)
        eventualCheckResult.collect {
          case c: PortCheckResult if c.status == Status.Success => Some(PortMatch(port, pipeline, c.maybeState))
          case _ => None
        }
      }
    }.map(_.flatten)
  }

  private def buildPipelines(componentInstance: ComponentInstance, portMatches: Map[Port, Seq[PortMatch]]): Future[Seq[Pipeline]] = {
    val allCombinations = combine(portMatches.values)
    Future.sequence(
      allCombinations.map { portSolutions => pipelineBuilder.buildPipeline(componentInstance, portSolutions) }
    )
  }

  private def combine[A](xs: Traversable[Traversable[A]]): Seq[Seq[A]] = {
    xs.foldLeft(Seq(Seq.empty[A])) {
      (x, y) => for (a <- x.view; b <- y) yield a :+ b
    }.toList
  }
}
