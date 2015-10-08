package discovery

import discovery.components.analyzer.{ClassBasedLinker, RuianGeocoderAnalyzer, TownsExtractorAnalyzer}
import discovery.components.datasource.JenaDataSource
import discovery.components.transformer.PopulationTransformer
import discovery.components.visualizer.{PopulationVisualizer, GoogleMapsVisualizer}
import discovery.model.DiscoveryInput
import org.scalatest.concurrent.ScalaFutures._

class RealPipelinesSpec extends LdvmSpec with DiscoveryCreator {

  "Discovery" should "discover LDOW 2015 pipelines for IPP and RUIAN" in {
    val ruian = new JenaDataSource(JenaUtil.modelFromTtlFile(getClass.getResource("ruian.ttl")), "ruian")
    val institutions = new JenaDataSource(JenaUtil.modelFromTtlFile(getClass.getResource("ipp.ttl")), "institutions")
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
      ExpectedBinding(ruian, googleMaps.portName, googleMaps)
    )

    pipelines shouldContainPipeline ExpectedPipeline(
      googleMaps,
      ExpectedBinding(ruian, townsExtractor.portName, townsExtractor),
      ExpectedBinding(townsExtractor, googleMaps.portName, googleMaps)
    )

    pipelines shouldContainPipeline ExpectedPipeline(
      googleMaps,
      ExpectedBinding(ruian, ruianGeocoder.geoPortName, ruianGeocoder),
      ExpectedBinding(institutions, ruianGeocoder.linkPortName, ruianGeocoder),
      ExpectedBinding(ruianGeocoder, googleMaps.portName, googleMaps)
    )

    pipelines shouldContainPipeline ExpectedPipeline(
      googleMaps,
      ExpectedBinding(ruian, townsExtractor.portName, townsExtractor),
      ExpectedBinding(townsExtractor, ruianGeocoder.geoPortName, ruianGeocoder),
      ExpectedBinding(institutions, ruianGeocoder.linkPortName, ruianGeocoder),
      ExpectedBinding(ruianGeocoder, googleMaps.portName, googleMaps)
    )

    pipelines should have size 4
  }

  it should "discover population visualization pipelines" in {
    val ruian = new JenaDataSource(PopulationModels.ruian, "Ruian")
    val dbpedia = new JenaDataSource(PopulationModels.dbpedia, "DBPedia")
    val ruianDbpediaLinks = new JenaDataSource(PopulationModels.ruianDbpediaLinks)
    val linker = new ClassBasedLinker()
    val populationTransformer = new PopulationTransformer()
    val populationVisualizer = new PopulationVisualizer()

    val input = new DiscoveryInput(
      Seq(ruian, dbpedia, ruianDbpediaLinks),
      Seq(populationVisualizer),
      Seq(linker, populationTransformer)
    )

    val pipelines = createDiscovery().discover(input).futureValue

    pipelines shouldContainPipeline ExpectedPipeline(
      populationVisualizer,
      ExpectedBinding(ruian, linker.dataSource1PortName, linker),
      ExpectedBinding(dbpedia, linker.dataSource2PortName, linker),
      ExpectedBinding(ruianDbpediaLinks, linker.linksPortName, linker),
      ExpectedBinding(linker, populationTransformer.portName, populationTransformer),
      ExpectedBinding(populationTransformer, populationVisualizer.portName, populationVisualizer)
    )
  }

}
