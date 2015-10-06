package discovery.components

import discovery.model.PortCheckResult.Status._
import discovery.model._
import discovery.model.components.TransformerInstance
import discovery.model.components.descriptor.Descriptor

import scala.concurrent.Future

class DummyTransformer(expectedPortCheckResult: Status) extends TransformerInstance {
  val port = Port("PORT1", 1)

  override def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample]) = Future.successful(EmptyDataSample)

  override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample): Future[PortCheckResult] = {
    Future.successful(PortCheckResult(expectedPortCheckResult))
  }

  override val getInputPorts: Seq[Port] = Seq(port)
}
