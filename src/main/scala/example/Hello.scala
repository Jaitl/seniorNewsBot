package example

object Hello extends Greeting with App {
  println(greeting) // scalastyle:off
}

trait Greeting {
  lazy val greeting: String = "hello"
}
