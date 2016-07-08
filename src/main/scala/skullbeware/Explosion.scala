package skullbeware

import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D

import scala.collection.mutable
import scala.scalajs.js.annotation.JSExport
import scalatags.JsDom.all._
import scala.util.Random

object Particle {

  def generate(x: Int, y: Int, lifeTime: Int): Particle = {
    val acceleration = Random.nextGaussian() * 0.007 + 0.02
    val angle = Random.nextInt(360)
    val gausX = Random.nextGaussian() * 3 + x
    val gausY = Random.nextGaussian() * 3 + y
    new Particle(gausX.toInt, gausY.toInt, acceleration, lifeTime, angle, Color.randomRed)
  }

}

case class Particle(var x: Int,
                    var y: Int,
                    var acceleration: Double,
                    life: Double,
                    var angle: Int,
                    color: Color)
    extends Renderable {

  var lifeLeft: Double = life
  val height = 4
  val width = 4
  var opacity = 1.0

  def drawOn(ctx: dom.CanvasRenderingContext2D, timeDiff: Double): Unit = {
    if (isGone) {
      return
    }
    color.opacity = opacity
    ctx.fillStyle = color.toString
    ctx.fillRect(x, y, height, width)
  }

  def move(time: Double): Unit = {
    if (opacity < 0) {
      return
    }

    if (time > Explosion.frameLength * 2) {
      // skip big jump during first call
      return
    }

    val s = ((acceleration * (time * time)) / 2).round
    x = x + (Math.cos(angle) * s).round.toInt
    y = y + (Math.sin(angle) * s).round.toInt
    opacity = lifeLeft / life.toFloat
    lifeLeft -= Explosion.frameLength
  }

  def isGone = {
    lifeLeft < 0 || opacity < 0 || y < 0 || y > Dodge.screenHeight || x < 0 ||
    x > Dodge.screenWidth
  }
}

object Explosion {

  val frameLength: Double = 1000 / 30

}

class Explosion(var x: Int, var y: Int, numParticles: Int = 100, lifeTime: Int = 1000) extends Renderable {

  val particles: mutable.ArrayBuffer[Particle] = mutable.ArrayBuffer()
  var hasEmitted = false

  def emit(): Unit = {
    generateParticles(numParticles)
    dom.setTimeout(gcParticles _, 100)
    hasEmitted = true
  }

  override def drawOn(ctx: CanvasRenderingContext2D, timeDiff: Double): Unit = {
    particles.foreach(_.move(timeDiff))
    particles.foreach(_.drawOn(ctx, timeDiff))
  }

  def gcParticles(): Unit = {
    for (particle <- particles.filter(_.isGone)) {
      particles -= particle
    }
  }

  def generateParticles(num: Int): Unit = {
    var i = 0
    while(i < num) {
      particles += Particle.generate(x, y, lifeTime)
      i += 1
    }
  }

  override def height: Int = 1

  override def width: Int = 1
}
