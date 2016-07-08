package skullbeware

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.html

import scala.collection.mutable
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scalatags.JsDom
import scalatags.JsDom.all._

@JSExport
object Dodge extends js.JSApp {

  val debug            = false // true
  val neverDie         = false // true
  val obstacleSpeed    = 30
  val obstacleGenSpeed = 70
  val screenWidth      = 600
  val screenHeight     = 600
  var obstacleGroupLimit = 5

  val mainCanvas   = createCanvas()
  val mainCtx      = canvasCtx(mainCanvas)
  val collisionCtx = canvasCtx(createCanvas(hide = true))
  val bufferCanvas = createCanvas(hide = true)
  val bufferCtx    = canvasCtx(bufferCanvas)
  var player = new Player
  val obstacleGroups: mutable.Set[ObstacleGroup] = mutable.Set()
  val explosions: mutable.Set[Explosion]         = mutable.Set()
  val kbd                                        = new KeyboardInput
  val gameStats                                  = new Stats
  var level: Option[List[String]]        = None
  var overlays: mutable.Set[TextOverlay] = mutable.Set()
  var lastRenderTime: Double             = 0
  var gameOver                           = false
  var gameOverAt: Option[Double]         = None
  val afterDeathPeriod: Double = 2000

  @JSExport
  def main(): Unit = {
    dom.document.addEventListener("keydown", kbd.down _, useCapture = false)
    dom.document.addEventListener("keyup", kbd.up _, useCapture = false)
    loadLevel("level1.dat") {
      dom.setInterval(generateObstacleGroups _, obstacleGenSpeed)
    }
    dom.window.requestAnimationFrame(loop _)
    MusicPlayer.play()
    overlays += new TextOverlay("Let the Dodging begin!")
  }

  def loadLevel(name: String)(whenDone: => Unit) = {
    Ajax.get(dataPath(name), responseType = "text").onComplete { xhr =>
      level = Option(Option(xhr.get.response).get.toString.split("""\n\n""").toList)
      whenDone
    }
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

  def canvasCtx(canvas: html.Canvas) =
    canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

  def loop(timer: Double): Unit = {
    val timeDiff = timer - lastRenderTime

    if (!gameOver) {
      if (testForGameOver(timer)) {
        overlays += new TextOverlay("Game over!", 0.05)
        dom.setTimeout(resetGame _, afterDeathPeriod + 100)
      }
      player.update(kbd)
    }

    if (!kbd.isPressed("p")) {
      moveObstacle()
    }

    player.moveBullets()
    shootObstacles()
    explosions.filterNot(_.hasEmitted).foreach(_.emit())

    renderLoop(timeDiff)
    renderStats()

    val haltRendering = gameOver && gameOverAt.exists(timer - _ > afterDeathPeriod)
    if (!haltRendering) {
      lastRenderTime = timer
      dom.window.requestAnimationFrame(loop _)
    }
  }

  def renderStats(): Unit = {
    dom.document.getElementById("stats").innerHTML = ""
    var attrs = Vector[JsDom.TypedTag[html.LI]]()
    gameStats.foreach { (pair) =>
      val (name, v) = pair
      val tag       = li(strong(name.capitalize + ": "), v)
      attrs = attrs.:+(tag)
    }
    val st = ul(attrs).render
    dom.document.getElementById("stats").appendChild(st)
  }

  def renderLoop(timeDiff: Double): Unit = {
    clear(bufferCtx)
    obstacleGroups.foreach(_.drawOn(bufferCtx, timeDiff))
    player.drawOn(bufferCtx, timeDiff)
    for (overlay <- overlays) {
      if (overlay.isGone) {
        overlays -= overlay
      } else {
        overlay.drawOn(bufferCtx, timeDiff)
      }
    }
    explosions.foreach(_.drawOn(bufferCtx, timeDiff))
    renderBuffer()
  }

  def shootObstacles(): Unit = {
    for (bullet <- player.bullets;
         group  <- obstacleGroups if isTouchingGroup(group, bullet)) {
      bullet.destroy()
      for (obstacle <- group.obstacles if obstacle.isTouching(bullet)) {
        obstacle.destroy()
      }
    }
  }

  def testForGameOver(timer: Double): Boolean = {
    if (!neverDie) {
      for (group <- obstacleGroups if isTouchingGroup(group, player)) {
        gameOver = true
        gameOverAt = Some(timer)
        for (obstacle <- group.obstacles if obstacle.isTouching(player)) {
          obstacle.destroy()
        }
        player.destroy()
        return true
      }
    }
    false
  }

  def resetGame(): Unit = {
    lastRenderTime = 0
    gameOver = false
    gameOverAt = None
    obstacleGroups.clear()
    explosions.clear()
    player = new Player
    gameStats.reset()
    MusicPlayer.reset()
    clear(mainCtx)
    dom.window.requestAnimationFrame(loop _)
  }

  def renderBuffer(): Unit = {
    mainCtx.drawImage(bufferCanvas, 0, 0)
  }

  def clear(ctx: dom.CanvasRenderingContext2D, fillStyle: String = "black"): Unit = {
    ctx.fillStyle = fillStyle
    ctx.fillRect(0, 0, mainCanvas.width, mainCanvas.height)
  }

  def isTouchingGroup(grp: ObstacleGroup, a: Renderable): Boolean = {
    grp.obstacles.exists(_.isTouching(a))
  }

  def generateObstacleGroups(): Unit = {
    if (obstacleGroups.size < 1) {
      obstacleGroups += ObstacleGroup.generate
    } else if (obstacleGroups.forall(_.isMaterialized) &&
               obstacleGroups.size < obstacleGroupLimit) {
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
}
