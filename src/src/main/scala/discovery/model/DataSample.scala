package discovery.model

import com.hp.hpl.jena.query.{QueryExecutionFactory, QueryFactory}
import com.hp.hpl.jena.rdf.model.Model
import discovery.JenaUtil
import discovery.model.components.descriptor.{AskDescriptor, ConstructDescriptor, SelectDescriptor}

import scala.concurrent.Future

trait DataSample {

  def executeAsk(query: AskDescriptor): Future[Boolean]

  def executeSelect(query: SelectDescriptor): Future[Boolean]

  def executeConstruct(query: ConstructDescriptor): Future[Boolean]

}

case class ModelDataSample(model: Model) extends DataSample {
  override def executeAsk(descriptor: AskDescriptor): Future[Boolean] = {
    Future.successful {
      val query = QueryFactory.create(descriptor.query)
      val execution = QueryExecutionFactory.create(query, model)
      execution.execAsk()
    }
  }

  override def executeConstruct(query: ConstructDescriptor): Future[Boolean] = ???

  override def executeSelect(query: SelectDescriptor): Future[Boolean] = ???
}

case class RdfDataSample(rdfData: String) extends DataSample {
  private val innerModel = ModelDataSample(JenaUtil.modelFromTtl(rdfData))

  override def executeAsk(descriptor: AskDescriptor): Future[Boolean] = {
    innerModel.executeAsk(descriptor)
  }

  override def executeSelect(query: SelectDescriptor): Future[Boolean] = ???

  override def executeConstruct(query: ConstructDescriptor): Future[Boolean] = ???
}

case object EmptyDataSample extends DataSample {
  override def executeAsk(query: AskDescriptor): Future[Boolean] = Future.successful(false)

  override def executeConstruct(query: ConstructDescriptor): Future[Boolean] = Future.successful(false)

  override def executeSelect(query: SelectDescriptor): Future[Boolean] = Future.successful(false)
}