package discovery.components.analyzer

import scala.concurrent.ExecutionContext.Implicits.global
import discovery.components.common.DescriptorChecker
import discovery.model._
import discovery.model.components.{AnalyzerInstance, ExtractorInstance}
import discovery.model.components.descriptor.{AskDescriptor, ConstructDescriptor}

import scala.concurrent.Future

class TownsExtractor extends ExtractorInstance with DescriptorChecker {
  val portName: String = "INPUT_PORT"
  val port = Port(portName, 0)

  private val descriptor = AskDescriptor(
      """
      |    prefix xsd:  <http://www.w3.org/2001/XMLSchema#>
      |    prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
      |    prefix skos:  <http://www.w3.org/2004/02/skos/core#>
      |    prefix s:  <http://schema.org/>
      |    prefix ogcgml:  <http://www.opengis.net/ont/gml#>
      |    prefix ruian:  <http://ruian.linked.opendata.cz/ontology/>
      |    ASK {
      |    ?obec
      |      rdf:type  ruian:Obec ;
      |      skos:notation  ?notation ;
      |      s:name  ?name ;
      |      ruian:definicniBod  ?definicniBod ;
      |      ruian:lau  ?lau ;
      |      ruian:okres  ?okres ;
      |      ruian:pou  ?pou
      |      .
      |
      |    ?definicniBod  rdf:type  ogcgml:MultiPoint ;
      |      ogcgml:pointMember  ?pointMember .
      |
      |    ?pointMember rdf:type  ogcgml:Point ;
      |      ogcgml:pos  ?pos ;
      |      s:geo ?geo .
      |    ?geo  rdf:type  s:GeoCoordinates ;
      |      s:longitude  ?lng ;
      |      s:latitude  ?lat .
      |    }
      """.stripMargin
  )

  override def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample]): Future[DataSample] = {
    dataSamples(port).executeConstruct(ConstructDescriptor(
      """
        | prefix xsd:  <http://www.w3.org/2001/XMLSchema#>
        | prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        | prefix skos:  <http://www.w3.org/2004/02/skos/core#>
        | prefix s:  <http://schema.org/>
        | prefix ogcgml:  <http://www.opengis.net/ont/gml#>
        | prefix ruian:  <http://ruian.linked.opendata.cz/ontology/>
        |
        | CONSTRUCT {
        |    ?obec
        |      rdf:type  ruian:Obec ;
        |      skos:notation  ?notation ;
        |      s:name  ?name ;
        |      ruian:definicniBod  ?definicniBod ;
        |      ruian:lau  ?lau ;
        |      ruian:okres  ?okres ;
        |      ruian:pou  ?pou
        |      .
        |
        |    ?definicniBod  rdf:type  ogcgml:MultiPoint ;
        |      ogcgml:pointMember  ?pointMember .
        |
        |    ?pointMember rdf:type  ogcgml:Point ;
        |      ogcgml:pos  ?pos ;
        |      s:geo ?geo .
        |    ?geo  rdf:type  s:GeoCoordinates ;
        |      s:longitude  ?lng ;
        |      s:latitude  ?lat .
        |
        |    ?pou s:name ?pouname .
        |} WHERE {
        |    ?obec
        |      rdf:type  ruian:Obec ;
        |      skos:notation  ?notation ;
        |      s:name  ?name ;
        |      ruian:definicniBod  ?definicniBod ;
        |      ruian:lau  ?lau ;
        |      ruian:okres  ?okres ;
        |      ruian:pou  ?pou
        |      .
        |
        |    ?definicniBod  rdf:type  ogcgml:MultiPoint ;
        |      ogcgml:pointMember  ?pointMember .
        |
        |    ?pointMember rdf:type  ogcgml:Point ;
        |      ogcgml:pos  ?pos ;
        |      s:geo ?geo .
        |    ?geo  rdf:type  s:GeoCoordinates ;
        |      s:longitude  ?lng ;
        |      s:latitude  ?lat .
        |
        |    OPTIONAL { ?pou s:name ?pouname . }
        |}
      """.stripMargin)).map(ModelDataSample)
  }

  override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample): Future[PortCheckResult] = {
    checkStatelessDescriptors(outputDataSample, descriptor)
  }

  override val getInputPorts: Seq[Port] = Seq(port)
}
