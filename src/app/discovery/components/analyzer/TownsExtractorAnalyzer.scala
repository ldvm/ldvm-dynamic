package discovery.components.analyzer

import discovery.model.{PortCheckResult, DataSample, ComponentState, Port}
import discovery.model.components.AnalyzerInstance
import discovery.model.components.descriptor.Descriptor

import scala.concurrent.Future

class TownsExtractorAnalyzer extends AnalyzerInstance {
  override def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample]): Future[DataSample] = ???

  override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample): Future[PortCheckResult] = ???

  override def getInputPorts: Seq[Port] = ???

  override def getDescriptorsByPort: Map[Port, Seq[Descriptor]] = ???
}
