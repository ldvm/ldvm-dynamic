package discovery.model.internal

import discovery.model.{DiscoveryInput, Pipeline}
import discovery.model.components.ComponentInstanceWithInputs

case class IterationData(
  givenPipelines: Seq[Pipeline],
  completedPipelines: Seq[Pipeline],
  availableComponents: DiscoveryInput,
  iterationNumber: Int
)
