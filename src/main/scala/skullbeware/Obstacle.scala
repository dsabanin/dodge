package skullbeware

import org.scalajs.dom

import scala.collection.mutable
import scala.util.Random
import scalatags.JsDom.all._

object Obstacle {
  def width = 40

  def height = 40

  def generate(speed: Double,
               x: Option[Double] = None): Obstacle = {
    val realX: Double = x.getOrElse {
      Random.shuffle(0 to (Dodge.screenWidth - Obstacle.width)).head
    }
    new Obstacle(realX, 0, speed)
  }

  def image = img(src := "skull.jpg").render
}

class Obstacle(private var xCoord: Double = 0,
               private var yCoord: Double = 0,
               val speed: Double = 10,
               val width: Double = Obstacle.width,
               val height: Double = Obstacle.height) extends Renderable {

  var destroyed = false

  def x = xCoord

  def y = yCoord

  def moveDown(): Unit = {
    yCoord = yCoord + speed
  }

  def drawOn(ctx: dom.CanvasRenderingContext2D): Unit = {
    ctx.drawImage(Obstacle.image, x, y, width, height)
  }

  def isGone = destroyed || (yCoord + height > Dodge.screenHeight)

  def destroy(): Unit = {
    destroyed = true
  }
}
