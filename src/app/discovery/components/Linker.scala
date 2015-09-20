package discovery.components

import discovery.model.components.AnalyzerInstance
import discovery.model.{CheckResult, DataSample, Port, State}

import scala.concurrent.Future

class Linker extends AnalyzerInstance{
  override def checkPort(port: Port, state: Option[State], outputDataSample: DataSample): Future[CheckResult] = ???

  override def getInputPorts: Seq[Port] = ???

  override def getOutputDataSample(state: Option[State], dataSamples: Map[Port, DataSample]): Future[DataSample] = ???
}
