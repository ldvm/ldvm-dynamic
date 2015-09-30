package discovery.components

import discovery.model.PortCheckResult.Status.Status
import discovery.model._
import discovery.model.components.VisualizerInstance
import discovery.model.components.descriptor.Descriptor

import scala.concurrent.Future

class DummyVisualizer(expectedPortCheckResult: Status) extends VisualizerInstance {
  
  val port = Port("PORT1", priority = 0)

  override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample): Future[PortCheckResult] = {
    Future.successful(PortCheckResult(expectedPortCheckResult))
  }

  override def getInputPorts = Seq(port)

  override def descriptorsForPort(port: Port): Seq[Descriptor] = Seq()
}
