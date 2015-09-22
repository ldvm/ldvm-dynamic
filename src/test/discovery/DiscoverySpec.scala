package discovery

import com.hp.hpl.jena.rdf.model.ModelFactory
import discovery.components.datasource.JenaDataSource
import discovery.components.visualizer.DummyVisualizer
import discovery.model.PortCheckResult.Status
import discovery.model._
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

    val future = createDiscovery().discover(input)

    val pipeline = future.futureValue.loneElement
    assertBindings(pipeline, ExpectedBinding(dummySource, "PORT1", dummySuccessVisualizer))
    assertCorrectComponents(pipeline)
    assertOutput(pipeline, dummySuccessVisualizer, RdfDataSample(""))
  }


  it should "discover nothing" in {

    val dummySource = new JenaDataSource(ModelFactory.createDefaultModel())
    val dummySuccessVisualizer = new DummyVisualizer(Status.Failure)

    val input = new DiscoveryInput(
      Seq(dummySource),
      Seq(dummySuccessVisualizer),
      Seq()
    )

    val future = createDiscovery().discover(input)

    future.futureValue shouldBe empty
  }

}