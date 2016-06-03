package discovery

import com.typesafe.scalalogging.LazyLogging
import discovery.model.PortCheckResult.Status
import discovery.model._
import discovery.model.components.{ComponentInstance, ComponentInstanceWithInputs, DataSourceInstance}

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
        val linksets = portMatches.flatMap {
          case pm => pm._2.flatMap {
            case m => m.startPipeline.components.map(_.componentInstance).filter {
              case c : DataSourceInstance => c.isLinkset
              case _ => false
            }
          }
        }.toSeq.distinct
        val filteredPipelines = givenPipelines.filterNot(_.components.exists(c => linksets.contains(c.componentInstance)))
        println(linksets.size, filteredPipelines.size, givenPipelines.size)
        tryMatchGivenPipelinesWithPort(headPort, filteredPipelines, lastStates, componentInstance).flatMap { matches =>
          tryMatchPorts(tail, filteredPipelines, portMatches + (headPort -> matches), matches.map(_.maybeState), iteration, componentInstance)
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
          case c: PortCheckResult if c.status == Status.Success =>
            //logger.info("Matched port {} of {} with {}.", port, componentInstance, pipeline.lastComponent)
            Some(PortMatch(port, pipeline, c.maybeState))
          case c: PortCheckResult if c.status == Status.Error =>
            logger.error("Failed matching port {} of {} with error", port, componentInstance)
            None
          case _ =>
            //logger.info("Port {} of {} not matched with {}.", port, componentInstance, pipeline.lastComponent)
            None
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
