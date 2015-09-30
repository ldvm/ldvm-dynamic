package discovery.components.analyzer

import discovery.model._
import discovery.model.components.AnalyzerInstance
import discovery.model.components.descriptor.{AskDescriptor, Descriptor}

import scala.concurrent.Future

class RuianGeocoderAnalyzer extends AnalyzerInstance {
  val linkPortName: String = "PORT_LINK"
  val geoPortName: String = "PORT_GEO"

  override def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample]): Future[DataSample] = {
    Future.successful(RdfDataSample(
      """
        | PREFIX s: <http://schema.org/>
        |
        | <http://example.com/entity/geo> s:geo [
        |   a s:GeoCoordinates ;
        |   s:latitude 15.13 ;
        |   s:longitude 50.72
        | ] .
      """.stripMargin
    ))
  }

  private val geoPortDescriptors = Seq(
    AskDescriptor(
      """
        |   prefix xsd: <http://www.w3.org/2001/XMLSchema#>
        |   prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        |   prefix skos:  <http://www.w3.org/2004/02/skos/core#>
        |   prefix s: <http://schema.org/>
        |   prefix ogcgml:  <http://www.opengis.net/ont/gml#>
        |   prefix ruian: <http://ruian.linked.opendata.cz/ontology/>
        |   ASK {
        |   ?object
        |     ruian:definicniBod  ?definicniBod .
        |
        |   ?definicniBod rdf:type  ogcgml:MultiPoint ;
        |     ogcgml:pointMember  ?pointMember .
        |
        |   ?pointMember rdf:type ogcgml:Point ;
        |     s:geo ?geo .
        |   ?geo  rdf:type  s:GeoCoordinates ;
        |     s:longitude ?lng ;
        |     s:latitude  ?lat .
        |   }
      """.stripMargin
    )
  )

  private val linkPortDescriptors = Seq(
    AskDescriptor(
      """
        |   prefix ruianlink: <http://ruian.linked.opendata.cz/ontology/links/>
        |   ASK {
        |     ?object ruianlink:obec  ?obec .
        |   }
      """.stripMargin
    )
  )

  override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample): Future[PortCheckResult] = {
    super.checkAskDescriptors(port, outputDataSample)
  }

  override val getInputPorts: Seq[Port] = Seq(Port(linkPortName, 0), Port(geoPortName, 0))

  override def descriptorsForPort(port: Port): Seq[Descriptor] = port match {
    case Port(`linkPortName`, _) => linkPortDescriptors
    case Port(`geoPortName`, _) => geoPortDescriptors
  }
}
