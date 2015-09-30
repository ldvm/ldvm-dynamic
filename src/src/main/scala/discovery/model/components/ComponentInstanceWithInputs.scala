package discovery.model.components

import discovery.model._
import discovery.model.components.descriptor.{AskDescriptor, Descriptor}

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global


trait ComponentInstanceWithInputs extends ComponentInstance {

  def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample): Future[PortCheckResult]

  def getInputPorts : Seq[Port]

  def descriptorsForPort(port: Port): Seq[Descriptor]

  def checkAskDescriptors(port: Port, dataSample: DataSample) : Future[PortCheckResult] = {

    val mandatoryDescriptors =  descriptorsForPort(port).filter(_.isMandatory)

    val eventuallyDescriptorChecks = Future.sequence(mandatoryDescriptors.map {
      case d: AskDescriptor => dataSample.executeAsk(d)
      case _ => Future.successful(false)
    })

    eventuallyDescriptorChecks.map { descriptorChecks =>
      PortCheckResult(
        descriptorChecks.forall(a => a)
      )
    }
  }

}
