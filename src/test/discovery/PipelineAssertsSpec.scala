package discovery

import com.hp.hpl.jena.rdf.model.ModelFactory
import discovery.components.{DummyTwoPortVisualizer, DummyTwoPortAnalyzer, DummyVisualizer, JenaDataSource}
import discovery.model.PortCheckResult.Status
import discovery.model._
import org.scalatest.exceptions.TestFailedException

class PipelineAssertsSpec extends LdvmSpec {
  val expectedDataSource = new JenaDataSource(ModelFactory.createDefaultModel())
  val expectedVisualizer = new DummyVisualizer(Status.Success)
  val expectedPipeline = ExpectedPipeline(
    expectedVisualizer,
    ExpectedBinding(expectedDataSource, expectedVisualizer.port.name, expectedVisualizer)
  )
  val sourceComponent = PipelineComponent("A", expectedDataSource)
  val visualizerComponent = PipelineComponent("B", expectedVisualizer)

  val validPipeline = Pipeline(
    Seq(sourceComponent, visualizerComponent),
    Seq(PortBinding(sourceComponent, expectedVisualizer.port, visualizerComponent)),
    visualizerComponent,
    DataSample()
  )

  "assertContainsPipeline" should "pass with expected pipeline" in {
    Seq(validPipeline) shouldContainPipeline expectedPipeline
  }

  it should "fail with unexpected bindings" in {
    val pipeline = validPipeline.copy(bindings = Seq(PortBinding(visualizerComponent, expectedVisualizer.port, sourceComponent)))
    failsWithMessage(Seq(pipeline), "did not contain pipeline with expected bindings")
  }

  it should "fail with unexpected last component" in {
    val pipeline = validPipeline.copy(lastComponent = sourceComponent)
    failsWithMessage(Seq(pipeline), "has expected last component")
  }

  it should "fail with two matching pipelines" in {
    failsWithMessage(Seq(validPipeline, validPipeline), "Multiple pipelines matching expected pipeline")
  }

  ignore should "fail with nonempty data sample" in {
    val pipeline = validPipeline.copy(lastOutputDataSample = DataSample())
    failsWithMessage(Seq(pipeline), "did not end with empty data sample")
  }

  it should "fail with non-unique pipeline component names" in {
    val pipeline = validPipeline.copy(components = Seq(PipelineComponent("A", expectedDataSource), PipelineComponent("A", expectedVisualizer)))
    failsWithMessage(Seq(pipeline), "does not have all components unique")
  }

  it should "fail with unbound component" in {
    val pipeline = validPipeline.copy(components = Seq(sourceComponent, visualizerComponent, PipelineComponent("C", expectedDataSource)))
    failsWithMessage(Seq(pipeline), "contains unbound components")
  }

  it should "fail with component with an unbound port" in {
    val twoPortVisualizer = new DummyTwoPortVisualizer()
    val twoPortVisualizerComponent = PipelineComponent("TPV", twoPortVisualizer)
    val pipeline = Pipeline(
      Seq(sourceComponent, twoPortVisualizerComponent),
      Seq(PortBinding(sourceComponent, twoPortVisualizer.port1, twoPortVisualizerComponent)),
      twoPortVisualizerComponent,
      DataSample()
    )
    val exception = intercept[TestFailedException] {
      Seq(pipeline) shouldContainPipeline ExpectedPipeline(
        twoPortVisualizer,
        ExpectedBinding(expectedDataSource, twoPortVisualizer.port1.name, twoPortVisualizer)
      )
    }
    exception.getMessage() should include("has unbound ports List(Port(PORT2,2))")
  }

  def failsWithMessage(pipelines: Seq[Pipeline], message: String): Unit = {
    val exception = intercept[TestFailedException] {
      pipelines shouldContainPipeline expectedPipeline
    }
    exception.getMessage() should include(message)
  }
}
