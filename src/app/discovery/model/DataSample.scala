package discovery.model

import java.io.ByteArrayInputStream

import com.hp.hpl.jena.query.{QueryExecutionFactory, QueryFactory}
import com.hp.hpl.jena.rdf.model.ModelFactory
import discovery.model.components.descriptor.{AskDescriptor, SelectDescriptor, ConstructDescriptor}

import scala.concurrent.Future

trait DataSample {

  def executeAsk(query: AskDescriptor): Future[Boolean]

  def executeSelect(query: SelectDescriptor): Future[Boolean]

  def executeConstruct(query: ConstructDescriptor): Future[Boolean]

}

case class RdfDataSample(rdfData: String) extends  DataSample {

  override def executeAsk(query: AskDescriptor) : Future[Boolean] = Future.successful {
    val model = ModelFactory.createDefaultModel().read(new ByteArrayInputStream(rdfData.getBytes), null, "N-TRIPLES")
    val jenaQuery = QueryFactory.create(query.query)
    val execution = QueryExecutionFactory.create(jenaQuery, model)
    execution.execAsk()
  }

  override def executeSelect(query: SelectDescriptor): Future[Boolean] = ???

  override def executeConstruct(query: ConstructDescriptor): Future[Boolean] = ???
}