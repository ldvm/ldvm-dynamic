package discovery.components

import discovery.model._
import discovery.model.components.VisualizerInstance

import scala.concurrent.Future

class PopulationVisualizer extends VisualizerInstance {
  override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample): Future[PortCheckResult] = ???

  override def getInputPorts: Seq[Port] = ???
}
