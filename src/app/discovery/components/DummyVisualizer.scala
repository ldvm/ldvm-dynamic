package discovery.components

import discovery.model.Status.Status
import discovery.model._
import discovery.model.components.VisualizerInstance

import scala.concurrent.Future

class DummyVisualizer(expectedResult: Status) extends VisualizerInstance {
  
  val port = Port("PORT1", priority = 0)

  override def checkPort(port: Port, state: Option[State], outputDataSample: DataSample): Future[CheckResult] = {
    Future.successful(CheckResult(expectedResult))
  }

  override def getInputPorts = Seq(port)
}
