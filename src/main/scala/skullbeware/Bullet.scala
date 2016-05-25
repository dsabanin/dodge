package skullbeware

import org.scalajs.dom

object Bullet {
  val width = 4

  val height = 20

  def generate(x: Int, y: Int): Bullet = {
    new Bullet(x - width / 2, y - height)
  }
}

class Bullet(private var xCoord: Int = 0,
             private var yCoord: Int = 0,
             val step: Int = 20,
             val width: Int = Bullet.width,
             val height: Int = Bullet.height)
    extends Renderable {

  val image = None
  var destroyed = false

  def x = xCoord

  def y = yCoord

  def drawOn(ctx: dom.CanvasRenderingContext2D): Unit = {
    ctx.fillStyle = "blue"
    ctx.fillRect(x, y, width, height)
  }

  def move(): Unit = {
    yCoord -= step // + (Math.random()*10)
  }

  def destroy(): Unit = {
    destroyed = true
  }

  def isGone = destroyed || yCoord < 0
}
