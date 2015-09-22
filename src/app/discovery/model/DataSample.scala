package discovery.model

import discovery.model.components.descriptor.{AskDescriptor, SelectDescriptor, ConstructDescriptor}

import scala.concurrent.Future

case class DataSample() {

  def executeAsk(query: AskDescriptor): Future[Boolean] = Future.successful(false)

  def executeSelect(query: SelectDescriptor): Future[Boolean] = ???

  def executeConstruct(query: ConstructDescriptor): Future[Boolean] = ???

}
