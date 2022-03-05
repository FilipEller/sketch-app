package logic

import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color

abstract class Shape(width: Int, height: Int, borderWidth: Int, borderColor: Color,
                     id: String, origin: Point, rotation: Int, color: Color,
                     group: Option[Long], previousVersion: Option[Element], hidden: Boolean, deleted: Boolean)
  extends Element(id, origin, rotation, color, group, previousVersion, hidden, deleted)


class Rectangle(width: Int, height: Int, borderWidth: Int, borderColor: Color,
                     id: String, origin: Point, rotation: Int, color: Color,
                     group: Option[Long], previousVersion: Option[Element], hidden: Boolean, deleted: Boolean)
  extends Shape(width, height, borderWidth, borderColor, id, origin, rotation, color, group, previousVersion, hidden, deleted) {

  def paint(canvas: Canvas): Unit = {
    val g = canvas.graphicsContext2D
    g.fill = this.color
    g.fillRect(origin.x, origin.y, this.width, this.height)
  }

  def move(newOrigin: Point): Element = {
    new Rectangle(width, height, borderWidth, borderColor, id, newOrigin, rotation, color, group, Some(this), hidden, deleted)
  }

  def rotate(angle: Int): Element = {
    new Rectangle(width, height, borderWidth, borderColor, id, origin, rotation + angle, color, group, Some(this), hidden, deleted)
  }
}


class Square(width: Int, borderWidth: Int, borderColor: Color,
                     id: String, origin: Point, rotation: Int, color: Color,
                     group: Option[Long], previousVersion: Option[Element], hidden: Boolean, deleted: Boolean)
  extends Rectangle(width, width, borderWidth, borderColor, id, origin, rotation, color, group, previousVersion, hidden, deleted) {

  override def paint(canvas: Canvas): Unit = {
    val g = canvas.graphicsContext2D
    g.fill = this.color
    g.fillRect(origin.x, origin.y, this.width, this.width)
  }

  override def move(newOrigin: Point): Element = {
    new Square(width, borderWidth, borderColor, id, newOrigin, rotation, color, group, Some(this), hidden, deleted)
  }

  override def rotate(angle: Int): Element = {
    new Square(width, borderWidth, borderColor, id, origin, rotation + angle, color, group, Some(this), hidden, deleted)
  }
}
