package discovery

import discovery.components.analyzer.{RuianGeocoderAnalyzer, TownsExtractorAnalyzer}
import discovery.components.datasource.JenaDataSource
import discovery.components.visualizer.GoogleMapsVisualizer
import discovery.model.DiscoveryInput
import org.scalatest.concurrent.ScalaFutures._

class RealPipelinesSpec extends LdvmSpec with DiscoveryCreator {

  var ttlPath = "ttl/"

  "Discovery" should "discover LDOW 2015 pipeline" in {
    val ruian = new JenaDataSource(JenaUtil.modelFromTtlFile(ttlPath + "ruian.ttl"))
    val institutions = new JenaDataSource(JenaUtil.modelFromTtlFile(ttlPath + "ipp.ttl"))
    val googleMaps = new GoogleMapsVisualizer()
    val townsExtractor = new TownsExtractorAnalyzer()
    val ruianGeocoder = new RuianGeocoderAnalyzer()

    val input = new DiscoveryInput(
      Seq(ruian),
      Seq(googleMaps),
      Seq(townsExtractor, ruianGeocoder)
    )

    val pipelines = createDiscovery().discover(input).futureValue

    pipelines shouldContainPipeline ExpectedPipeline(
      googleMaps,
      ExpectedBinding(ruian, "PORT1", townsExtractor),
      ExpectedBinding(townsExtractor, "PORT1", ruianGeocoder),
      ExpectedBinding(institutions, "PORT2", ruianGeocoder),
      ExpectedBinding(ruianGeocoder, "PORT1", googleMaps)
    )
    pipelines should have size 1
  }

}
