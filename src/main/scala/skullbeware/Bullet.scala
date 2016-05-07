package skullbeware

import org.scalajs.dom

object Bullet {
  val width = 4

  val height = 20

  def generate(x: Double, y: Double): Bullet = {
    new Bullet(x - width / 2, y - height)
  }
}
class Bullet(private var xCoord: Double = 0,
             private var yCoord: Double = 0,
             val step: Double = 30,
             val width: Double = Bullet.width,
             val height: Double = Bullet.height) extends Renderable {
  def x = xCoord

  def y = yCoord

  def drawOn(ctx: dom.CanvasRenderingContext2D): Unit = {
    ctx.fillStyle = "blue"
    ctx.fillRect(x, y, width, height)
  }

  def move(): Unit = {
    yCoord -= step // + (Math.random()*10)
  }

  def isGone = yCoord < 0
}
