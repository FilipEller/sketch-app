package logic

import scalafx.geometry.Point2D
import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.rgb
import scalafx.scene.text.Font

case class TextBox(text: String,
                   width: Double,
                   height: Double,
                   fontSize: Double,
                   color: Color,
                   origin: Point2D,
                   name: String,
                   previousVersion: Option[Element] = None,
                   hidden: Boolean = false,
                   deleted: Boolean = false) extends Element {

  override def toString = s"$name ($text)"

  def paint(canvas: Canvas): Unit = {
    if (!this.deleted) {
      val g = canvas.graphicsContext2D
      g.fill = this.color
      g.setLineWidth(1)
      g.font = new Font("Poppins", fontSize)
      g.fillText(this.text, origin.x + 1, origin.y + fontSize, this.width)
      // g.fill = rgb(0, 0, 0)
      // g.strokeRect(origin.x, origin.y, this.width, this.height)
    }
  }


  def collidesWith(point: Point2D): Boolean =
    (point.x >= this.origin.x
        && point.x <= this.origin.x + this.width
        && point.y >= this.origin.y
        && point.y <= this.origin.y + this.height)
}

object TextBox {

  var strokeCount = 0

  def apply(text: String, width: Double, height: Double, fontSize: Double, color: Color, origin: Point2D,
                 name: String = "", previousVersion: Option[Element] = None, hidden: Boolean = false, deleted: Boolean = false) = {

    val nameToUse = {
      if (name == "") {
        strokeCount += 1
        s"Stroke $strokeCount"
      } else {
        name
      }
    }

    // TODO: Undo not working with text boxes

    new TextBox(text, width, height, fontSize, color, origin, nameToUse, previousVersion, hidden, deleted)
  }

}