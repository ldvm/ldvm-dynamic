package discovery

import discovery.model.components.ComponentInstance
import discovery.model.{DataSample, Pipeline}
import org.scalatest.Assertions._

trait PipelineAsserts {
  private val emptyDataSample = DataSample()

  case class ExpectedPipeline(lastComponent: ComponentInstance, bindings: ExpectedBinding*)

  case class ExpectedBinding(startComponent: ComponentInstance, portName: String, endComponent: ComponentInstance)

  implicit class PipelineAssertsWrapper(actualPipelines: Seq[Pipeline]) {
    def shouldContainPipeline(expectedPipeline: ExpectedPipeline, expectedDataSample: DataSample = emptyDataSample): Unit = {
      val pipelinesWithExpectedBindings = assertHasExpectedBindings(actualPipelines, expectedPipeline)

      val pipelinesWithExpectedLastComponent = assertHasLastComponent(pipelinesWithExpectedBindings, expectedPipeline)

      val matchingPipeline: Pipeline = assertSingleMatchingPipeline(pipelinesWithExpectedLastComponent, expectedPipeline)

      assertEndsWithEmptyDataSample(matchingPipeline, expectedDataSample)
      assertUniquePipelineComponents(matchingPipeline)
      assertNoUnboundComponents(matchingPipeline)
    }

    private def assertHasExpectedBindings(pipelines: Seq[Pipeline], expectedPipeline: ExpectedPipeline): Seq[Pipeline] = {
      val pipelinesWithExpectedBindings = pipelines.filter(hasExpectedBindings(expectedPipeline.bindings))
      if (pipelinesWithExpectedBindings.isEmpty) {
        fail(s"Given pipelines $pipelines\n\tdid not contain pipeline with expected bindings ${expectedPipeline.bindings}")
      }
      pipelinesWithExpectedBindings
    }

    private def assertHasLastComponent(pipelines: Seq[Pipeline], expectedPipeline: ExpectedPipeline): Seq[Pipeline] = {
      val pipelinesWithExpectedLastComponent = pipelines.filter(_.lastComponent.componentInstance == expectedPipeline.lastComponent)
      if (pipelinesWithExpectedLastComponent.isEmpty) {
        fail(s"None of pipelines $pipelines\n\t has expected last component ${expectedPipeline.lastComponent}")
      }
      pipelinesWithExpectedLastComponent
    }

    private def assertSingleMatchingPipeline(pipelines: Seq[Pipeline], expectedPipeline: ExpectedPipeline): Pipeline = {
      if (pipelines.size > 1) {
        fail(s"Multiple pipelines matching expected pipeline $expectedPipeline found:\n\t $pipelines")
      }
      pipelines.head
    }

    private def assertEndsWithEmptyDataSample(pipeline: Pipeline, expectedDataSample: DataSample): Unit = {
      if (expectedDataSample != pipeline.lastOutputDataSample) {
        fail(s"Pipeline $pipeline\n\t did not end with expected data sample $expectedDataSample")
      }
    }

    private def assertUniquePipelineComponents(pipeline: Pipeline): Unit = {
      val hasUniqueComponents = pipeline.components.map(_.id).toSet.size == pipeline.components.size
      if (!hasUniqueComponents) {
        fail(s"Pipeline $pipeline\n\t does not have all components unique: ${pipeline.components}")
      }
    }

    private def assertNoUnboundComponents(pipeline: Pipeline): Unit = {
      val boundComponents = pipeline.bindings
        .flatMap { b => Seq(b.startComponent, b.endComponent) }
        .toSet
      if (pipeline.components.toSet != boundComponents) {
        fail(s"Pipeline $pipeline\n\t contains unbound components")
      }
    }

    private def hasExpectedBindings(expectedBindings: Seq[ExpectedBinding])(pipeline: Pipeline): Boolean = {
      val actualBindings = pipeline.bindings.map {
        b => ExpectedBinding(b.startComponent.componentInstance, b.endPort.name, b.endComponent.componentInstance)
      }
      haveSameElements(actualBindings, expectedBindings)
    }

    private def haveSameElements[T](seq1: Seq[T], seq2: Seq[T]): Boolean = {
      seq1.groupBy(identity) == seq2.groupBy(identity)
    }
  }

}
