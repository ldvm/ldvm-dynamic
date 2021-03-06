package discovery.components.transformer

import discovery.components.common.DescriptorChecker
import discovery.model._
import discovery.model.components.descriptor.AskDescriptor
import discovery.model.components.{TransformerInstance, UpdateQuery}

import scala.concurrent.Future

class FusionTransformer extends TransformerInstance with DescriptorChecker {
  val portName = "INPUT"
  val port = Port(portName, 0)

  private val descriptor = AskDescriptor(
    """
      |PREFIX owl: <http://www.w3.org/2002/07/owl#>
      |
      |ASK {
      |   ?entity1 owl:sameAs ?entity2 ;
      |       ?prop1 ?subj1 .
      |   ?entity2 ?prop2 ?subj2 .
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
        |
        | DELETE { ?entity1 owl:sameAs ?entity2 . }
        | INSERT {
        |   ?entity2 ?prop1 ?subj1 .
        |   ?entity2 ?prop2 ?subj2 .
        | }
        | WHERE {
        |   ?entity1 owl:sameAs ?entity2 ;
        |      ?prop1 ?subj1 .
        |   ?entity2 ?prop2 ?subj2 .
        |   FILTER (?prop1 != owl:sameAs)
        | }
      """.stripMargin))
    Future.successful(ModelDataSample(newSample))
  }
}
