package skullbeware

import org.scalajs.dom

trait Renderable {
  def x: Double

  def y: Double

  def width: Double

  def height: Double

  def drawOn(ctx: dom.CanvasRenderingContext2D): Unit

  def xs = x.to(x + width).by(1)

  def ys = y.to(y + height).by(1)
}
