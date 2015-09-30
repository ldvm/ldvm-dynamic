package discovery

import discovery.components.analyzer.{RuianGeocoderAnalyzer, TownsExtractorAnalyzer}
import discovery.components.datasource.JenaDataSource
import discovery.components.visualizer.GoogleMapsVisualizer
import discovery.model.DiscoveryInput
import org.scalatest.concurrent.ScalaFutures._

class RealPipelinesSpec extends LdvmSpec with DiscoveryCreator {

  "Discovery" should "discover LDOW 2015 pipelines for IPP and RUIAN" in {
    val ruian = new JenaDataSource(JenaUtil.modelFromTtlFile("ruian.ttl"))
    val institutions = new JenaDataSource(JenaUtil.modelFromTtlFile("ipp.ttl"))
    val googleMaps = new GoogleMapsVisualizer()
    val townsExtractor = new TownsExtractorAnalyzer()
    val ruianGeocoder = new RuianGeocoderAnalyzer()

    val input = new DiscoveryInput(
      Seq(ruian, institutions),
      Seq(googleMaps),
      Seq(townsExtractor, ruianGeocoder)
    )

    val pipelines = createDiscovery().discover(input).futureValue

    pipelines shouldContainPipeline ExpectedPipeline(
      googleMaps,
      ExpectedBinding(ruian, "PORT1", townsExtractor),
      ExpectedBinding(townsExtractor, "PORT1", googleMaps)
    )

    pipelines shouldContainPipeline ExpectedPipeline(
      googleMaps,
      ExpectedBinding(ruian, "PORT1", googleMaps)
    )

    pipelines shouldContainPipeline ExpectedPipeline(
      googleMaps,
      ExpectedBinding(ruian, "PORT1", townsExtractor),
      ExpectedBinding(townsExtractor, "PORT1", ruianGeocoder),
      ExpectedBinding(institutions, "PORT2", ruianGeocoder),
      ExpectedBinding(ruianGeocoder, "PORT1", googleMaps)
    )

    pipelines should have size 3
  }

}
