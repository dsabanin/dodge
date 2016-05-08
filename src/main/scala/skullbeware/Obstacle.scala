package skullbeware

import org.scalajs.dom

import scala.collection.mutable
import scala.util.Random
import scalatags.JsDom.all._

object Obstacle {
  def width = 40

  def height = 40

  def generate(speed: Int,
               x: Option[Int] = None): Obstacle = {
    val realX: Int = x.getOrElse {
      Random.shuffle(0 to (Dodge.screenWidth - Obstacle.width)).head
    }
    new Obstacle(realX, -Obstacle.height, speed)
  }

  def image = img(src := "skull.jpg").render
}

class Obstacle(private var xCoord: Int = 0,
               private var yCoord: Int = 0,
               val speed: Int = 10,
               val width: Int = Obstacle.width,
               val height: Int = Obstacle.height) extends Renderable {

  var destroyed = false

  def x = xCoord

  def y = yCoord

  def moveDown(): Unit = {
    yCoord = yCoord + speed
  }

  def drawOn(ctx: dom.CanvasRenderingContext2D): Unit = {
    ctx.drawImage(Obstacle.image, x, y, width, height)
  }

  def isGone = destroyed || (yCoord - height > Dodge.screenHeight)

  def destroy(): Unit = {
    destroyed = true
  }
}
