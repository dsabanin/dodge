package skullbeware

import org.scalajs.dom

class TextOverlay(val str: String, val fadeSpeed: Double = 0.005) extends Renderable {

  def x = (Dodge.screenWidth - str.size*(fontSize/2)) / 2
  def y = Dodge.screenHeight/2

  def width = 3
  def height  = 4

  val image = None
  val fontSize = 30
  var opacity = 1.0

  def drawOn(ctx: dom.CanvasRenderingContext2D): Unit = {
    if(opacity < 0) {
      return
    }
    opacity -= fadeSpeed
    ctx.fillStyle = s"rgba(255, 255, 255, $opacity)"
    ctx.font = s"normal bold ${fontSize}px sans-serif"
    ctx.textBaseline = "bottom"
    ctx.fillText(str, x, y, Dodge.screenWidth)
  }

  def isGone = opacity < 0
}
