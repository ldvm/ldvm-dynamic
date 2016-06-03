package discovery

import com.typesafe.scalalogging.StrictLogging
import discovery.components.analyzer.LinksetBasedUnion
import discovery.model._
import discovery.model.components.DataSourceInstance
import discovery.model.internal.IterationData

import scala.concurrent.{ExecutionContext, Future}

class Discovery(portMatcher: DiscoveryPortMatcher, pipelineBuilder: PipelineBuilder)(implicit executor: ExecutionContext) extends StrictLogging {
  val MAX_ITERATIONS = 10

  def discover(input: DiscoveryInput): Future[Seq[Pipeline]] = {
    createInitialPipelines(input.dataSources).flatMap { initialPipelines =>
      val data = IterationData(
        givenPipelines = initialPipelines,
        completedPipelines = Seq(),
        availableComponents = input,
        iterationNumber = 1
      )
      iterate(data)
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

  private def endsWithLargeDataset(pipeline: Pipeline): Boolean = {
    pipeline.lastComponent.componentInstance match {
      case ci: DataSourceInstance => ci.isLarge
      case _ => false
    }
  }

  private def iterationBody(iterationData: IterationData): Future[IterationData] = {

    val (extractorCandidates, otherPipelines) = iterationData.givenPipelines.partition(endsWithLargeDataset)
    val extractors = iterationData.availableComponents.extractors
    val otherComponents = iterationData.availableComponents.processors ++ iterationData.availableComponents.visualizers

    val eventualPipelines = Future.sequence(Seq(
      (extractorCandidates, extractors),
      (otherPipelines, otherComponents)
    ).flatMap {
      case (pipelines, components) => components.map {
        case c if c.isInstanceOf[LinksetBasedUnion] => portMatcher.tryMatchPorts(c, pipelines.filterNot(_.components.exists(_.componentInstance == c)), iterationData.iterationNumber)
        case c => portMatcher.tryMatchPorts(c, pipelines, iterationData.iterationNumber)
      }
    })

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
        iterationData.availableComponents,
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
