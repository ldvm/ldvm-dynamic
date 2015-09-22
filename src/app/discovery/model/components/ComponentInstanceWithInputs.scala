package discovery.model.components

import discovery.model._
import discovery.model.components.descriptor.{AskDescriptor, Descriptor}

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._


trait ComponentInstanceWithInputs extends ComponentInstance {

  def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample): Future[PortCheckResult]

  def getInputPorts : Seq[Port]

  def getDescriptorsByPort : Map[Port, Seq[Descriptor]]

  def checkAskDescriptors(port: Port, dataSample: DataSample) : Future[PortCheckResult] = {

    val mandatoryDescriptors = getDescriptorsByPort(port).filter(_.isMandatory)

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
