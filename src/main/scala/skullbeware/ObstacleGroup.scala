package skullbeware

import org.scalajs.dom

import scala.collection.mutable
import scala.util.Random

object ObstacleGroup {

  val innerSpacing = 30
  val finalSpacing = 60

  def layouts =
    List("""X...X...X""",
         """..XXXXX..""",
         """X..XXX..X""",
         """
                        |...XXX...
                        |..X...X..
                        |...XXX...""",
         """
                        |X.......X
                        |.X.....X.
                        |..X...X..
                        |...X.X...
                        |....X....
                        |...X.X...
                        |..X...X..
                        |.X.....X.
                        |X.......X""",
         """X...X...X
                        |....X....""",
         """XXXXXXXXX
                        |XX.....XX""",
         """..XX.XX..
                        |..XXXXX..
                        |..XXXXX..
                        |...XXX...
                        |....X....""",
         """..XXXXX..
                        |...XXX...
                        |.........
                        |....X....""",
         """
                        |.XXXXXXXX
                        |.XXXXXXXX
                        |.XXXXXXXX
                        |.XXXXXXXX
                        |.XXXXXXXX
                        |.XXXXXXXX""",
         """
                        |...XXX...
                        |.........
                        |...XXX...
                        |...XXX...
                        |...XXX...
                        |...XXX...
                        |.........
                        |..XX.XX..
                        |..XX.XX..
                        |..XXXXX..
                        |..XX.XX..
                        |..XX.XX..""",
         """
                        |XXXXXX...
                        |XXXXX...X
                        |XXXXXX..X""",
         """
          |XXXXXXXX.
          |XXXXXXX
          |X.XXX.X
          |X.XXX.XX
        """)

  def generate: ObstacleGroup = {
    new ObstacleGroup(Random.shuffle(layouts).head)
  }
}

class ObstacleGroup(var rawLayout: String, val cols: Int = 9) {

  val layout = parseLayout(rawLayout)
  val obstacles: mutable.Set[Obstacle] = mutable.Set()
  val blockSections = calculateBlockSections
  val buffer = new mutable.Stack[Seq[Obstacle]]().pushAll {
    layout.indices.map(generateObstacles(_))
  }

  def isMaterialized = buffer.isEmpty

  def generateObstacles(row: Int): Seq[Obstacle] = {
    for ((blk, idx) <- layout(row).zipWithIndex if blk == 1) yield {
      val obst = Obstacle.skull
      obst.speed = 2
      obst.x = blockSections(idx) + obst.width / 2
      obst.y = -obst.height
      obst
    }
  }

  def parseLayout(raw: String): Vector[Vector[Int]] = {
    val rawLines: Vector[(String, Int)] =
      raw.stripMargin.split("\n").toVector.zipWithIndex
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
      val row = for (s <- line.split("").toVector if s != "") yield {
        if (s == ".") {
          0
        } else {
          1
        }
      }
      row
    }
  }

  def emptyLines(spacing: Int): Vector[String] = {
    Vector.fill[String](spacing)("." * cols)
  }

  def calculateBlockSections: Seq[Int] = {
    for (idx <- 0 until cols) yield {
      (Dodge.screenWidth / cols) * idx
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

  def drawOn(ctx: dom.CanvasRenderingContext2D): Unit = {
    for (obstacle <- obstacles) {
      obstacle.drawOn(ctx)
    }
  }
}
