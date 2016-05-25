package skullbeware

import org.scalajs.dom

import scalatags.JsDom.all._

object Obstacle {
  def skull = new Obstacle(imageSrc = "skull2.png",
                            width = 28,
                            height = 40,
                            deathSound = "Gun14.m4a")
}

class Obstacle(val imageSrc: String,
               val width: Int,
               val height: Int,
               val deathSound: String,
               var x: Int = 0,
               var y: Int = 0,
               var speed: Int = 10)
    extends Renderable {

  var destroyed = false

  val image = Some(img(src := imageSrc).render)

  def moveDown(): Unit = {
    y = y + speed
  }

  def drawOn(ctx: dom.CanvasRenderingContext2D): Unit = {
    ctx.drawImage(image.get, x, y, width, height)
  }

  def isGone = destroyed || (y - height > Dodge.screenHeight)

  def destroy(): Unit = {
    MusicPlayer.playEffect(deathSound)
    Dodge.gameStats.incr("kills")
    destroyed = true
  }
}
