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

  val debug = false // true
  val neverDie = false // true
  val obstacleSpeed = 30
  val obstacleGenSpeed = 70
  val screenWidth = 600
  val screenHeight = 600
  var obstacleGroupLimit = 5

  val mainCanvas = createCanvas()
  val mainCtx = canvasCtx(mainCanvas)
  val collisionCtx = canvasCtx(createCanvas(hide = true))
  val bufferCanvas = createCanvas(hide = true)
  val bufferCtx = canvasCtx(bufferCanvas)
  var player = new Player
  var obstacleGroups: mutable.Set[ObstacleGroup] = mutable.Set()
  val kbd = new KeyboardInput
  val gameStats = new Stats
  var level: Option[List[String]] = None
  var overlays: mutable.Set[TextOverlay] = mutable.Set()

  @JSExport
  def main(): Unit = {
    dom.document.addEventListener("keydown", kbd.down _, false)
    dom.document.addEventListener("keyup", kbd.up _, false)
    loadLevel("level1.dat") {
      dom.setInterval(generateObstacleGroups _, obstacleGenSpeed)
    }
    dom.window.requestAnimationFrame(loop _)
    MusicPlayer.play()
    overlays += new TextOverlay("Let the Dodging begin!")
  }

  def loadLevel(name: String)(whenDone: => Unit) = {
    val future = Ajax.get(dataPath(name), responseType = "text").onComplete { xhr =>
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
    player.update(kbd)
    if(!kbd.isPressed("p")) {
      moveObstacle()
    }
    player.moveBullets()
    shootObstacles()
    val gameOver = testForGameOver()
    if(gameOver) {
      overlays += new TextOverlay("Game over!", 0.05)
      dom.setTimeout(resetGame _, 700)
    }
    renderLoop()
    renderStats()
    if (!testForGameOver()) {
      dom.window.requestAnimationFrame(loop _)
    }
  }

  def renderStats(): Unit = {
    dom.document.getElementById("stats").innerHTML = ""
    var attrs = Vector[JsDom.TypedTag[html.LI]]()
    gameStats.foreach { (pair) =>
      val (name, v) = pair
      val tag = li(strong(name.capitalize + ": "), v)
      attrs = attrs.:+(tag)
    }
    val st = ul(attrs).render
    dom.document.getElementById("stats").appendChild(st)
  }

  def renderLoop(): Unit = {
    clear(bufferCtx)
    for (obstacle <- obstacleGroups) {
      obstacle.drawOn(bufferCtx)
    }
    player.drawOn(bufferCtx)
    for (overlay <- overlays) {
      if(overlay.isGone) {
        overlays -= overlay
      } else {
        overlay.drawOn(bufferCtx)
      }
    }
    renderBuffer()
  }

  def shootObstacles(): Unit = {
    for (bullet <- player.bullets) {
      for (touchingGrp <- obstacleGroups.filter(isTouchingGroup(_, bullet))) {
        touchingGrp.obstacles.filter(isTouching(_, bullet)).map(_.destroy())
        bullet.destroy()
      }
    }
  }

  def testForGameOver(): Boolean = {
    !neverDie && obstacleGroups.exists(isTouchingGroup(_, player))
  }

  def resetGame(): Unit = {
    obstacleGroups = mutable.Set()
    player = new Player
    gameStats.reset()
    MusicPlayer.reset()
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
    val xInter = a.xs.intersect(b.xs)
    if (xInter.nonEmpty) {
      val yInter = a.ys.intersect(b.ys)
      if (yInter.nonEmpty) {
        if (!(a.isEmptyAt(xInter, yInter, Dodge.collisionCtx) ||
                b.isEmptyAt(xInter, yInter, Dodge.collisionCtx))) {
          return true
        }
      }
    }
    false
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
