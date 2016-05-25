package skullbeware

import org.scalajs.dom

import scala.collection.mutable

class KeyboardInput {
  val state = new mutable.HashMap[String, Boolean]

  def down(evt: dom.KeyboardEvent): Unit = {
    state(evt.key) = true
  }

  def up(evt: dom.KeyboardEvent): Unit = {
    state(evt.key) = false
  }

  def isPressed(code: String): Boolean = state.getOrElse(code, false)
}
