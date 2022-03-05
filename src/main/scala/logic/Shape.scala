package logic

import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color

abstract class ShapeType

case object Rectangle extends ShapeType
case object Square extends ShapeType
case object Ellipse extends ShapeType
case object Circle extends ShapeType


case class Shape(stype: ShapeType, width: Int, height: Int, borderWidth: Int, borderColor: Color,
                     id: String, origin: Point, rotation: Int, color: Color,
                     group: Option[Long], previousVersion: Option[Element], hidden: Boolean, deleted: Boolean) extends Element {

  def paint(canvas: Canvas): Unit = {
    val g = canvas.graphicsContext2D
    g.fill = this.color
    this.stype match {
      case Rectangle => g.fillRect(origin.x, origin.y, this.width, this.height)
      case Square => g.fillRect(origin.x, origin.y, this.width, this.width)
      case Ellipse => g.fillOval(origin.x, origin.y, this.width, this.height)
      case Square => g.fillOval(origin.x, origin.y, this.width, this.width)
    }

    if (this.borderWidth > 0) {
      g.stroke = this.borderColor
      g.setLineWidth(borderWidth)
      this.stype match {
        case Rectangle => g.strokeRect(origin.x, origin.y, this.width, this.height)
        case Square => g.strokeRect(origin.x, origin.y, this.width, this.width)
        case Ellipse => g.strokeOval(origin.x, origin.y, this.width, this.height)
        case Square => g.strokeOval(origin.x, origin.y, this.width, this.width)
      }
    }
  }

  def move(newOrigin: Point): Element = {
    /*this.stype match {
      case Rectangle => new Rectangle(width, height, borderWidth, borderColor, id, newOrigin, rotation, color, group, Some(this), hidden, deleted)
      case Square => new Square(width, borderWidth, borderColor, id, newOrigin, rotation, color, group, Some(this), hidden, deleted)
      case Ellipse => new Ellipse(width, height, borderWidth, borderColor, id, newOrigin, rotation, color, group, Some(this), hidden, deleted)
      case Circle => new Circle(width, borderWidth, borderColor, id, newOrigin, rotation, color, group, Some(this), hidden, deleted)
    }*/

    this.copy(origin = newOrigin)
  }

  def rotate(angle: Int): Element = {
    /*this.stype match {
      case Rectangle => new Rectangle(width, height, borderWidth, borderColor, id, origin, rotation + angle, color, group, Some(this), hidden, deleted)
      case Square => new Square(width, borderWidth, borderColor, id, origin, rotation + angle, color, group, Some(this), hidden, deleted)
      case Ellipse => new Ellipse(width, height, borderWidth, borderColor, id, origin, rotation + angle, color, group, Some(this), hidden, deleted)
      case Circle => new Circle(width, borderWidth, borderColor, id, origin, rotation + angle, color, group, Some(this), hidden, deleted)
    }*/

    this.copy(rotation = this.rotation + angle)
  }

}

/*
 class Rectangle(width: Int, height: Int, borderWidth: Int, borderColor: Color,
                     id: String, origin: Point, rotation: Int, color: Color,
                     group: Option[Long], previousVersion: Option[Element], hidden: Boolean, deleted: Boolean)
  extends Shape(Rectangle, width, height, borderWidth, borderColor, id, origin, rotation, color, group, previousVersion, hidden, deleted)

class Square(width: Int, borderWidth: Int, borderColor: Color,
                     id: String, origin: Point, rotation: Int, color: Color,
                     group: Option[Long], previousVersion: Option[Element], hidden: Boolean, deleted: Boolean)
  extends Shape(Square, width, width, borderWidth, borderColor, id, origin, rotation, color, group, previousVersion, hidden, deleted)

class Ellipse(width: Int, height: Int, borderWidth: Int, borderColor: Color,
                     id: String, origin: Point, rotation: Int, color: Color,
                     group: Option[Long], previousVersion: Option[Element], hidden: Boolean, deleted: Boolean)
  extends Shape(Ellipse, width, height, borderWidth, borderColor, id, origin, rotation, color, group, previousVersion, hidden, deleted)

class Circle(width: Int, borderWidth: Int, borderColor: Color,
                     id: String, origin: Point, rotation: Int, color: Color,
                     group: Option[Long], previousVersion: Option[Element], hidden: Boolean, deleted: Boolean)
  extends Shape(Circle, width, width, borderWidth, borderColor, id, origin, rotation, color, group, previousVersion, hidden, deleted)
 */