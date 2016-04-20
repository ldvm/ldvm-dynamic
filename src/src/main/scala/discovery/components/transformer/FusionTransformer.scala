package discovery.components.transformer

import discovery.components.common.DescriptorChecker
import discovery.model._
import discovery.model.components.TransformerInstance
import discovery.model.components.descriptor.{AskDescriptor, ConstructDescriptor}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FusionTransformer extends TransformerInstance with DescriptorChecker {
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
      |         dbo:populationTotal ?population .
      |
      |      ?object
      |         ruian:definicniBod ?definicniBod ;
      |         owl:sameAs ?p .
      |
      |      ?definicniBod rdf:type ogcgml:MultiPoint ;
      |         ogcgml:pointMember ?pointMember .
      |
      |      ?pointMember rdf:type ogcgml:Point ;
      |         s:geo ?geo .
      |      ?geo  rdf:type  s:GeoCoordinates ;
      |         s:longitude ?lng ;
      |         s:latitude ?lat .
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
        | PREFIX owl: <http://www.w3.org/2002/07/owl#>
        | CONSTRUCT {
        |   ?entity2 ?prop1 ?subj1 .
        |   ?entity2 ?prop2 ?subj2 .
        |   ?s ?p ?o .
        | } WHERE {
        |   ?entity1 owl:sameAs ?entity2 ;
        |      ?prop1 ?subj1 .
        |   ?entity2 ?prop2 ?subj2 .
        |   ?s ?p ?o .
        |   FILTER NOT EXISTS { ?s ?p ?entity1 . }
        |   FILTER NOT EXISTS { ?entity1 ?p ?o . }
        |   FILTER (?prop1 != owl:sameAs)
        | }
      """.stripMargin)).map { s =>
      ModelDataSample(s)
    }
  }
}
