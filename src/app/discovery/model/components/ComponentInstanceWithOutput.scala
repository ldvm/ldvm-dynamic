package discovery.model.components

import discovery.model.{DataSample, Port, State}

import scala.concurrent.Future

trait ComponentInstanceWithOutput extends ComponentInstance {

  def getOutputDataSample(state: Option[State], dataSamples: Map[Port, DataSample]): Future[DataSample]

}
