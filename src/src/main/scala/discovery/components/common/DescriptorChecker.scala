package discovery.components.common

import discovery.model.components.descriptor.{AskDescriptor, Descriptor}
import discovery.model.{DataSample, PortCheckResult}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait DescriptorChecker {
  protected def checkStatelessDescriptors(dataSample: DataSample, descriptors: Descriptor*): Future[PortCheckResult] = {

    val eventuallyDescriptorChecks = Future.sequence(descriptors.map {
      case d: AskDescriptor => dataSample.executeAsk(d)
      case some => throw new RuntimeException("Unsupported type of descriptor: " + some)
    })

    eventuallyDescriptorChecks.map { descriptorCheckResults =>
      PortCheckResult(
        descriptorCheckResults.forall(identity)
      )
    }
  }
}
