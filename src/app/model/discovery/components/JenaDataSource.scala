package model.discovery.components

import model.discovery.{DataSample, Port, State, DataSourceInstance}
import com.hp.hpl.jena.rdf.model.Model
import scala.concurrent.Future

class JenaDataSource(model: Model) extends DataSourceInstance {
  override def getOutputDataSample(state: Option[State], dataSamples: Map[Port, DataSample]): Future[DataSample] =  Future.successful(DataSample())
}
