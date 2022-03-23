package logic

import scalafx.geometry.Point2D
import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color

import scala.math.{pow, sqrt}

abstract class ShapeType

case object Rectangle extends ShapeType
case object Square extends ShapeType
case object Ellipse extends ShapeType
case object Circle extends ShapeType


case class Shape(stype: ShapeType, id: String, width: Double, height: Double, borderWidth: Double, color: Color, borderColor: Color,
                 origin: Point2D, rotation: Int = 0,
                 previousVersion: Option[Element] = None, hidden: Boolean = false, deleted: Boolean = false) extends Element {

  val center = new Point2D(this.origin.x + this.width, this.origin.y + this.height)

  override def toString: String = {
    s"$stype" // at $origin with size $width and $height colored $color"
  }

  def collidesWith(point: Point2D): Boolean = {
    this.stype match { // does not take rotation into account yet
      case s if s == Rectangle || s == Square => (point.x >= this.origin.x
        && point.x <= this.origin.x + this.width
        && point.y >= this.origin.y
        && point.y <= this.origin.y + this.height)
      case Ellipse => pow((point.x - this.center.x) / (this.width / 2), 2) + pow((point.y - this.center.y) / (this.height / 2), 2) <= 1
        // https://www.geeksforgeeks.org/check-if-a-point-is-inside-outside-or-on-the-ellipse/
      case Circle => {
        val middle = new Point2D(this.origin.x + 0.5 * this.width, this.origin.y + 0.5 * this.height)
        val xDiff = point.x - middle.x
        val yDiff = point.y - middle.y
        sqrt(pow(xDiff, 2) + pow(yDiff, 2)) <= 0.5 * this.width
      }
      case _ => false
    }
  }

  def paint(canvas: Canvas): Unit = {
    val g = canvas.graphicsContext2D
    // rotation not implemented
    g.fill = this.color
    this.stype match {
      case Rectangle => g.fillRect(origin.x, origin.y, this.width, this.height)
      case Square => g.fillRect(origin.x, origin.y, this.width, this.width)
      case Ellipse => g.fillOval(origin.x, origin.y, this.width, this.height)
      case Circle => g.fillOval(origin.x, origin.y, this.width, this.width)
      case _ =>
    }

    if (this.borderWidth > 0) {
      g.stroke = this.borderColor
      g.setLineWidth(borderWidth)
      this.stype match {
        case Rectangle => g.strokeRect(origin.x, origin.y, this.width, this.height)
        case Square => g.strokeRect(origin.x, origin.y, this.width, this.width)
        case Ellipse => g.strokeOval(origin.x, origin.y, this.width, this.height)
        case Circle => g.strokeOval(origin.x, origin.y, this.width, this.width)
        case _ =>
      }
    }
  }

  def move(newOrigin: Point2D) = this.copy(origin = newOrigin)


  def rotate(angle: Int) = this.copy(rotation = this.rotation + angle)

}