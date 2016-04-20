package discovery

import com.typesafe.scalalogging.StrictLogging
import discovery.model._
import discovery.model.components.DataSourceInstance
import discovery.model.internal.IterationData

import scala.concurrent.{ExecutionContext, Future}

class Discovery(portMatcher: DiscoveryPortMatcher, pipelineBuilder: PipelineBuilder)(implicit executor: ExecutionContext) extends StrictLogging {
  val MAX_ITERATIONS = 10

  def discover(input: DiscoveryInput): Future[Seq[Pipeline]] = {
    createInitialPipelines(input.dataSources).flatMap { initialPipelines =>
      val possibleComponents = input.processors ++ input.visualizers
      iterate(IterationData(initialPipelines, completedPipelines = Seq(), possibleComponents, 1))
    }
  }

  private def iterate(iterationData: IterationData): Future[Seq[Pipeline]] = {

    logger.info(s"Starting iteration ${iterationData.iterationNumber}.")

    iterationBody(iterationData).flatMap { nextIterationData =>
      val discoveredNewPipeline = nextIterationData.givenPipelines.size > iterationData.givenPipelines.size
      val stop = !discoveredNewPipeline || iterationData.iterationNumber == MAX_ITERATIONS

      stop match {
        case true => Future.successful(nextIterationData.completedPipelines)
        case false => iterate(nextIterationData)
      }
    }
  }

  private def iterationBody(iterationData: IterationData): Future[IterationData] = {
    val eventualPipelines = Future.sequence {
      iterationData.possibleComponents.map { c => portMatcher.tryMatchPorts(c, iterationData.givenPipelines, iterationData.iterationNumber) }
    }

    eventualPipelines.map { rawPipelines =>

      val newPipelines = rawPipelines.view.flatten
      val fresh = newPipelines.filter(containsBindingToIteration(iterationData.iterationNumber - 1))

      val (completePipelines, partialPipelines) = fresh.partition(_.isComplete)

      logger.info(s"Discovered ${newPipelines.size} new RAW pipeline(s), ${fresh.size} new partial pipeline(s) in iteration ${iterationData.iterationNumber}")
      logger.info(s"Discovered ${completePipelines.size} new complete pipeline(s) and ${partialPipelines.size} new partial pipeline(s) in iteration ${iterationData.iterationNumber}")

      val nextIterationGivenPipelines = (iterationData.givenPipelines ++ partialPipelines).distinct
      val nextIterationCompletePipelines = iterationData.completedPipelines ++ completePipelines

      completePipelines.foreach(p => logger.info(p.prettyFormat()))

      IterationData(
        nextIterationGivenPipelines,
        nextIterationCompletePipelines,
        iterationData.possibleComponents,
        iterationData.iterationNumber + 1
      )
    }
  }

  private def createInitialPipelines(dataSources: Seq[DataSourceInstance]): Future[Seq[Pipeline]] = {
    Future.sequence(dataSources.map(pipelineBuilder.buildInitialPipeline))
  }

  private def containsBindingToIteration(iterationNumber: Int)(pipeline: Pipeline): Boolean = pipeline.bindings.exists(
    binding => binding.startComponent.discoveryIteration == iterationNumber
  )
}
