package discovery.components.transformer

import discovery.model._
import discovery.model.components.TransformerInstance

import scala.concurrent.Future

class PopulationTransformer extends TransformerInstance {
  override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample): Future[PortCheckResult] = ???

  override def getInputPorts: Seq[Port] = ???

  override def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample]): Future[DataSample] = ???
}
