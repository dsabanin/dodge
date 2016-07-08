package skullbeware

import org.scalajs.dom

import scala.collection.mutable
import scalatags.JsDom.all._

object Player {
  val width = 30

  val height = 50

  def image = img(src := assetPath("spaceship2.png")).render
}

class Player(private var xCoord: Int = Dodge.screenWidth / 2 - Player.width / 2,
             private var yCoord: Int = Dodge.screenHeight - 100,
             val speed: Int = 5,
             val width: Int = Player.width,
             val height: Int = Player.height)
    extends Renderable {

  override val image               = Some(Player.image)
  val shootingDelay                = 200
  val bullets: mutable.Set[Bullet] = mutable.Set()
  val deathSound                   = assetPath("Explosion1.m4a")
  var lastShooting: Long = shootingDelay * 2
  var isVisible            = true

  def x = xCoord

  def y = yCoord

  def moveLeft(): Unit = {
    val newX = xCoord - speed
    if (newX < 0) {
      xCoord = 0
    } else {
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

  def moveUp(): Unit = {
    val newY = yCoord - speed
    if (newY < 0) {
      yCoord = 0
    } else {
      yCoord = newY
    }
  }

  def moveDown(): Unit = {
    val newY = yCoord + speed
    if (newY + height > Dodge.screenHeight) {
      yCoord = Dodge.screenHeight - height
    } else {
      yCoord = newY
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

  def drawOn(ctx: dom.CanvasRenderingContext2D, timeDiff: Double): Unit = {
    if(!isVisible)
      return
    ctx.drawImage(Player.image, x, y, width, height)
    for (bullet <- bullets) {
      bullet.drawOn(ctx, timeDiff)
    }
  }

  def destroy(): Unit = {
    MusicPlayer.playEffect(deathSound)
    Dodge.explosions += new Explosion(x + middleX, y + middleY, numParticles = 200)
    isVisible = false
  }

  def update(kbd: KeyboardInput): Unit = {
    if (kbd.isPressed("ArrowLeft")) {
      moveLeft()
    }
    if (kbd.isPressed("ArrowRight")) {
      moveRight()
    }
    if (kbd.isPressed("ArrowUp")) {
      moveUp()
    }
    if (kbd.isPressed("ArrowDown")) {
      moveDown()
    }
    if (kbd.isPressed(" ")) {
      if (System.currentTimeMillis() - lastShooting > shootingDelay) {
        initiateShoot()
        lastShooting = System.currentTimeMillis()
      }
    }
  }
}
