package discovery.components

import discovery.model.PortCheckResult.Status
import discovery.model.{PortCheckResult, DataSample, ComponentState, Port}
import discovery.model.components.AnalyzerInstance

import scala.concurrent.Future

class DummyTwoPortAnalyzer extends AnalyzerInstance {
  val port1 = Port("PORT1", 1)
  val port2 = Port("PORT2", 2)

  val port1BoundState = ComponentState("port 1 bound")
  val port2BoundState = ComponentState("port 1 & 2 bound")

  override def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample]) = {
    assert(state.contains(port2BoundState))
    Future.successful(DataSample())
  }

  override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample): Future[PortCheckResult] = {
    port.name match {
      case port1.name => Future.successful(PortCheckResult(Status.Success, Option(port1BoundState)))
      case port2.name => Future.successful {
        if (state.contains(port1BoundState))
          PortCheckResult(Status.Success, Option(port2BoundState))
        else
          PortCheckResult(Status.Failure)
      }
      case _ => Future.successful(PortCheckResult(Status.Error))
    }
  }

  override val getInputPorts: Seq[Port] = Seq(port1, port2)
}
