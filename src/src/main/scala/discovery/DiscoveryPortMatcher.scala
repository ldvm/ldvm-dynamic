package discovery

import com.typesafe.scalalogging.LazyLogging
import discovery.model.PortCheckResult.Status
import discovery.model._
import discovery.model.components.{ComponentInstance, ComponentInstanceWithInputs}

import scala.concurrent.{ExecutionContext, Future}

class DiscoveryPortMatcher(pipelineBuilder: PipelineBuilder)(implicit executor: ExecutionContext) extends LazyLogging {

  def tryMatchPorts(componentInstance: ComponentInstanceWithInputs, givenPipelines: Seq[Pipeline], iteration: Int): Future[Seq[Pipeline]] = {
    val ports = componentInstance.getInputPorts.sortBy(_.priority)
    tryMatchPorts(ports, givenPipelines, portMatches = Map(), lastStates = Seq(None), iteration, componentInstance)
  }

  private def tryMatchPorts(
    remainingPorts: Seq[Port],
    givenPipelines: Seq[Pipeline],
    portMatches: Map[Port, Seq[PortMatch]],
    lastStates: Seq[Option[ComponentState]],
    iteration: Int,
    componentInstance: ComponentInstanceWithInputs
    ): Future[Seq[Pipeline]] = {
    remainingPorts match {
      case Nil =>
        portMatches.values.forall(_.nonEmpty) match {
          case true => buildPipelines(componentInstance, portMatches, iteration)
          case false => Future.successful(Seq())
        }
      case headPort :: tail =>
        tryMatchGivenPipelinesWithPort(headPort, givenPipelines, lastStates, componentInstance).flatMap { matches =>
          tryMatchPorts(tail, givenPipelines, portMatches + (headPort -> matches), matches.map(_.maybeState), iteration, componentInstance)
        }
    }
  }

  private def tryMatchGivenPipelinesWithPort(
    port: Port,
    givenPipelines: Seq[Pipeline],
    lastStates: Seq[Option[ComponentState]],
    componentInstance: ComponentInstanceWithInputs): Future[Seq[PortMatch]] = {
    val eventualMaybeMatches = Future.sequence {
      for {
        pipeline <- givenPipelines if !pipeline.endsWith(componentInstance)
        state <- lastStates
      } yield {
        val eventualCheckResult = componentInstance.checkPort(port, state, pipeline.lastOutputDataSample)
        eventualCheckResult.map {
          case c: PortCheckResult if c.status == Status.Success => Some(PortMatch(port, pipeline, c.maybeState))
          case c: PortCheckResult if c.status == Status.Error =>
            logger.error("Failed matching port {} of {} with error", port, componentInstance)
            None
          case _ => None
        }
      }
    }
    eventualMaybeMatches.map(_.flatten)
  }

  private def buildPipelines(componentInstance: ComponentInstance, portMatches: Map[Port, Seq[PortMatch]], iteration: Int): Future[Seq[Pipeline]] = {
    val allCombinations = combine(portMatches.values)
    logger.info(s"All portMatches count: ${portMatches.values.flatten.size}; combinations: ${allCombinations.size}.")
    Future.sequence(
      allCombinations.map { portSolutions => pipelineBuilder.buildPipeline(componentInstance, portSolutions, iteration) }
    )
  }

  private def combine[A](xs: Traversable[Traversable[A]]): Seq[Seq[A]] = {
    xs.foldLeft(Seq(Seq.empty[A])) {
      (x, y) => for (a <- x.view; b <- y) yield a :+ b
    }
  }
}
