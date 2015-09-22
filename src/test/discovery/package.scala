import discovery.model.components.ComponentInstance
import discovery.model.{DataSample, Pipeline, PipelineComponent}

package object discovery extends LdvmSpec {
  def assertBindings(pipeline: Pipeline, expectedBindings: ExpectedBinding*) = {
    val actualBindings = pipeline.bindings.map {
      b => ExpectedBinding(b.startComponent.componentInstance, b.endPort.name, b.endComponent.componentInstance)
    }
    actualBindings should contain theSameElementsAs expectedBindings
    actualBindings should have size expectedBindings.size
  }

  def assertCorrectComponents(pipeline: Pipeline) = {
    assertContainsOnlyBindedComponents(pipeline)
    assertUniqueComponentNames(pipeline.components)
  }

  private def assertContainsOnlyBindedComponents(pipeline: Pipeline): Unit = {
    val bindingComponents = pipeline.bindings
      .flatMap { b => Seq(b.startComponent, b.endComponent) }
      .toSet
    pipeline.components should contain theSameElementsAs bindingComponents
  }

  private def assertUniqueComponentNames(components: Seq[PipelineComponent]): Unit = {
    val componentNames: Set[String] = components.map(_.id).toSet
    componentNames should have size components.size
  }

  def assertOutput(pipeline: Pipeline, expectedLastComponent: ComponentInstance, expectedDataSample: DataSample) = {
    pipeline.lastComponent.componentInstance shouldBe expectedLastComponent
    pipeline.lastOutputDataSample shouldBe expectedDataSample
  }
}
