package skullbeware

import org.scalajs.dom
import org.scalajs.dom.html

trait Renderable {
  val image: Option[html.Image]

  def x: Int

  def y: Int

  def width: Int

  def height: Int

  def drawOn(ctx: dom.CanvasRenderingContext2D): Unit

  def xs = x.to(x + width).by(1)

  def ys = y.to(y + height).by(1)

  def middleX = width / 2

  def middleY = height / 2

  def isEmptyAt(xs: Seq[Int],
                ys: Seq[Int],
                ctx: dom.CanvasRenderingContext2D): Boolean = {
    if (image.isEmpty) {
      return false
    }
    val sx = xs.min
    val sy = ys.min
    val w = xs.max - xs.min + 1
    val h = ys.max - ys.min + 1
//    println(s"sx = $sx; sy = $sy; w = $w; h = $h; xInter = $xs; yInter = $ys")
    ctx.fillStyle = "black"
    ctx.fillRect(0, 0, ctx.canvas.width, ctx.canvas.height)
    ctx.drawImage(image.get, x, y, width, height)
    val imageData = ctx.getImageData(sx, sy, w, h)
    ctx.fillStyle = "red"
    ctx.fillRect(sx, sy, w, h)
    val pixels = imageData.data

    for (pixel <- pixels.grouped(4)) {
      if (!(pixel(0) == 0 && pixel(1) == 0 && pixel(2) == 0)) {
        return false
      }
    }
    true
  }
}
