package discovery

import com.hp.hpl.jena.rdf.model.Model

object PopulationModels {

  private val commonPrefixes =
    """
      | PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
      | PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
      | PREFIX owl: <http://www.w3.org/2002/07/owl#>
      | PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
      | PREFIX ldvm: <http://linked.opendata.cz/ontology/ldvm/>
      | PREFIX ruianlink: <http://ruian.linked.opendata.cz/ontology/links>
      | PREFIX ruiands: <http://ruian.linked.opendata.cz/resource/dataset>
      | PREFIX void: <http://rdfs.org/ns/void#>
      |
      |
    """.stripMargin

  val dbpedia: Model = JenaUtil.modelFromTtl(
    commonPrefixes +
      """
        | PREFIX dbo: <http://dbpedia.org/ontology/>
        | PREFIX dbr: <http://dbpedia.org/resource/>
        | PREFIX dbp: <http://dbpedia.org/property/>
        |
        | dbr:Prague rdf:type <http://schema.org/City> ;
        |   dbo:populationTotal	"1243201"^^xsd:nonNegativeInteger ;
        |   a dbo:PopulatedPlace ;
        |   dbp:officialName	"Prague"@cs .
      """.stripMargin
  )

  val ruian: Model = JenaUtil.modelFromTtl(
    commonPrefixes +
      """
        |@prefix xsd:	<http://www.w3.org/2001/XMLSchema#> .
        |@prefix rdf:	<http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
        |@prefix skos:	<http://www.w3.org/2004/02/skos/core#> .
        |@prefix s:	<http://schema.org/> .
        |@prefix ogcgml:	<http://www.opengis.net/ont/gml#> .
        |
        |@prefix ruian:	<http://ruian.linked.opendata.cz/ontology/> .
        |
        |<http://ruian.linked.opendata.cz/resource/obce/554782>
        |	rdf:type	ruian:Obec ;
        |	skos:notation	"554782" ;
        |	s:name	"Praha" ;
        |	ruian:definicniBod	<http://ruian.linked.opendata.cz/resource/obce/554782/multi-point/DOB.554782> ;
        |	ruian:lau	<http://linked.opendata.cz/resource/region/554782> ;
        |	ruian:okres	<http://ruian.linked.opendata.cz/resource/okresy/3100> ;
        |	ruian:pou	<http://ruian.linked.opendata.cz/resource/pou/19> ;
        |	.
        |
        |<http://ruian.linked.opendata.cz/resource/obce/554782/multi-point/DOB.554782>	rdf:type	ogcgml:MultiPoint ;
        |	ogcgml:pointMember	<http://ruian.linked.opendata.cz/resource/obce/554782/multi-point/DOB.554782/point/DOB.554782.1> .
        |
        |<http://ruian.linked.opendata.cz/resource/obce/554782/multi-point/DOB.554782/point/DOB.554782.1>	rdf:type	ogcgml:Point ;
        |	ogcgml:pos	"-743100.00 -1043300.00" ;
        |	s:geo <http://ruian.linked.opendata.cz/resource/obce/554782/multi-point/DOB.554782/point/DOB.554782.1/wgs84> .
        |
        |<http://ruian.linked.opendata.cz/resource/obce/554782/multi-point/DOB.554782/point/DOB.554782.1/wgs84>	rdf:type	s:GeoCoordinates ;
        |	s:longitude	"14.417780627777776913944762782193720340728759765625" ;
        |	s:latitude	"50.084552136111113895822199992835521697998046875" .
      """.stripMargin
  )

  val ruianDbpediaLinks = JenaUtil.modelFromTtl(
    commonPrefixes +
    """
      | <http://ruian.linked.opendata.cz/resource/obce/554782> owl:sameAs <http://dbpedia.org/resource/Prague> .
      |
      | ruiands:Ruian_Dbpedia a void:Linkset ;
      |     void:subjectsTarget <http://ruian.linked.opendata.cz/dataset/ruian/obce> ;
      |     void:objectsTarget <http://dbpedia.org/dataset/populatedPlaces> ;
      |     void:linkPredicate owl:sameAs ;
      |     void:exampleResource <http://ruian.linked.opendata.cz/resource/obce/554782> ;
      |     void:uriSpace "http://ruian.linked.opendata.cz/resource/obce/" .
      |
      | <http://ruian.linked.opendata.cz/dataset/ruian/obce> a void:Dataset ;
      |     void:class <http://ruian.linked.opendata.cz/ontology/Obec> .
      |
      | <http://dbpedia.org/dataset/populatedPlaces> a void:Dataset ;
      |     void:class <http://dbpedia.org/ontology/PopulatedPlace> .
      |
    """.stripMargin
  )

}
