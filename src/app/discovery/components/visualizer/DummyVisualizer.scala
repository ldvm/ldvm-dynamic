package discovery.components.visualizer

import discovery.model.PortCheckResult.Status.Status
import discovery.model._
import discovery.model.components.VisualizerInstance

import scala.concurrent.Future

class DummyVisualizer(expectedResult: Status) extends VisualizerInstance {
  
  val port = Port("PORT1", priority = 0)

  override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample): Future[PortCheckResult] = {
    Future.successful(PortCheckResult(expectedResult))
  }

  override def getInputPorts = Seq(port)
}
