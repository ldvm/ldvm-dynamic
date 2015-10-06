package discovery

import com.hp.hpl.jena.rdf.model.Model

object PopulationModels {

  private val commonPrefixes =
    """
      | PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
      | PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
      | PREFIX owl: <http://www.w3.org/2002/07/owl#>
      | PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
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
        |   rdfs:label	"Prague"@en .
      """.stripMargin
  )

  val ruian: Model = JenaUtil.modelFromTtl(
    commonPrefixes +
      """
        | PREFIX ruian:	<http://ruian.linked.opendata.cz/ontology/>
        | PREFIX ogcgml: <http://www.opengis.net/ont/gml#>
        |
        | <http://ruian.linked.opendata.cz/resource/obce/554782> rdf:type ruian:Obec ;
        |   <http://schema.org/name> "Praha" ;
        |   ruian:definicniBod <http://ruian.linked.opendata.cz/resource/obce/554782/multi-point/DOB.554782> .
        |
        | <http://ruian.linked.opendata.cz/resource/obce/554782/multi-point/DOB.554782> rdf:type ogcgml:MultiPoint ;
        |   ogcgml:pointMember <http://ruian.linked.opendata.cz/resource/obce/554782/multi-point/DOB.554782/point/DOB.554782.1> .
        |
        | <http://ruian.linked.opendata.cz/resource/obce/554782/multi-point/DOB.554782/point/DOB.554782.1> rdf:type ogcgml:Point ;
        |   ogcgml:id	"DOB.554782.1" ;
        |   ogcgml:pos "-743100.00 -1043300.00" ;
        |   ogcgml:srsDimension	2 ;
        |   <http://schema.org/geo>	<http://ruian.linked.opendata.cz/resource/obce/554782/multi-point/DOB.554782/point/DOB.554782.1/wgs84> .
        |
        | <http://ruian.linked.opendata.cz/resource/obce/554782/multi-point/DOB.554782/point/DOB.554782.1/wgs84> rdf:type <http://schema.org/GeoCoordinates> ;
        |   <http://schema.org/longitude> "14.417780627777776913944762782193720340728759765625" ;
        |   <http://schema.org/latitude> "50.084552136111113895822199992835521697998046875" .
      """.stripMargin
  )

  val ruianDbpediaLinks = JenaUtil.modelFromTtl(
    commonPrefixes +
    """
      | <http://ruian.linked.opendata.cz/resource/obce/554782> owl:sameAs <http://dbpedia.org/resource/Prague> .
      | <http://ruian.linked.opendata.cz/resource/obce/554782> rdf:type <http://ruian.linked.opendata.cz/ontology/Obec> .
      | <http://dbpedia.org/resource/Prague> rdf:type <http://schema.org/City> .
    """.stripMargin // TODO: specify linked classes differently, in metadata global for entire dataset??
  )

}
