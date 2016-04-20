package discovery.components.transformer

import discovery.components.common.DescriptorChecker
import discovery.model._
import discovery.model.components.TransformerInstance
import discovery.model.components.descriptor.{AskDescriptor, ConstructDescriptor}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PopulationTransformer extends TransformerInstance with DescriptorChecker {
  val portName = "INPUT"
  val port = Port(portName, 0)

  private val descriptor = AskDescriptor(
    """
      |PREFIX s: <http://schema.org/>
      |PREFIX dbo: <http://dbpedia.org/ontology/>
      |prefix ogcgml:  <http://www.opengis.net/ont/gml#>
      |prefix ruian: <http://ruian.linked.opendata.cz/ontology/>
      |prefix ruianlink: <http://ruian.linked.opendata.cz/ontology/links/>
      |PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
      |PREFIX owl: <http://www.w3.org/2002/07/owl#>
      |
      |    ASK {
      |      ?p a dbo:PopulatedPlace ;
      |         dbo:populationTotal ?population ;
      |         ruian:definicniBod  ?definicniBod .
      |
      |      ?definicniBod rdf:type  ogcgml:MultiPoint ;
      |         ogcgml:pointMember  ?pointMember .
      |
      |      ?pointMember rdf:type ogcgml:Point ;
      |         s:geo ?geo .
      |      ?geo  rdf:type  s:GeoCoordinates ;
      |         s:longitude ?lng ;
      |         s:latitude  ?lat .
      |    }
    """.stripMargin
  )

  override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample): Future[PortCheckResult] = {
    checkStatelessDescriptors(outputDataSample, descriptor)
  }

  override def getInputPorts: Seq[Port] = Seq(port)

  override def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample]): Future[DataSample] = {
    dataSamples(port).executeConstruct(ConstructDescriptor(
      """
        | PREFIX s: <http://schema.org/>
        | PREFIX dbo: <http://dbpedia.org/ontology/>
        | PREFIX dbp: <http://dbpedia.org/property/>
        | PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        | PREFIX ruian: <http://ruian.linked.opendata.cz/ontology/>
        | PREFIX ogcgml:  <http://www.opengis.net/ont/gml#>
        |
        | CONSTRUCT {
        |  ?p a dbo:PopulatedPlace ;
        |     a ?type ;
        |     dbo:populationTotal ?population ;
        |     rdf:type s:Place ;
        |     s:name ?name ;
        |     s:geo [
        |       rdf:type s:GeoCoordinates ;
        |       s:longitude ?lng ;
        |       s:latitude  ?lat
        |     ] .
        | } WHERE {
        |  ?p a dbo:PopulatedPlace ;
        |     dbp:officialName ?name ;
        |     dbo:populationTotal ?population ;
        |     ruian:definicniBod ?definicniBod .
        |
        |  OPTIONAL { ?p a ?type . }
        |
        |  ?definicniBod rdf:type ogcgml:MultiPoint ;
        |     ogcgml:pointMember ?pointMember .
        |
        |  ?pointMember rdf:type ogcgml:Point ;
        |     s:geo ?geo .
        |  ?geo rdf:type s:GeoCoordinates ;
        |     s:longitude ?lng ;
        |     s:latitude ?lat .
        | }
        |
      """.stripMargin)).map{ m =>
      val s = ModelDataSample(m)
      s
    }
  }
}
