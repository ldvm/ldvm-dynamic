package discovery.model

import com.hp.hpl.jena.query.{QueryExecutionFactory, QueryFactory}
import discovery.JenaUtil
import discovery.model.components.descriptor.{AskDescriptor, ConstructDescriptor, SelectDescriptor}

import scala.concurrent.Future

trait DataSample {

  def executeAsk(query: AskDescriptor): Future[Boolean]

  def executeSelect(query: SelectDescriptor): Future[Boolean]

  def executeConstruct(query: ConstructDescriptor): Future[Boolean]

}

case object EmptyDataSample extends DataSample{
  override def executeAsk(query: AskDescriptor): Future[Boolean] = Future.successful(false)

  override def executeConstruct(query: ConstructDescriptor): Future[Boolean] = Future.successful(false)

  override def executeSelect(query: SelectDescriptor): Future[Boolean] = Future.successful(false)
}

case class RdfDataSample(rdfData: String) extends  DataSample {
  override def executeAsk(descriptor: AskDescriptor) : Future[Boolean] = Future.successful {
    val model = JenaUtil.modelFromTtl(rdfData)
    val jenaQuery = QueryFactory.create(descriptor.query)
    val execution = QueryExecutionFactory.create(jenaQuery, model)
    execution.execAsk()
  }

  override def executeSelect(query: SelectDescriptor): Future[Boolean] = ???

  override def executeConstruct(query: ConstructDescriptor): Future[Boolean] = ???
}