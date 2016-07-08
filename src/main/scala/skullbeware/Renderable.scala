package skullbeware

import org.scalajs.dom
import org.scalajs.dom.html

trait Renderable {
  def image: Option[html.Image] = None

  def x: Int

  def y: Int

  def width: Int

  def height: Int

  def drawOn(ctx: dom.CanvasRenderingContext2D, timeDiff: Double): Unit

  def xs = x to endX by 1

  def ys = y to endY by 1

  def endX = x + width
  def endY = y + height

  def isTouching(other: Renderable): Boolean = {
    if (((this.endX >= other.x) && (this.x <= other.endX)) && ((this.endY >= other.y) && (this.y <= other.endY))) {

      val xInter = xs.intersect(other.xs)
      val yInter = ys.intersect(other.ys)

      val x0 = xInter.min
      val x1 = xInter.max
      val y0 = yInter.min
      val y1 = yInter.max
      if (!(this.isEmptyAt(x0, x1, y0, y1, Dodge.collisionCtx) ||
                other.isEmptyAt(x0, x1, y0, y1, Dodge.collisionCtx))) {
        return true
      }
    }
    false
  }

  def middleX = width / 2

  def middleY = height / 2

  def isEmptyAt(x0: Int, x1: Int, y0: Int, y1: Int, ctx: dom.CanvasRenderingContext2D): Boolean = {
    if (image.isEmpty) {
      return false
    }
    val w = x1 - x0 + 1
    val h = y1 - y0 + 1
    ctx.fillStyle = "black"
    ctx.fillRect(0, 0, ctx.canvas.width, ctx.canvas.height)
    ctx.drawImage(image.get, x, y, width, height)
    val imageData = ctx.getImageData(x0, y0, w, h)
    ctx.fillStyle = "red"
    ctx.fillRect(x0, y0, w, h)
    val pixels = imageData.data

    var i = 0
    while (i < pixels.length) {
      if (!(pixels(i) == 0 && pixels(i + 1) == 0 && pixels(i + 2) == 0)) {
        return false
      }
      i += 4
    }
    true
  }
}
