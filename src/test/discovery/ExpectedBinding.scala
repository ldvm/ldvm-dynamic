package discovery

import discovery.model.components.ComponentInstance


case class ExpectedBinding(startComponent: ComponentInstance, portName: String, endComponent: ComponentInstance)