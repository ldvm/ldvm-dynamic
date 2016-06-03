package discovery

import discovery.components.analyzer.{LinksetBasedUnion, PopulatedPlacesExtractor, RuianGeocoderAnalyzer, TownsExtractor}
import discovery.components.datasource.JenaDataSource
import discovery.components.transformer.{DBPediaPopulationTransformer, FusionTransformer, Gml2SchemaOrgTransformer}
import discovery.components.visualizer.{GoogleMapsVisualizer, PopulationVisualizer}
import discovery.model.DiscoveryInput
import org.scalatest.concurrent.ScalaFutures._

class RealPipelinesSpec extends LdvmSpec with DiscoveryCreator {

  ignore should "discover LDOW 2015 pipelines for IPP and RUIAN" in {
    val ruian = new JenaDataSource(JenaUtil.modelFromTtlFile(getClass.getResource("ruian.ttl")), "ruian")
    val institutions = new JenaDataSource(JenaUtil.modelFromTtlFile(getClass.getResource("ipp.ttl")), "institutions")
    val googleMaps = new GoogleMapsVisualizer()
    val townsExtractor = new TownsExtractor()
    val ruianGeocoder = new RuianGeocoderAnalyzer()

    val input = new DiscoveryInput(
      Seq(ruian, institutions),
      Seq(townsExtractor),
      Seq(googleMaps),
      Seq(ruianGeocoder)
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
    val ruian = new JenaDataSource(PopulationModels.ruian, "Ruian", isLarge = true)
    val dbpedia = new JenaDataSource(PopulationModels.dbpedia, "DBPedia", isLarge = true)
    val ruianDbpediaLinks = new JenaDataSource(PopulationModels.ruianDbpediaLinks, "Ruian2DbPedia", isLinkset = true)
    val linker = new LinksetBasedUnion()
    val fusion = new FusionTransformer
    val gml2SchemaOrg = new Gml2SchemaOrgTransformer()
    val populationVisualizer = new PopulationVisualizer()
    val townsExtractor = new TownsExtractor()
    val populatedPlacesExtractor = new PopulatedPlacesExtractor()
    val populationTransformer = new DBPediaPopulationTransformer()

    val input = new DiscoveryInput(
      Seq(ruian, dbpedia, ruianDbpediaLinks),
      Seq(townsExtractor, populatedPlacesExtractor),
      Seq(populationVisualizer),
      Seq(linker, gml2SchemaOrg, fusion, populationTransformer)
    )

    val pipelines = createDiscovery().discover(input).futureValue

    pipelines shouldContainPipeline ExpectedPipeline(
      populationVisualizer,
      ExpectedBinding(ruian, townsExtractor.portName, townsExtractor),
      ExpectedBinding(townsExtractor, linker.dataSource1PortName, linker),
      ExpectedBinding(dbpedia, populatedPlacesExtractor.port, populatedPlacesExtractor),
      ExpectedBinding(populatedPlacesExtractor, linker.dataSource2PortName, linker),
      ExpectedBinding(ruianDbpediaLinks, linker.linksPortName, linker),
      ExpectedBinding(linker, gml2SchemaOrg.portName, fusion),
      ExpectedBinding(fusion, fusion.portName, gml2SchemaOrg),
      ExpectedBinding(gml2SchemaOrg, fusion.portName, populationTransformer),
      ExpectedBinding(populationTransformer, populationVisualizer.portName, populationVisualizer)
    )
  }

}
