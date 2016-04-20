package discovery.model.components.descriptor

case class SelectDescriptor(query: String, override val isMandatory: Boolean = false) extends Descriptor
