package discovery

import com.hp.hpl.jena.rdf.model.ModelFactory
import discovery.components.{DummyTwoPortAnalyzer, DummyVisualizer, JenaDataSource}
import discovery.model.PortCheckResult.Status
import discovery.model._
import org.scalatest.concurrent.ScalaFutures._

class DiscoverySpec extends LdvmSpec with DiscoveryCreator {
  "Discovery" should "discover pipeline from two matching components" in {
    val dummySource = new JenaDataSource(ModelFactory.createDefaultModel())
    val dummySuccessVisualizer = new DummyVisualizer(Status.Success)

    val input = new DiscoveryInput(Seq(dummySource), Seq(dummySuccessVisualizer), Seq())
    val pipelines = createDiscovery().discover(input).futureValue

    pipelines shouldContainPipeline ExpectedPipeline(dummySuccessVisualizer, ExpectedBinding(dummySource, "PORT1", dummySuccessVisualizer))
    pipelines should have size 1
  }

  it should "discover nothing with two non-matching components" in {
    val dummySource = new JenaDataSource(ModelFactory.createDefaultModel())
    val dummySuccessVisualizer = new DummyVisualizer(Status.Failure)

    val input = new DiscoveryInput(Seq(dummySource), Seq(dummySuccessVisualizer), Seq())
    val pipelines = createDiscovery().discover(input).futureValue

    pipelines shouldBe empty
  }

  it should "discover two possible pipelines" in {
    val sourceComponent = new JenaDataSource(ModelFactory.createDefaultModel())
    val visualizerComponent1 = new DummyVisualizer(Status.Success)
    val visualizerComponent2 = new DummyVisualizer(Status.Success)

    val input = new DiscoveryInput(Seq(sourceComponent), Seq(visualizerComponent1, visualizerComponent2), Seq())
    val pipelines = createDiscovery().discover(input).futureValue

    pipelines shouldContainPipeline ExpectedPipeline(visualizerComponent1, ExpectedBinding(sourceComponent, "PORT1", visualizerComponent1))
    pipelines shouldContainPipeline ExpectedPipeline(visualizerComponent2, ExpectedBinding(sourceComponent, "PORT1", visualizerComponent2))
    pipelines should have size 2
  }

  ignore should "bind analyzer with two ports in correct order" in {
    val sourceComponent = new JenaDataSource(ModelFactory.createDefaultModel())
    val analyzerComponent = new DummyTwoPortAnalyzer()
    val visualizerComponent = new DummyVisualizer(Status.Success)

    val input = new DiscoveryInput(Seq(sourceComponent), Seq(visualizerComponent), Seq(analyzerComponent))
    val pipelines = createDiscovery().discover(input).futureValue

    pipelines.foreach(println)
  }
}