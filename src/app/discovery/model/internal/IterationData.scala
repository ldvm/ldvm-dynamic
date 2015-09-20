package discovery.model.internal

import discovery.model.Pipeline
import discovery.model.components.ComponentInstanceWithInputs

case class IterationData(givenPipelines: Seq[Pipeline], completedPipelines: Seq[Pipeline], possibleComponents: Seq[ComponentInstanceWithInputs])
