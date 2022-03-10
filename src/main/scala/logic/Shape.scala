package logic

import scalafx.geometry.Point2D
import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color

abstract class ShapeType

case object Rectangle extends ShapeType
case object Square extends ShapeType
case object Ellipse extends ShapeType
case object Circle extends ShapeType


case class Shape(stype: ShapeType, id: String, width: Double, height: Double, borderWidth: Double, color: Color, borderColor: Color,
                 origin: Point2D, rotation: Int = 0,
                 group: Option[Long] = None, previousVersion: Option[Element] = None, hidden: Boolean = false, deleted: Boolean = false) extends Element {

   override def toString: String = {
     s"$stype at $origin with size $width and $height colored $color"
   }

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

  def move(newOrigin: Point2D): Element = this.copy(origin = newOrigin)


  def rotate(angle: Int): Element = this.copy(rotation = this.rotation + angle)

}