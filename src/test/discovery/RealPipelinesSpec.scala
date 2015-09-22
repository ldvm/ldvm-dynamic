package discovery

import discovery.components.analyzer.{RuianGeocoderAnalyzer, TownsExtractorAnalyzer}
import discovery.components.datasource.JenaDataSource
import discovery.components.visualizer.GoogleMapsVisualizer
import discovery.model.{DiscoveryInput, RdfDataSample}
import org.scalatest.LoneElement._
import org.scalatest.concurrent.ScalaFutures._

class RealPipelinesSpec extends LdvmSpec {

  var ttlPath = "src/test/discovery/ttl/"

  "Discovery" should "discover LDOW 2015 pipeline" in {

    val ruian = new JenaDataSource(JenaUtil.modelFromTtl(ttlPath + "ruian.ttl"))
    val institutions = new JenaDataSource(JenaUtil.modelFromTtl(ttlPath + "ipp.ttl"))
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
