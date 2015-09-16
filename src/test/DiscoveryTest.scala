import com.hp.hpl.jena.rdf.model.ModelFactory
import model.discovery._
import model.discovery.components.{DummyVisualizer, JenaDataSource}
import org.scalatest.concurrent.ScalaFutures._
import org.specs2.concurrent.ExecutionEnv

class DiscoveryTest extends LdvmSpec {

  "Dsicovery" should "disvoer dummy pipeline" in { implicit ee: ExecutionEnv =>
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

      future.futureValue shouldBe Seq(Pipeline(
        Seq(dsComponent, vComponent),
        Seq(Binding(dsComponent, dummySuccessVisualizer.port, vComponent)),
        vComponent,
        DataSample()
      ))
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

      future.futureValue shouldBe Seq()
  }
}