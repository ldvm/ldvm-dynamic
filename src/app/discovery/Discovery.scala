package discovery

import discovery.model._
import discovery.model.components.DataSourceInstance
import discovery.model.internal.IterationData
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

class Discovery(portMatcher: DiscoveryPortMatcher, pipelineBuilder: PipelineBuilder) {

  val MAX_ITERATIONS = 10

  def discover(input: DiscoveryInput): Future[Seq[Pipeline]] = {
    createInitialPipelines(input.dataSources).flatMap { initialPipelines =>
      val possibleComponents = input.processors ++ input.visualizers
      iterate(0, IterationData(initialPipelines, completedPipelines = Seq(), possibleComponents))
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
      iterationData.possibleComponents.map { c => portMatcher.tryMatchPorts(c, iterationData.givenPipelines) }
    }.map { pipelines =>
      val (completePipelines, incompletePipelines) = pipelines.flatten.partition(_.isComplete)
      IterationData(iterationData.givenPipelines ++ incompletePipelines, iterationData.completedPipelines ++ completePipelines, iterationData.possibleComponents)
    }
  }

  private def createInitialPipelines(dataSources: Seq[DataSourceInstance]): Future[Seq[Pipeline]] = {
    Future.sequence(dataSources.map(pipelineBuilder.buildInitialPipeline))
  }

}
