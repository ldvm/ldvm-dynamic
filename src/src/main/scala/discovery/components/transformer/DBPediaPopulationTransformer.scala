package discovery.components.transformer

import discovery.components.common.DescriptorChecker
import discovery.model._
import discovery.model.components.descriptor.AskDescriptor
import discovery.model.components.{TransformerInstance, UpdateQuery}

import scala.concurrent.Future

class DBPediaPopulationTransformer extends TransformerInstance with DescriptorChecker {
  val portName = "INPUT"
  val port = Port(portName, 0)

  private val descriptor = AskDescriptor(
    """
      |PREFIX dbo: <http://dbpedia.org/ontology/>
      |
      |ASK {
      |   ?place dbo:populationTotal ?pop .
      |}
    """.stripMargin
  )

  override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample): Future[PortCheckResult] = {
    checkStatelessDescriptors(outputDataSample, descriptor)
  }

  override def getInputPorts: Seq[Port] = Seq(port)

  override def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample]): Future[DataSample] = {
    val newSample = dataSamples(port).transform(UpdateQuery(
      """
        | PREFIX owl: <http://www.w3.org/2002/07/owl#>
        | PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        | PREFIX dbo: <http://dbpedia.org/ontology/>
        |
        | DELETE { ?place dbo:populationTotal ?pop . }
        | INSERT { ?place rdf:Value ?pop . }
        | WHERE { ?place dbo:populationTotal ?pop . }
        |
      """.stripMargin))
    Future.successful(ModelDataSample(newSample))
  }
}
