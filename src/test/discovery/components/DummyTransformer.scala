package discovery.components

import discovery.model.PortCheckResult.Status._
import discovery.model.components.{TransformerInstance, AnalyzerInstance}
import discovery.model.{ComponentState, DataSample, Port, PortCheckResult}

import scala.concurrent.Future

class DummyTransformer(expectedPortCheckResult: Status) extends TransformerInstance {
  val port = Port("PORT1", 1)

  override def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample]) = Future.successful(DataSample())

  override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample): Future[PortCheckResult] = {
    Future.successful(PortCheckResult(expectedPortCheckResult))
  }

  override val getInputPorts: Seq[Port] = Seq(port)
}
