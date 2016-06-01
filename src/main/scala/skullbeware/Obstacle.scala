package skullbeware

import org.scalajs.dom

import scalatags.JsDom.all._

object Obstacle {
  def skull =
    new Obstacle(imageSrc = assetPath("skull2.png"),
                 width = 28,
                 height = 40,
                 deathSound = assetPath("Gun14.m4a"),
                 rotationSpeed = 1)

  def kitten =
    new Obstacle(imageSrc = assetPath("kitten.png"),
                 width = 80,
                 height = 80,
                 deathSound = assetPath("Gun14.m4a"),
                 rotationSpeed = 0.5)

  def block1 =
    new Obstacle(imageSrc = assetPath("block1.png"),
                 width = 150,
                 height = 150,
                 deathSound = assetPath("Gun14.m4a"),
                 rotationSpeed = 0.5)
}

class Obstacle(val imageSrc: String,
               val width: Int,
               val height: Int,
               val deathSound: String,
               var x: Int = 0,
               var y: Int = 0,
               var speed: Int = 10,
               val rotationSpeed: Double = 0)
    extends Renderable {

  var destroyed = false
  var angle = 0.0

  val image = Some(img(src := imageSrc).render)

  def moveDown(): Unit = {
    if (rotationSpeed > 0) {
      angle += rotationSpeed
      if (angle > 359) {
        angle = 0
      }
    }
    y = y + speed
  }

  def drawOn(ctx: dom.CanvasRenderingContext2D): Unit = {
    if (Dodge.debug) {
      ctx.fillStyle = "green"
      ctx.fillRect(x, y, width, 4)
    }
    withRotation(angle, ctx) {
      ctx.drawImage(image.get, x, y, width, height)
    }
  }

  override def isEmptyAt(xs: Seq[Int],
                         ys: Seq[Int],
                         ctx: dom.CanvasRenderingContext2D): Boolean = {
    withRotation(angle, ctx) {
      super.isEmptyAt(xs, ys, ctx)
    }
  }

  def isGone = destroyed || (y - height > Dodge.screenHeight)

  def destroy(): Unit = {
    MusicPlayer.playEffect(deathSound)
    Dodge.gameStats.incr("kills")
    destroyed = true
  }

  def withRotation[R](angle: Double, ctx: dom.CanvasRenderingContext2D)(
      fn: => R): R = {
    ctx.save
    val transX = x + middleX
    val transY = y + middleY
    ctx.translate(transX, transY)
    val radian = degreeToRadian(angle)
    ctx.rotate(radian)
    ctx.translate(-transX, -transY)
    val ret: R = fn
    ctx.rotate(-radian)
    ctx.restore
    ret
  }

  def degreeToRadian(degree: Double) = degree * Math.PI / 180
}
