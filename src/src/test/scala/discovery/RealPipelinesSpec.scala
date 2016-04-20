package discovery

import discovery.components.analyzer.{ClassBasedLinker, PopulatedPlacesAnalyzer, RuianGeocoderAnalyzer, TownsExtractorAnalyzer}
import discovery.components.datasource.JenaDataSource
import discovery.components.transformer.{FusionTransformer, PopulationTransformer}
import discovery.components.visualizer.{GoogleMapsVisualizer, PopulationVisualizer}
import discovery.model.DiscoveryInput
import org.scalatest.concurrent.ScalaFutures._

class RealPipelinesSpec extends LdvmSpec with DiscoveryCreator {

  ignore should "discover LDOW 2015 pipelines for IPP and RUIAN" in {
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

  "Discovery" should "discover population visualization pipelines" in {
    val ruian = new JenaDataSource(PopulationModels.ruian, "Ruian")
    val dbpedia = new JenaDataSource(PopulationModels.dbpedia, "DBPedia")
    val ruianDbpediaLinks = new JenaDataSource(PopulationModels.ruianDbpediaLinks)
    val linker = new ClassBasedLinker()
    val fusion = new FusionTransformer
    val populationTransformer = new PopulationTransformer()
    val populationVisualizer = new PopulationVisualizer()
    val townsExtractor = new TownsExtractorAnalyzer()
    val populatedPlacesExtractor = new PopulatedPlacesAnalyzer()

    val input = new DiscoveryInput(
      Seq(ruian, dbpedia, ruianDbpediaLinks),
      Seq(populationVisualizer),
      Seq(linker, populationTransformer, fusion, townsExtractor, populatedPlacesExtractor)
    )

    val pipelines = createDiscovery().discover(input).futureValue

    pipelines shouldContainPipeline ExpectedPipeline(
      populationVisualizer,
      ExpectedBinding(ruian, townsExtractor.portName, townsExtractor),
      ExpectedBinding(townsExtractor, linker.dataSource1PortName, linker),
      ExpectedBinding(dbpedia, populatedPlacesExtractor.port, populatedPlacesExtractor),
      ExpectedBinding(populatedPlacesExtractor, linker.dataSource2PortName, linker),
      ExpectedBinding(ruianDbpediaLinks, linker.linksPortName, linker),
      ExpectedBinding(linker, populationTransformer.portName, fusion),
      ExpectedBinding(fusion, fusion.portName, populationTransformer),
      ExpectedBinding(populationTransformer, populationVisualizer.portName, populationVisualizer)
    )
  }

}
