import discovery.model.components.ComponentInstance
import discovery.model.{DataSample, Pipeline}

package object discovery extends LdvmSpec {
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

  def createDiscovery(): Discovery = {
    new Discovery(new DiscoveryPortMatcher(new PipelineBuilder()))
  }
}
