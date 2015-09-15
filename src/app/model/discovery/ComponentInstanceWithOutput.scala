package model.discovery

import scala.concurrent.Future

trait ComponentInstanceWithOutput extends ComponentInstance {

  def getOutputDataSample(state: Option[State], dataSamples: Map[Port, DataSample]): Future[DataSample]

}
