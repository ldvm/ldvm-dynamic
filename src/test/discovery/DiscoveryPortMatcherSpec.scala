package discovery

import com.hp.hpl.jena.rdf.model.ModelFactory
import discovery.components.{DummyTwoPortAnalyzer, JenaDataSource}
import discovery.model.Pipeline
import org.scalatest.concurrent.ScalaFutures._

class DiscoveryPortMatcherSpec extends LdvmSpec with DiscoveryCreator {
  "DiscoveryPortMatcher" should "match two port components with compatible partial pipelines" in {
    val portMatcher = createPortMatcher()

    val sourceComponent = new JenaDataSource(ModelFactory.createDefaultModel())
    val twoPortComponent = new DummyTwoPortAnalyzer()

    val matchedPipelines: Seq[Pipeline] = portMatcher.tryMatchPorts(twoPortComponent, Seq(buildInitialPipeline(sourceComponent))).futureValue

    assertContainsPipeline(
      matchedPipelines,
      ExpectedPipeline(
        twoPortComponent,
        ExpectedBinding(sourceComponent, twoPortComponent.port1.name, twoPortComponent),
        ExpectedBinding(sourceComponent, twoPortComponent.port2.name, twoPortComponent)
      )
    )
  }

  def buildInitialPipeline(sourceComponent: JenaDataSource): Pipeline = {
    pipelineBuilder.buildInitialPipeline(sourceComponent).futureValue
  }
}
