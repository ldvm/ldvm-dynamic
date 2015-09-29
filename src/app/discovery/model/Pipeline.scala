package discovery.model

import discovery.model.components.VisualizerInstance

// TODO: do we need components at all? We can always infer them from bindings
sealed trait Pipeline {
  def components: Seq[PipelineComponent]

  def bindings: Seq[PortBinding]

  def isComplete: Boolean

  def lastComponent: PipelineComponent
}

case class CompletePipeline(components: Seq[PipelineComponent], bindings: Seq[PortBinding], lastComponent: PipelineComponent) extends Pipeline {
  assert(lastComponent.componentInstance.isInstanceOf[VisualizerInstance])
  override val isComplete: Boolean = true
}

case class PartialPipeline(components: Seq[PipelineComponent], bindings: Seq[PortBinding], lastComponent: PipelineComponent, lastOutputDataSample: DataSample) extends Pipeline {
  override val isComplete: Boolean = false
}