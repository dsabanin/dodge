package skullbeware

import org.scalajs.dom

import scala.collection.mutable

class KeyboardInput {
  val state = new mutable.HashMap[String, Boolean]
  val lastPressed = new mutable.HashMap[String, Long]

  def down(evt: dom.KeyboardEvent): Unit = {
    lastPressed(evt.key) = System.currentTimeMillis()
    state(evt.key) = true
  }

  def up(evt: dom.KeyboardEvent): Unit = {
    state(evt.key) = false
  }

  def isPressed(code: String): Boolean = state.getOrElse(code, false)

  def pressedInterval(code: String): Option[Long] = {
    if (lastPressed.contains(code)) {
      Some(System.currentTimeMillis() - lastPressed(code))
    } else {
      None
    }
  }
}
