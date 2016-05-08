package skullbeware

import org.scalajs.dom

import scala.collection.mutable
import scalatags.JsDom.all._

object Player {
  val width = 36

  val height = 50

  def image = img(src := "spaceship.png").render
}

class Player(private var xCoord: Int = Dodge.screenWidth / 2 - Player.width / 2,
             private var yCoord: Int = Dodge.screenHeight - 100,
             val speed: Int = 5,
             val width: Int = Player.width,
             val height: Int = Player.height) extends Renderable {

  val shootingDelay = 200
  val bullets: mutable.Set[Bullet] = mutable.Set()
  var lastShooting: Long = shootingDelay * 2

  def x = xCoord

  def y = yCoord

  def moveLeft(): Unit = {
    val newX = xCoord - speed
    if (newX < 0) {
      xCoord = 0
    }
    else {
      xCoord = newX
    }
  }

  def moveRight(): Unit = {
    val newX = xCoord + speed
    if (newX + width > Dodge.screenWidth) {
      xCoord = Dodge.screenWidth - width
    } else {
      xCoord = newX
    }
  }

  def initiateShoot() = {
    bullets += Bullet.generate(x + width / 2, y)
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

  def update(kbd: KeyboardInput): Unit = {
    if (kbd.isPressed("ArrowLeft")) {
      moveLeft()
    }
    if (kbd.isPressed("ArrowRight")) {
      moveRight()
    }
    if (kbd.isPressed(" ")) {
      if (System.currentTimeMillis() - lastShooting > shootingDelay) {
        initiateShoot()
        lastShooting = System.currentTimeMillis()
      }
    }
  }
}
