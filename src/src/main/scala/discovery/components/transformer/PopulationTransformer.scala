package discovery.components.transformer

import discovery.components.common.DescriptorChecker
import discovery.model._
import discovery.model.components.TransformerInstance
import discovery.model.components.descriptor.AskDescriptor

import scala.concurrent.Future

class PopulationTransformer extends TransformerInstance with DescriptorChecker {
  val portName = "INPUT"

  private val descriptor = AskDescriptor(
    """
      |PREFIX s: <http://schema.org/>
      |PREFIX dbo: <http://dbpedia.org/ontology/>
      |prefix ogcgml:  <http://www.opengis.net/ont/gml#>
      |prefix ruian: <http://ruian.linked.opendata.cz/ontology/>
      |prefix ruianlink: <http://ruian.linked.opendata.cz/ontology/links/>
      |PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
      |
      |    ASK {
      |      ?p a dbo:PopulatedPlace ;
      |         dbo:populationTotal ?population ;
      |         ruianlink:obec ?object .
      |
      |      ?object
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

  override def getInputPorts: Seq[Port] = Seq(Port(portName, 0))

  override def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample]): Future[DataSample] = ???
}
