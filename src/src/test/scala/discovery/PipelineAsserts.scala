package discovery

import discovery.model.components.{ComponentInstance, ComponentInstanceWithInputs}
import discovery.model.{DataSample, Pipeline, _}
import org.scalatest.Assertions._

trait PipelineAsserts {
  case class ExpectedPipeline(lastComponent: ComponentInstance, bindings: ExpectedBinding*)

  case class ExpectedBinding(startComponent: ComponentInstance, portName: String, endComponent: ComponentInstance) {

    override def toString: String = s"\nExpectedBiding(${startComponent.getClass.getSimpleName} --$portName-> ${endComponent.getClass.getSimpleName})"
  }

  object ExpectedBinding {
    def apply(startComponent: ComponentInstance, port: Port, endComponent: ComponentInstance) = new ExpectedBinding(startComponent, port.name, endComponent)
  }

  implicit class PipelineAssertsWrapper(actualPipelines: Seq[Pipeline]) {
    def shouldContainPipeline(expectedPipeline: ExpectedPipeline, expectedDataSample: DataSample = EmptyDataSample): Unit = {
      val pipelinesWithExpectedBindings = assertHasExpectedBindings(actualPipelines, expectedPipeline)

      val pipelinesWithExpectedLastComponent = assertHasLastComponent(pipelinesWithExpectedBindings, expectedPipeline)

      val matchingPipeline: Pipeline = assertSingleMatchingPipeline(pipelinesWithExpectedLastComponent, expectedPipeline)

      assertEndsWithEmptyDataSample(matchingPipeline, expectedDataSample)
      assertUniquePipelineComponents(matchingPipeline)
      assertNoUnboundComponents(matchingPipeline)
      assertAllComponentsBoundCompletely(matchingPipeline)
    }

    private def assertHasExpectedBindings(pipelines: Seq[Pipeline], expectedPipeline: ExpectedPipeline): Seq[Pipeline] = {
      val pipelinesWithExpectedBindings = pipelines.filter(hasExpectedBindings(expectedPipeline.bindings))
      if (pipelinesWithExpectedBindings.isEmpty) {
        fail(s"Given pipelines ${formatPipelines(pipelines)}\n\tdo not contain pipeline with expected bindings ${expectedPipeline.bindings}")
      }
      pipelinesWithExpectedBindings
    }

    private def assertHasLastComponent(pipelines: Seq[Pipeline], expectedPipeline: ExpectedPipeline): Seq[Pipeline] = {
      val pipelinesWithExpectedLastComponent = pipelines.filter(_.lastComponent.componentInstance == expectedPipeline.lastComponent)
      if (pipelinesWithExpectedLastComponent.isEmpty) {
        fail(s"None of pipelines ${formatPipelines(pipelines)}\n\t has expected last component ${expectedPipeline.lastComponent}")
      }
      pipelinesWithExpectedLastComponent
    }

    private def assertSingleMatchingPipeline(pipelines: Seq[Pipeline], expectedPipeline: ExpectedPipeline): Pipeline = {
      if (pipelines.size > 1) {
        fail(s"Multiple pipelines matching expected pipeline $expectedPipeline found:\n\t ${formatPipelines(pipelines)}")
      }
      pipelines.head
    }

    private def assertEndsWithEmptyDataSample(pipeline: Pipeline, expectedDataSample: DataSample): Unit = {
      if (expectedDataSample != pipeline.lastOutputDataSample) {
        fail(s"Pipeline ${pipeline.prettyFormat()}\n\t do not end with expected data sample $expectedDataSample")
      }
    }

    private def assertUniquePipelineComponents(pipeline: Pipeline): Unit = {
      val hasUniqueComponents = pipeline.components.map(_.id).toSet.size == pipeline.components.size
      if (!hasUniqueComponents) {
        fail(s"Pipeline ${pipeline.prettyFormat()}\n\t does not have all components unique: ${pipeline.components}")
      }
    }

    private def assertNoUnboundComponents(pipeline: Pipeline): Unit = {
      val boundComponents = pipeline.bindings
        .flatMap { b => Seq(b.startComponent, b.endComponent) }
        .toSet
      if (pipeline.components.toSet != boundComponents) {
        fail(s"Pipeline ${pipeline.prettyFormat()}\n\t contains unbound components")
      }
    }

    private def assertAllComponentsBoundCompletely(pipeline: Pipeline): Unit = {
      pipeline.components.map(_.componentInstance).collect { case componentInstance: ComponentInstanceWithInputs =>
        val boundPorts = pipeline.bindings.filter(_.endComponent.componentInstance == componentInstance).map(_.endPort).toSet
        val unboundPorts = componentInstance.getInputPorts.filterNot(boundPorts.contains)
        if (unboundPorts.nonEmpty) {
          fail(s"Component instance $componentInstance in pipeline ${pipeline.prettyFormat()}\n\t has unbound ports $unboundPorts")
        }
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

  def formatPipelines(pipelines: Seq[Pipeline]): String = {
    pipelines.map(_.prettyFormat("\t")).mkString("List(\n", ",\n", "\n)")
  }
}
