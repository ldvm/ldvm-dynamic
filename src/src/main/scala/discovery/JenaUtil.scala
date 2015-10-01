package discovery

import com.hp.hpl.jena.rdf.model.{Model, ModelFactory}
import org.apache.commons.io.IOUtils
import resource._

object JenaUtil {

  def modelFromTtl(ttl: String): Model = {
    // scalastyle:off null
    ModelFactory.createDefaultModel().read(IOUtils.toInputStream(ttl, "UTF-8"), null, "TTL")
    // scalastyle:on null
  }

  def modelFromTtlFile(fileName: String): Model = {
    val model = ModelFactory.createDefaultModel()
    for (
      source <- managed(scala.io.Source.fromURL(getClass.getResource(fileName)));
      reader <- managed(source.reader())
    ) {
      // scalastyle:off null
      model.read(reader, null, "TTL")
      // scalastyle:on null
    }
    model
  }

}
