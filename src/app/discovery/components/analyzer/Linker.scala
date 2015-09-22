package discovery.components.analyzer

import discovery.model._
import discovery.model.components.AnalyzerInstance

import scala.concurrent.Future

class Linker extends AnalyzerInstance{
  override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample): Future[PortCheckResult] = ???

  override def getInputPorts: Seq[Port] = ???

  override def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample]): Future[DataSample] = ???
}
