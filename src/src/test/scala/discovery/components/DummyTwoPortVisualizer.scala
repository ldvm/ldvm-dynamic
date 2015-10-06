package discovery.components

import discovery.model.PortCheckResult.Status
import discovery.model.components.VisualizerInstance
import discovery.model.components.descriptor.Descriptor
import discovery.model.{ComponentState, DataSample, Port, PortCheckResult}

import scala.concurrent.Future

class DummyTwoPortVisualizer extends VisualizerInstance {
  val port1 = Port("PORT1", 1)
  val port2 = Port("PORT2", 2)

  override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample): Future[PortCheckResult] = {
    Future.successful(PortCheckResult(Status.Success))
  }

  override val getInputPorts = Seq(port1, port2)
}