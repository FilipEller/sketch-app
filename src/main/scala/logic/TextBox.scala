package logic

import scalafx.geometry.Point2D
import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.rgb

case class TextBox(text: String, width: Double, height: Double, fontSize: Double, color: Color,
                 origin: Point2D, name: String, rotation: Int = 0,
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


  def collidesWith(point: Point2D): Boolean =
    (point.x >= this.origin.x
        && point.x <= this.origin.x + this.width
        && point.y >= this.origin.y
        && point.y <= this.origin.y + this.height)
}

object TextBox {

  var strokeCount = 0

  def apply(text: String, width: Double, height: Double, fontSize: Double, color: Color,
                 origin: Point2D, name: String = "", rotation: Int = 0,
                 previousVersion: Option[Element] = None, hidden: Boolean = false, deleted: Boolean = false) = {

    val nameToUse = {
      if (name == "") {
        strokeCount += 1
        s"Stroke $strokeCount"
      } else {
        name
      }
    }

    new TextBox(text, width, height, fontSize, color, origin, nameToUse, rotation, previousVersion, hidden, deleted)
  }

}