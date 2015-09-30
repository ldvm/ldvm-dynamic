package discovery.model.components.descriptor

case class AskDescriptor(query: String, isMandatory: Boolean = true) extends Descriptor