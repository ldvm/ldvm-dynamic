package discovery.model

import discovery.model.components.{DataSourceInstance, ProcessorInstance, VisualizerInstance}

// TODO: do we need to pass data sources/visualizers/processors separately?
case class DiscoveryInput(dataSources: Seq[DataSourceInstance], visualizers: Seq[VisualizerInstance], processors: Seq[ProcessorInstance])
