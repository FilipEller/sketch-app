package logic

import scalafx.geometry.Point2D
import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color

import scala.math.{pow, sqrt}

abstract sealed class ShapeType

case object Rectangle extends ShapeType
case object Square extends ShapeType
case object Ellipse extends ShapeType
case object Circle extends ShapeType


case class Shape(stype: ShapeType,
                 width: Double,
                 height: Double,
                 borderWidth: Double,
                 color: Color,
                 fillColor: Color,
                 useBorder: Boolean,
                 useFill: Boolean,
                 origin: Point2D,
                 name: String,
                 previousVersion: Option[Element] = None,
                 hidden: Boolean = false,
                 deleted: Boolean = false) extends Element {

  val center = new Point2D(this.origin.x + 0.5 * this.width, this.origin.y + 0.5 * this.height)

  override def toString: String = this.name

  def collidesWith(point: Point2D): Boolean = {
    this.stype match {
      case s: ShapeType if s == Rectangle || s == Square => (point.x >= this.origin.x - 0.5 * this.borderWidth
        && point.x <= this.origin.x + this.width + 0.5 * this.borderWidth
        && point.y >= this.origin.y - 0.5 * this.borderWidth
        && point.y <= this.origin.y + this.height + 0.5 * this.borderWidth)
      case Ellipse => pow((point.x - this.center.x) / ((this.width + this.borderWidth) / 2), 2) + pow((point.y - this.center.y) / ((this.height + this.borderWidth) / 2), 2) <= 1
        // https://www.geeksforgeeks.org/check-if-a-point-is-inside-outside-or-on-the-ellipse/
      case Circle => {
        val xDiff = point.x - this.center.x
        val yDiff = point.y - this.center.y
        sqrt(pow(xDiff, 2) + pow(yDiff, 2)) <= 0.5 * (this.width + this.borderWidth)
      }
      case _ => false
    }
  }

  def paint(canvas: Canvas): Unit = {
    if (!this.deleted) {
      val g = canvas.graphicsContext2D
      if (this.useFill) {
        g.fill = this.fillColor
        this.stype match {
          case Rectangle | Square => g.fillRect(origin.x, origin.y, this.width, this.height)
          case Ellipse | Circle => g.fillOval(origin.x, origin.y, this.width, this.height)
          case _ =>
        }
      }

      if (this.useBorder) {
        g.stroke = this.color
        g.setLineWidth(borderWidth)
        this.stype match {
          case Rectangle | Square => g.strokeRect(origin.x, origin.y, this.width, this.height)
          case Ellipse | Circle => g.strokeOval(origin.x, origin.y, this.width, this.height)
          case _ =>
        }
      }
    }
  }
}

object Shape {

  var rectangleCount = 0
  var squareCount = 0
  var circleCount = 0
  var ellipseCount = 0

  def apply(stype: ShapeType, width: Double, height: Double, borderWidth: Double, color: Color, borderColor: Color, useBorder: Boolean, useFill: Boolean, origin: Point2D,
            name: String = "", previousVersion: Option[Element] = None, hidden: Boolean = false, deleted: Boolean = false) = {

    val nameToUse = {
      if (name == "") {
        stype match {
          case Rectangle => {
            rectangleCount += 1
            s"Rectangle $rectangleCount"
          }
          case Square => {
            squareCount += 1
            s"Square $squareCount"
          }
          case Circle => {
            circleCount += 1
            s"Circle $circleCount"
          }
          case Ellipse => {
            ellipseCount += 1
            s"Ellipse $ellipseCount"
          }
        }
      } else {
        name
      }
    }

    new Shape(stype, width, height, borderWidth, color, borderColor, useBorder, useFill, origin, nameToUse, previousVersion, hidden, deleted)
  }

}