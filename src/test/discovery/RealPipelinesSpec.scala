package discovery

import com.hp.hpl.jena.rdf.model.ModelFactory
import discovery.components.analyzer.{TownsExtractorAnalyzer, RuianGeocoderAnalyzer}
import discovery.components.datasource.JenaDataSource
import discovery.components.visualizer.GoogleMapsVisualizer
import discovery.model.{RdfDataSample, DataSample, DiscoveryInput}
import org.scalatest.LoneElement._
import org.scalatest.concurrent.ScalaFutures._

class RealPipelinesSpec extends LdvmSpec {

  "Discovery" should "discover LDOW 2015 pipeline" in {


    val ruian = new JenaDataSource(ModelFactory.createDefaultModel()) // TODO ruian sample
    val institutions = new JenaDataSource(ModelFactory.createDefaultModel()) // TODO IPP sample
    val googleMaps = new GoogleMapsVisualizer()
    val townsExtractor = new TownsExtractorAnalyzer()
    val ruianGeocoder = new RuianGeocoderAnalyzer()

    val input = new DiscoveryInput(
      Seq(ruian),
      Seq(googleMaps),
      Seq(townsExtractor, ruianGeocoder)
    )

    val future = createDiscovery().discover(input)

    val pipeline = future.futureValue.loneElement
    assertBindings(
      pipeline,
      ExpectedBinding(ruian, "PORT1", townsExtractor),
      ExpectedBinding(townsExtractor, "PORT1", ruianGeocoder),
      ExpectedBinding(institutions, "PORT2", ruianGeocoder),
      ExpectedBinding(ruianGeocoder, "PORT1", googleMaps)
    )

    assertCorrectComponents(pipeline)
    assertOutput(pipeline, googleMaps, RdfDataSample(""))

  }

}
