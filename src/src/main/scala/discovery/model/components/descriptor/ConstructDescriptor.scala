package discovery.model.components.descriptor

case class ConstructDescriptor(query: String, override val isMandatory: Boolean = true) extends Descriptor

object ConstructDescriptor {
  def getAll = ConstructDescriptor("CONSTRUCT { ?s ?p ?o . } WHERE { ?s ?p ?o . }")
}