package skullbeware

import org.scalajs.dom

import scala.collection.mutable
import scalatags.JsDom.all._

object Player {
  val width = 36

  val height = 50

  def image = img(src := "spaceship.png").render
}

class Player(private var xCoord: Double = Dodge.screenWidth / 2 - Player.width / 2,
             private var yCoord: Double = Dodge.screenHeight - 100,
             val step: Double = 10,
             val width: Double = Player.width,
             val height: Double = Player.height) extends Renderable {

  val bullets: mutable.Set[Bullet] = mutable.Set()

  def x = xCoord

  def y = yCoord

  def moveLeft(): Unit = {
    val newX = xCoord - step
    if (newX < 0) {
      xCoord = 0
    }
    else {
      xCoord = newX
    }
  }

  def moveRight(): Unit = {
    val newX = xCoord + step
    if (newX + width > Dodge.screenWidth) {
      xCoord = Dodge.screenWidth - width
    } else {
      xCoord = newX
    }
  }

  def initiateShoot() = {
    bullets += Bullet.generate(x + width/2, y)
  }

  def moveBullets() = {
    for (bullet <- bullets) {
      bullet.move()
      if (bullet.isGone) {
        bullets -= bullet
      }
    }
  }

  def drawOn(ctx: dom.CanvasRenderingContext2D): Unit = {
    ctx.drawImage(Player.image, x, y, width, height)
    for (bullet <- bullets) {
      bullet.drawOn(ctx)
    }
  }
}
