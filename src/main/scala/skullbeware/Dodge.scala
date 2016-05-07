package skullbeware

import org.scalajs.dom
import org.scalajs.dom.html

import scala.collection.mutable
import scala.scalajs.js.annotation.JSExport
import scala.util.Random
import scalatags.JsDom.all._

@JSExport object Dodge {

  val obstacleSpeed = 50
  val obstacleGenSpeed = 70
  val screenWidth = 600
  val screenHeight = 600
  val mainCanvas = createCanvas()
  val mainCtx = canvasCtx(mainCanvas)
  val bufferCanvas = createCanvas(hide = true)
  val bufferCtx = canvasCtx(bufferCanvas)
  var player = new Player
  var obstacleGroups: mutable.Set[ObstacleGroup] = mutable.Set()
  var obstacleGroupLimit = 5

  @JSExport def main(): Unit = {
    clear(mainCtx)
    dom.document.addEventListener("keydown", keydown _)
    dom.setInterval(generateObstacleGroups _, obstacleGenSpeed)
    dom.window.requestAnimationFrame(loop _)
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

  def loop(something: Double): Unit = {
    moveObstacle()
    player.moveBullets()
    shootObstacles()
    renderLoop()
    testForGameOver()
  }

  def renderLoop(): Unit = {
    clear(bufferCtx)
    player.drawOn(bufferCtx)
    for (obstacle <- obstacleGroups) {
      obstacle.drawOn(bufferCtx)
    }
    renderBuffer()
  }

  def shootObstacles(): Unit = {
    for (bullet <- player.bullets) {
      for (touchingGrp <- obstacleGroups.filter(isTouchingGroup(_, bullet))) {
        touchingGrp.obstacles.filter(isTouching(_, bullet)).map(_.destroy)
      }
    }
  }

  def testForGameOver(): Unit = {
    if (obstacleGroups.exists(isTouchingGroup(_, player))) {
      resetGame()
    } else {
      dom.window.requestAnimationFrame(loop _)
    }
  }

  def resetGame(): Unit = {
    obstacleGroups = mutable.Set()
    player = new Player
    clear(mainCtx)
    dom.window.requestAnimationFrame(loop _)
  }

  def renderBuffer(): Unit = {
    mainCtx.drawImage(bufferCanvas, 0, 0)
  }

  def clear(ctx: dom.CanvasRenderingContext2D): Unit = {
    ctx.fillStyle = "black"
    ctx.fillRect(0, 0, mainCanvas.width, mainCanvas.height)
  }

  def isTouchingGroup(grp: ObstacleGroup, a: Renderable): Boolean = {
    grp.obstacles.exists(isTouching(_, a))
  }

  def isTouching(a: Renderable, b: Renderable): Boolean = {
    a.xs.intersect(b.xs).nonEmpty && a.ys.intersect(b.ys).nonEmpty
  }

  def generateObstacleGroups(): Unit = {
    if (obstacleGroups.size < 1) {
      obstacleGroups += ObstacleGroup.generate
    } else if(obstacleGroups.forall(_.isMaterialized) && obstacleGroups.size < obstacleGroupLimit) {
      obstacleGroups += ObstacleGroup.generate
    }
  }

  def moveObstacle(): Unit = {
    for (obstacleGrp <- obstacleGroups) {
      obstacleGrp.moveDown()
      if (obstacleGrp.isGone) {
        obstacleGroups -= obstacleGrp
      }
    }
  }

  def keydown(evt: dom.KeyboardEvent): Boolean = {
    evt.stopImmediatePropagation()
    evt.stopPropagation()
    evt.preventDefault()
    
    evt.key match {
      case "ArrowLeft" => player.moveLeft()
      case "ArrowRight" => player.moveRight()
      case " " => player.initiateShoot()
      case _ => return true
    }
    false
  }
}
