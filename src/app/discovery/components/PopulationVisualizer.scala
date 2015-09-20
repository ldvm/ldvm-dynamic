package discovery.components

import discovery.model.components.VisualizerInstance
import discovery.model.{CheckResult, DataSample, Port, State}

import scala.concurrent.Future

class PopulationVisualizer extends VisualizerInstance {
  override def checkPort(port: Port, state: Option[State], outputDataSample: DataSample): Future[CheckResult] = ???

  override def getInputPorts: Seq[Port] = ???
}
