package discovery.model.components

trait DataSourceInstance extends ComponentInstanceWithOutput {

  def isLarge : Boolean

  def isLinkset : Boolean

}
