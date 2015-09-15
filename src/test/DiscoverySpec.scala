import com.hp.hpl.jena.rdf.model.ModelFactory
import model.discovery._
import model.discovery.components.{DummyVisualizer, JenaDataSource}
import org.junit.runner._
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable._
import org.specs2.runner._

@RunWith(classOf[JUnitRunner])
class DiscoverySpec extends Specification {

  "Dsicovery" should {

    "discover dummy pipeline" >> { implicit ee: ExecutionEnv =>

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

      future must beEqualTo(Seq(Pipeline(
        Seq(dsComponent, vComponent),
        Seq(Binding(dsComponent, dummySuccessVisualizer.port, vComponent)),
        vComponent,
        DataSample()
      ))).await

    }
  }
}