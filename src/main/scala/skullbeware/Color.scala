package skullbeware

import scala.util.Random

object Color {
  def random: Color = {
    new Color(Random.nextInt(255), Random.nextInt(255), Random.nextInt(255))
  }

  def randomRed: Color = {
    new Color(Random.nextInt(155) + 100, 0, 0)
  }
}

case class Color(r: Int, g: Int, b: Int, var opacity: Double = 1.0) {

  override def toString = s"rgba($r,$g,$b,$opacity)"
}
