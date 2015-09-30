package discovery.components.datasource

import com.hp.hpl.jena.rdf.model.Model
import discovery.model.components.DataSourceInstance
import discovery.model.{RdfDataSample, ComponentState, DataSample, Port}

import scala.concurrent.Future

class JenaDataSource(model: Model) extends DataSourceInstance {
  override def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample]): Future[DataSample] = {
    Future.successful(RdfDataSample(""))
  }
}
