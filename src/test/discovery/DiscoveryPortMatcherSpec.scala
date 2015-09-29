package discovery

import com.hp.hpl.jena.rdf.model.ModelFactory
import discovery.components.{DummyTwoPortAnalyzer, DummyVisualizer, JenaDataSource}
import discovery.model.PortCheckResult.Status
import discovery.model._
import org.scalatest.concurrent.ScalaFutures._

class DiscoveryPortMatcherSpec extends LdvmSpec with DiscoveryCreator {

  "DiscoveryPortMatcher" should "match simple component with compatible partial pipeline" in {
    val portMatcher = createPortMatcher()
    val sourceComponent = new JenaDataSource(ModelFactory.createDefaultModel())
    val visualizerComponent = new DummyVisualizer(Status.Success)

    val matchedPipelines: Seq[Pipeline] = portMatcher.tryMatchPorts(visualizerComponent, Seq(buildInitialPipeline(sourceComponent)), 1).futureValue

    matchedPipelines shouldContainPipeline ExpectedPipeline(
      visualizerComponent,
      ExpectedBinding(sourceComponent, visualizerComponent.port.name, visualizerComponent)
    )
    matchedPipelines should have size 1
  }

  it should "find no match for incompatible partial pipelines" in {
    val portMatcher = createPortMatcher()
    val sourceComponent = new JenaDataSource(ModelFactory.createDefaultModel())
    val visualizerComponent = new DummyVisualizer(Status.Failure)

    val matchedPipelines: Seq[Pipeline] = portMatcher.tryMatchPorts(visualizerComponent, Seq(buildInitialPipeline(sourceComponent)), 1).futureValue

    matchedPipelines shouldBe empty
  }

  it should "discover two possible pipelines" in {
    val portMatcher = createPortMatcher()
    val sourceComponent1 = new JenaDataSource(ModelFactory.createDefaultModel())
    val sourceComponent2 = new JenaDataSource(ModelFactory.createDefaultModel())
    val visualizerComponent = new DummyVisualizer(Status.Success)

    val matchedPipelines: Seq[Pipeline] = portMatcher.tryMatchPorts(visualizerComponent, Seq(buildInitialPipeline(sourceComponent1), buildInitialPipeline(sourceComponent2)), 1).futureValue

    matchedPipelines shouldContainPipeline ExpectedPipeline(visualizerComponent, ExpectedBinding(sourceComponent1, visualizerComponent.port.name, visualizerComponent))
    matchedPipelines shouldContainPipeline ExpectedPipeline(visualizerComponent, ExpectedBinding(sourceComponent2, visualizerComponent.port.name, visualizerComponent))
    matchedPipelines should have size 2
  }

  it should "match two port components with compatible partial pipelines" in {
    val portMatcher = createPortMatcher()
    val sourceComponent = new JenaDataSource(ModelFactory.createDefaultModel())
    val twoPortComponent = new DummyTwoPortAnalyzer()

    val matchedPipelines: Seq[Pipeline] = portMatcher.tryMatchPorts(twoPortComponent, Seq(buildInitialPipeline(sourceComponent)), 1).futureValue

    matchedPipelines shouldContainPipeline ExpectedPipeline(
      twoPortComponent,
      ExpectedBinding(sourceComponent, twoPortComponent.port1.name, twoPortComponent),
      ExpectedBinding(sourceComponent, twoPortComponent.port2.name, twoPortComponent)
    )
    matchedPipelines should have size 1
  }

  it should "extend longer pipeline with another compatible component" in {
    val portMatcher = createPortMatcher()
    val sourceComponent = new JenaDataSource(ModelFactory.createDefaultModel())
    val twoPortComponent = new DummyTwoPortAnalyzer()
    val visualizerComponent = new DummyVisualizer(Status.Success)

    val initialPipeline = buildInitialPipeline(sourceComponent)
    val twoPortCoponentState: Some[ComponentState] = Some(twoPortComponent.port2BoundState)
    val matchedPipelines: Seq[Pipeline] = portMatcher.tryMatchPorts(
      visualizerComponent,
      Seq(
        initialPipeline,
        pipelineBuilder.buildPartialPipeline(twoPortComponent, Seq(PortMatch(twoPortComponent.port1, initialPipeline, twoPortCoponentState), PortMatch(twoPortComponent.port2, initialPipeline, twoPortCoponentState)), 0).futureValue
      ),
      1
    ).futureValue

    matchedPipelines shouldContainPipeline ExpectedPipeline(
      visualizerComponent,
      ExpectedBinding(sourceComponent, twoPortComponent.port1.name, twoPortComponent),
      ExpectedBinding(sourceComponent, twoPortComponent.port2.name, twoPortComponent),
      ExpectedBinding(twoPortComponent, visualizerComponent.port.name, visualizerComponent)
    )
    matchedPipelines shouldContainPipeline ExpectedPipeline(visualizerComponent, ExpectedBinding(sourceComponent, visualizerComponent.port.name, visualizerComponent))
    matchedPipelines should have size 2
  }

  def buildInitialPipeline(sourceComponent: JenaDataSource): PartialPipeline = {
    pipelineBuilder.buildInitialPipeline(sourceComponent).futureValue
  }
}
