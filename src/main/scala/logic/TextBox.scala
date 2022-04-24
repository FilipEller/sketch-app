package logic

import scalafx.geometry.Point2D
import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color
import scalafx.scene.text.Font

case class TextBox(text: String,
                   width: Double,
                   height: Double,
                   fontSize: Double,
                   color: Color,
                   origin: Point2D,
                   name: String,
                   previousVersion: Option[Element] = None,
                   isDeleted: Boolean = false) extends Element {

  override def toString = s"$name ($text)"

  def paint(canvas: Canvas): Unit = {
    if (!this.isDeleted) {
      val g = canvas.graphicsContext2D
      g.fill = this.color
      g.setLineWidth(1)
      g.font = new Font("Poppins", fontSize)
      if (this.text.nonEmpty) {
        // Render the text of the Element
        // The text is squeezed to the width of the Element
        // but it may overflow the height
        g.fillText(this.text, origin.x + 1, origin.y + fontSize, this.width)
      } else {
        // Render a filler text if the TextBox is empty.
        g.fillText("Lorem Ipsum", origin.x + 1, origin.y + fontSize, this.width)
      }
    }
  }

  def rewrite(newText: String): TextBox = {
    this.copy(text = newText, previousVersion = Some(this))
  }

  def collidesWith(point: Point2D): Boolean =
    (point.x >= this.origin.x
        && point.x <= this.origin.x + this.width
        && point.y >= this.origin.y
        && point.y <= this.origin.y + this.height)
}

object TextBox {

  // Counts the number of TextBoxes created during this run of the program
  // The count is used to name new TextBoxes uniquely.
  var boxCount = 0

  def apply(text: String, width: Double, height: Double, fontSize: Double, color: Color, origin: Point2D,
                 name: String = "", previousVersion: Option[Element] = None, deleted: Boolean = false) = {

    val nameToUse = {
      if (name == "") {
        boxCount += 1
        s"Text Box $boxCount"
      } else {
        name
      }
    }

    new TextBox(text, width, height, fontSize, color, origin, nameToUse, previousVersion, deleted)
  }

}