package model.discovery.components

import model.discovery._

import scala.concurrent.Future

class PopulationTransformer extends TransformerInstance {
  override def checkPort(port: Port, state: Option[State], outputDataSample: DataSample): Future[CheckResult] = ???

  override def getInputPorts: Seq[Port] = ???

  override def getOutputDataSample(state: Option[State], dataSamples: Map[Port, DataSample]): Future[DataSample] = ???
}
