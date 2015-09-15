package model.discovery.components

import model.discovery._

import scala.concurrent.Future

class PopulationVisualizer extends VisualizerInstance {
  override def checkPort(port: Port, state: Option[State], outputDataSample: DataSample): Future[CheckResult] = ???

  override def getInputPorts: Seq[Port] = ???
}
