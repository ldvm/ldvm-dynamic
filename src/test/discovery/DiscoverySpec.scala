package discovery

import com.hp.hpl.jena.rdf.model.ModelFactory
import discovery.components.{DummyVisualizer, JenaDataSource}
import discovery.model._
import discovery.model.components.ComponentInstance
import org.scalatest.LoneElement._
import org.scalatest.concurrent.ScalaFutures._

class DiscoverySpec extends LdvmSpec {
  "Discovery" should "discover dummy pipeline" in {
    val dummySource = new JenaDataSource(ModelFactory.createDefaultModel())
    val dummySuccessVisualizer = new DummyVisualizer(Status.Success)

    val input = new DiscoveryInput(
      Seq(dummySource),
      Seq(dummySuccessVisualizer),
      Seq()
    )

    val dsComponent = PipelineComponent("A", dummySource)
    val vComponent = PipelineComponent("A", dummySuccessVisualizer)

    val future = new Discovery().discover(input)

    val pipeline = future.futureValue.loneElement
    assertBindings(pipeline, ExpectedBinding(dummySource, "PORT1", dummySuccessVisualizer))
    assertCorrectComponents(pipeline)
    assertOutput(pipeline, dummySuccessVisualizer, DataSample())
  }

  it should "discover nothing" in {
    val dummySource = new JenaDataSource(ModelFactory.createDefaultModel())
    val dummySuccessVisualizer = new DummyVisualizer(Status.Failure)

    val input = new DiscoveryInput(
      Seq(dummySource),
      Seq(dummySuccessVisualizer),
      Seq()
    )

    val future = new Discovery().discover(input)

    future.futureValue shouldBe empty
  }

  // Helper assert methods ------

  case class ExpectedBinding(startComponent: ComponentInstance, portName: String, endComponent: ComponentInstance)

  def assertBindings(pipeline: Pipeline, expectedBindings: ExpectedBinding*) = {
    val actualBindings = pipeline.bindings.map {
      b => ExpectedBinding(b.startComponent.componentInstance, b.endPort.name, b.endComponent.componentInstance)
    }
    actualBindings should contain theSameElementsAs expectedBindings
    actualBindings should have size expectedBindings.size
  }

  def assertCorrectComponents(pipeline: Pipeline) = {
    val bindingComponents = pipeline.bindings
      .flatMap { b => Seq(b.startComponent, b.endComponent) }
      .toSet
    pipeline.components should contain theSameElementsAs bindingComponents
  }

  def assertOutput(pipeline: Pipeline, expectedLastComponent: ComponentInstance, expectedDataSample: DataSample) = {
    pipeline.lastComponent.componentInstance shouldBe expectedLastComponent
    pipeline.lastOutputDataSample shouldBe expectedDataSample
  }
}