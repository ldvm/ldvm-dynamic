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
      iterate(IterationData(initialPipelines, completedPipelines = Seq(), possibleComponents, 1))
    }
  }

  private def iterate(iterationData: IterationData): Future[Seq[CompletePipeline]] = {
    iterationBody(iterationData).flatMap { nextIterationData =>
      val discoveredNewPipeline = nextIterationData.givenPipelines.size > iterationData.givenPipelines.size

      if (!discoveredNewPipeline || iterationData.iterationNumber == MAX_ITERATIONS) {
        Future.successful(nextIterationData.completedPipelines)
      } else {
        iterate(nextIterationData)
      }
    }
  }

  private def iterationBody(iterationData: IterationData): Future[IterationData] = {
    val eventualPipelines = Future.sequence {
      iterationData.possibleComponents.map { c => portMatcher.tryMatchPorts(c, iterationData.givenPipelines, iterationData.iterationNumber) }
    }
    eventualPipelines.map { rawPipelines =>
      val newPipelines = rawPipelines.view.flatten
        .filterNot(containsComponentBoundToItself)
        .filter(containsBindingToIteration(iterationData.iterationNumber - 1))
      val completePipelines = newPipelines collect { case p: CompletePipeline => p }
      val incompletePipelines = newPipelines collect { case p: PartialPipeline => p }
      IterationData(iterationData.givenPipelines ++ incompletePipelines, iterationData.completedPipelines ++ completePipelines, iterationData.possibleComponents, iterationData.iterationNumber + 1)
    }
  }

  private def createInitialPipelines(dataSources: Seq[DataSourceInstance]): Future[Seq[PartialPipeline]] = {
    Future.sequence(dataSources.map(pipelineBuilder.buildInitialPipeline))
  }

  private def containsComponentBoundToItself(pipeline: Pipeline): Boolean = pipeline.bindings.exists(
    binding => binding.startComponent.componentInstance == binding.endComponent.componentInstance
  )

  private def containsBindingToIteration(iterationNumber: Int)(pipeline: Pipeline): Boolean = pipeline.bindings.exists(
    binding => binding.startComponent.discoveryIteration == iterationNumber
  )
}
