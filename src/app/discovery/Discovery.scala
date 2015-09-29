package discovery

import discovery.model._
import discovery.model.components.DataSourceInstance
import discovery.model.internal.IterationData

import scala.concurrent.{ExecutionContext, Future}

class Discovery(portMatcher: DiscoveryPortMatcher, pipelineBuilder: PipelineBuilder)(implicit executor: ExecutionContext) {

  val MAX_ITERATIONS = 10

  def discover(input: DiscoveryInput): Future[Seq[CompletePipeline]] = {
    createInitialPipelines(input.dataSources).flatMap { initialPipelines =>
      val possibleComponents = input.processors ++ input.visualizers
      iterate(0, IterationData(initialPipelines, completedPipelines = Seq(), possibleComponents))
    }
  }

  private def iterate(iterationNumber: Int, iterationData: IterationData): Future[Seq[CompletePipeline]] = {
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
    val eventualPipelines = Future.sequence {
      iterationData.possibleComponents.map { c => portMatcher.tryMatchPorts(c, iterationData.givenPipelines) }
    }
    eventualPipelines.map { unflatennedPipelines =>
      val pipelines = unflatennedPipelines.flatten
      val completePipelines = pipelines collect { case p: CompletePipeline => p }
      val incompletePipelines = pipelines collect { case p: PartialPipeline => p }
      IterationData(iterationData.givenPipelines ++ incompletePipelines, iterationData.completedPipelines ++ completePipelines, iterationData.possibleComponents)
    }
  }

  private def createInitialPipelines(dataSources: Seq[DataSourceInstance]): Future[Seq[PartialPipeline]] = {
    Future.sequence(dataSources.map(pipelineBuilder.buildInitialPipeline))
  }

}
