package skullbeware

import org.scalajs.dom
import org.scalajs.dom.html

import scala.collection.mutable
import scala.scalajs.js.annotation.JSExport
import scala.util.Random
import scalatags.JsDom.all._

trait Renderable {
  def x: Double
  def y: Double
  def width: Double
  def height: Double
  def drawOn(ctx: dom.CanvasRenderingContext2D): Unit
  def xs = x.to(x + width).by(1)
  def ys = y.to(y + height).by(1)
}

object Player {
  def width = 36
  def height = 50
  def image = img(src:= "spaceship.png").render
}

class Player(private var xCoord: Double = Dodge.screenWidth / 2 - Player.width/2,
             private val yCoord: Double = Dodge.screenHeight - 100,
             val step: Double = 5,
             val width: Double = Player.width,
             val height: Double = Player.height) extends Renderable {

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

  def drawOn(ctx: dom.CanvasRenderingContext2D): Unit = {
    ctx.drawImage(Player.image, x, y, width, height)
  }
}

object Obstacle {
  def width = 40
  def height = 40

  def generate(step: Double): Obstacle = {
    val randomX = Random.shuffle(0 to (Dodge.screenWidth - Obstacle.width)).head
    new Obstacle(randomX, 0, step)
  }

  def image = img(src:= "skull.jpg").render
}
class Obstacle(private var xCoord: Double = 0,
               private var yCoord: Double = 0,
               val step: Double = 10,
               val width: Double = Obstacle.width,
               val height: Double = Obstacle.height) extends Renderable {

  def x = xCoord

  def y = yCoord

  def moveDown(): Unit = {
    yCoord = yCoord + step
  }

  def drawOn(ctx: dom.CanvasRenderingContext2D): Unit = {
    ctx.drawImage(Obstacle.image, x, y, width, height)
  }

  def isGone = yCoord + height > Dodge.screenHeight
}

@JSExport object Dodge {

  val loopSpeed = 25
  val obstacleSpeed = 50
  val obstacleGenSpeed = 70
  val screenWidth = 600
  val screenHeight = 600
  val mainCanvas = createCanvas()
  val mainCtx = canvasCtx(mainCanvas)
  val bufferCanvas = createCanvas(hide = true)
  val bufferCtx = canvasCtx(bufferCanvas)
  var player = new Player
  var obstacles: mutable.Set[Obstacle] = mutable.Set()
  var obstacleLimit = 3

  @JSExport def main(): Unit = {
    clear(mainCtx)
    dom.addEventListener("keypress", keypress _)
    dom.setInterval(moveObstacle _, obstacleSpeed)
    dom.setInterval(generateObstacles _, obstacleGenSpeed)
    loop()
  }

  def createCanvas(hide: Boolean = false): html.Canvas = {
    val cv = canvas(style := "display:block").render
    cv.width = screenWidth
    cv.height = screenHeight
    if (hide) {
      cv.style.display = "none"
    }
    dom.document.getElementById("game").appendChild(cv)
    cv
  }

  def canvasCtx(canvas: html.Canvas) = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

  def loop(): Unit = {
    clear(bufferCtx)
    player.drawOn(bufferCtx)
    for(obstacle <- obstacles) {
      obstacle.drawOn(bufferCtx)
    }
    renderBuffer()

    if (obstacles.exists(isTouching(_, player))) {
      dom.alert("YOU LOSE! :-(")
      resetGame()
    } else {
      dom.setTimeout(loop _, loopSpeed)
    }
  }

  def resetGame(): Unit = {
    obstacles = mutable.Set()
    player = new Player
    clear(mainCtx)
    loop()
  }

  def renderBuffer(): Unit = {
    mainCtx.drawImage(bufferCanvas, 0, 0)
  }

  def clear(ctx: dom.CanvasRenderingContext2D): Unit = {
    ctx.fillStyle = "black"
    ctx.fillRect(0, 0, mainCanvas.width, mainCanvas.height)
  }

  def isTouching(a: Renderable, b: Renderable) = {
    a.xs.intersect(b.xs).nonEmpty && a.ys.intersect(b.ys).nonEmpty
  }

  def generateObstacles(): Unit = {
    if (obstacles.size < obstacleLimit) {
      val num = Random.shuffle(1 to (obstacleLimit - obstacles.size)).head
      (0 to num).foreach { i =>
        var obstacle: Option[Obstacle] = None
        do {
          obstacle = Some(Obstacle.generate(Random.shuffle(5 to 10).head))
        } while (obstacles.exists(isTouching(_, obstacle.get)))
        obstacles += obstacle.get
      }
    }
  }

  def moveObstacle(): Unit = {
    for (obstacle <- obstacles) {
      obstacle.moveDown()
      if(obstacle.isGone) {
        obstacles -= obstacle
      }
    }
  }

  def keypress(evt: dom.KeyboardEvent): Boolean = {
    val x = evt.key match {
      case "ArrowLeft" => player.moveLeft()
      case "ArrowRight" => player.moveRight()
      case _ => return true
    }
    evt.stopImmediatePropagation()
    evt.stopPropagation()
    evt.preventDefault()
    false
  }
}
