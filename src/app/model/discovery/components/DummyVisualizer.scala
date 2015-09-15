package model.discovery.components

import model.discovery.Status.Status
import model.discovery._

import scala.concurrent.Future

class DummyVisualizer(expectedResult: Status) extends VisualizerInstance {
  
  val port = Port("PORT1", priority = 0)

  override def checkPort(port: Port, state: Option[State], outputDataSample: DataSample): Future[CheckResult] = {
    Future.successful(CheckResult(expectedResult))
  }

  override def getInputPorts = Seq(port)
}
