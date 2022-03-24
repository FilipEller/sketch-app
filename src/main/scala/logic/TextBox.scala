package logic

import scalafx.geometry.Point2D
import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.rgb

case class TextBox(text: String, name: String, width: Double, height: Double, fontSize: Double, color: Color,
                 origin: Point2D, rotation: Int = 0,
                 previousVersion: Option[Element] = None, hidden: Boolean = false, deleted: Boolean = false) extends Element {

  def paint(canvas: Canvas): Unit = {
    val g = canvas.graphicsContext2D
    // rotation not implemented
    g.fill = this.color
    g.setLineWidth(1)
    // g.font = OTHER FONTS NOT IMPLEMENTED
    g.fillText(this.text, origin.x + 1, origin.y + fontSize, this.width)
    // g.fill = rgb(0, 0, 0)
    // g.strokeRect(origin.x, origin.y, this.width, this.height)
  }

  def move(newOrigin: Point2D) = this.copy(origin = newOrigin, previousVersion = Some(this))
  def rotate(angle: Int) = this.copy(rotation = this.rotation + angle, previousVersion = Some(this))

  def collidesWith(point: Point2D): Boolean =
    (point.x >= this.origin.x
        && point.x <= this.origin.x + this.width
        && point.y >= this.origin.y
        && point.y <= this.origin.y + this.height)
}
