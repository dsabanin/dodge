package skullbeware

import org.scalajs.dom

import scalatags.JsDom.all._

object Obstacle {
  def skull =
    new Obstacle(
      imageSrc = assetPath("skull2.png"),
      width = 28,
      height = 40,
      deathSound = assetPath("Explosion2.m4a")
    )

  def kitten =
    new Obstacle(
      imageSrc = assetPath("kitten.png"),
      width = 80,
      height = 80,
      deathSound = assetPath("Explosion2.m4a"),
      rotationSpeed = 0.5
    )

  def block1 =
    new Obstacle(
      imageSrc = assetPath("block1.png"),
      width = 150,
      height = 150,
      deathSound = assetPath("Explosion2.m4a"),
      rotationSpeed = 0.5
    )
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
  var angle     = 0.0

  override lazy val image = Some(img(src := imageSrc).render)

  def moveDown(): Unit = {
    if (rotationSpeed > 0) {
      angle += rotationSpeed
      if (angle > 359) {
        angle = 0
      }
    }
    y = y + speed
  }

  def drawOn(ctx: dom.CanvasRenderingContext2D, timeDiff: Double): Unit = {
    if (Dodge.debug) {
      ctx.fillStyle = "green"
      ctx.fillRect(x, y, width, 4)
    }
    withRotation(angle, ctx) {
      ctx.drawImage(image.get, x, y, width, height)
    }
  }

  override def isEmptyAt(x0: Int, x1: Int, y0: Int, y1: Int, ctx: dom.CanvasRenderingContext2D): Boolean = {
    withRotation(angle, ctx) {
      super.isEmptyAt(x0, x1, y0, y1, ctx)
    }
  }

  def isGone = destroyed || (y - height > Dodge.screenHeight)

  def destroy(): Unit = {
    MusicPlayer.playEffect(deathSound)
    Dodge.gameStats.incr("kills")
    Dodge.explosions += new Explosion(x + middleX, y + middleY)
    destroyed = true
  }

  def withRotation[R](angle: Double, ctx: dom.CanvasRenderingContext2D)(fn: => R): R = {
    ctx.save
    val transX = x + middleX
    val transY = y + middleY
    ctx.translate(transX, transY)
    ctx.rotate(angle.toRadians)
    ctx.translate(-transX, -transY)
    val ret: R = fn
    ctx.rotate(-angle.toRadians)
    ctx.restore
    ret
  }

}
