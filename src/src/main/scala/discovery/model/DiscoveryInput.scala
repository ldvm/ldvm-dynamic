package discovery.model

import discovery.model.components.{DataSourceInstance, ProcessorInstance, VisualizerInstance}

case class DiscoveryInput(dataSources: Seq[DataSourceInstance], visualizers: Seq[VisualizerInstance], processors: Seq[ProcessorInstance])
