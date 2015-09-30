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
    val path = getClass.getResource(fileName).getPath
    val source = scala.io.Source.fromFile(path)
    val model = ModelFactory.createDefaultModel()
    for (reader <- managed(source.reader())) {
      // scalastyle:off null
      model.read(reader, null, "TTL")
      // scalastyle:on null
    }
    model
  }

}
