package skullbeware

import org.scalajs.dom

import scala.collection.mutable
import scala.util.Random

object ObstacleGroup {

  val innerSpacing = 60
  val finalSpacing = 150

  def generate: ObstacleGroup = {
    new ObstacleGroup(Random.shuffle(Dodge.level.get).head)
  }
}

class ObstacleGroup(var rawLayout: String, val cols: Int = 9) {

  val layout                           = parseLayout(rawLayout)
  val obstacles: mutable.Set[Obstacle] = mutable.Set()
  val blockWidth                       = Dodge.screenWidth / cols
  val blockSections                    = calculateBlockSections
  val buffer = new mutable.Stack[Seq[Obstacle]]().pushAll {
    layout.indices.map(generateObstacles(_))
  }

  def isMaterialized = buffer.isEmpty

  def generateObstacles(row: Int): Seq[Obstacle] = {
    val obstacles = for ((blk, idx) <- layout(row).zipWithIndex if blk != ".")
      yield {
        val obst = blk match {
          case "X" => Obstacle.skull
          case "K" => Obstacle.kitten
          case "B" => Obstacle.block1
        }
        (obst, idx)
      }
    if (obstacles.isEmpty) {
      return Seq()
    }
    val maxHeight = obstacles.map(_._1.height).max
    for ((obst, idx) <- obstacles) yield {
      obst.speed = 2
      obst.x = blockSections(idx) + blockWidth / 2 - obst.middleX
      obst.y = -maxHeight
      obst
    }
  }

  def parseLayout(raw: String): Vector[Vector[String]] = {
    val rawLines: Vector[(String, Int)] = raw.stripMargin.split("\n").toVector.zipWithIndex

    val layoutLines = for ((line, idx) <- rawLines) yield {
      val re = """^\s*(\d+)\s*$""".r

      line match {
        case re(num) => Vector.fill[String](num.toInt)("." * cols)
        case _ =>
          if (idx == rawLines.size - 1) {
            Vector(line.trim)
          } else {
            Vector(line.trim) ++ emptyLines(ObstacleGroup.innerSpacing)
          }
      }
    }

    val lines = layoutLines ++ Vector(emptyLines(ObstacleGroup.finalSpacing))
    for (line <- lines.flatten) yield {
      for (s  <- line.split("").toVector if s != "") yield s
    }
  }

  def emptyLines(spacing: Int): Vector[String] = {
    Vector.fill[String](spacing)("." * cols)
  }

  def calculateBlockSections: Seq[Int] = {
    for (idx <- 0 until cols) yield {
      blockWidth * idx
    }
  }

  def moveDown(): Unit = {
    obstacles.foreach(_.moveDown())
    if (buffer.nonEmpty) {
      obstacles ++= buffer.pop
    }
    for (obstacle <- obstacles if obstacle.isGone) {
      if (!obstacle.destroyed) {
        Dodge.gameStats.incr("dodged")
      }
      obstacles -= obstacle
    }
  }

  def isGone: Boolean = buffer.isEmpty && obstacles.forall(_.isGone)

  def drawOn(ctx: dom.CanvasRenderingContext2D, timeDiff: Double): Unit = {
    if (Dodge.debug) {
      for (section <- calculateBlockSections) {
        ctx.fillStyle = "red"
        ctx.fillRect(section, 0, 1, 1000)
      }
    }
    for (obstacle <- obstacles) {
      obstacle.drawOn(ctx, timeDiff)
    }
  }
}
