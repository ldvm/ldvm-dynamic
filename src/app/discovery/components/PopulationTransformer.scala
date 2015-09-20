package discovery.components

import discovery.model.components.TransformerInstance
import discovery.model.{CheckResult, DataSample, Port, State}

import scala.concurrent.Future

class PopulationTransformer extends TransformerInstance {
  override def checkPort(port: Port, state: Option[State], outputDataSample: DataSample): Future[CheckResult] = ???

  override def getInputPorts: Seq[Port] = ???

  override def getOutputDataSample(state: Option[State], dataSamples: Map[Port, DataSample]): Future[DataSample] = ???
}
