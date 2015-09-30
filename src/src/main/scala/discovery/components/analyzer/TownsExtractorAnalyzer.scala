package discovery.components.analyzer

import discovery.JenaUtil
import discovery.model._
import discovery.model.components.AnalyzerInstance
import discovery.model.components.descriptor.{AskDescriptor, Descriptor}

import scala.concurrent.Future

class TownsExtractorAnalyzer extends AnalyzerInstance {
  val portName: String = "INPUT_PORT"

  private val descriptors = Seq(
    AskDescriptor(
      """
      |   prefix xsd:  <http://www.w3.org/2001/XMLSchema#>
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
      |
      |    ?pou s:name ?pouname .
      |    }
      """.stripMargin
    )
  )

  override def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample]): Future[DataSample] = {
    val filePath = getClass.getResource("ldvm-ruian-obce-datasample.ttl").getFile
    Future.successful(ModelDataSample(JenaUtil.modelFromTtlFile(filePath)))
  }

  override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample): Future[PortCheckResult] = {
    super.checkAskDescriptors(port, outputDataSample)
  }

  override val getInputPorts: Seq[Port] = Seq(Port(portName, 0))

  override def descriptorsForPort(port: Port): Seq[Descriptor] = port match {
    case Port(`portName`, _) => descriptors
  }
}
