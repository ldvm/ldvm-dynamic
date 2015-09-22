package discovery

import com.hp.hpl.jena.rdf.model.{Model, ModelFactory}
import org.apache.commons.io.IOUtils
import resource._

object JenaUtil {

  def modelFromTtl(ttl: String): Model = {
    ModelFactory.createDefaultModel().read(IOUtils.toInputStream(ttl, "UTF-8"), null, "TTL")
  }

  def modelFromTtlFile(fileName: String): Model = {
    val source = scala.io.Source.fromFile(fileName)
    val model = ModelFactory.createDefaultModel()
    for (reader <- managed(source.reader())) {
      model.read(reader, null, "TTL")
    }
    model
  }

}
