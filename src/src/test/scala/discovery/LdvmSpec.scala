package discovery

import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{FlatSpec, Matchers}

trait LdvmSpec extends FlatSpec with Matchers with PipelineAsserts {
  implicit val patienceConfig = PatienceConfig(timeout = Span(1000, Seconds))
}