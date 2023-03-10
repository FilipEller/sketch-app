package logic

import scalafx.geometry.Point2D
import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color

import scala.math.{pow, sqrt}

// Used to distinguish different kinds of shapes
abstract sealed class ShapeType

case object Rectangle extends ShapeType
case object Square extends ShapeType
case object Ellipse extends ShapeType
case object Circle extends ShapeType


case class Shape(shapeType: ShapeType,
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
                 isDeleted: Boolean = false) extends Element {

  val center = new Point2D(this.origin.x + 0.5 * this.width, this.origin.y + 0.5 * this.height)

  override def toString: String = this.name

  // Check if this Element encloses the given Point.
  def collidesWith(point: Point2D): Boolean = {
    this.shapeType match {
      case s: ShapeType if s == Rectangle || s == Square => (point.x >= this.origin.x - 0.5 * this.borderWidth
        && point.x <= this.origin.x + this.width + 0.5 * this.borderWidth
        && point.y >= this.origin.y - 0.5 * this.borderWidth
        && point.y <= this.origin.y + this.height + 0.5 * this.borderWidth)
      case Ellipse => {
        val xDir = pow((point.x - this.center.x) / ((this.width + this.borderWidth) / 2), 2)
        val yDir = pow((point.y - this.center.y) / ((this.height + this.borderWidth) / 2), 2)
        xDir + yDir <= 1
      }
      case Circle => {
        val xDiff = point.x - this.center.x
        val yDiff = point.y - this.center.y
        sqrt(pow(xDiff, 2) + pow(yDiff, 2)) <= (this.width + this.borderWidth) / 2
      }
      case _ => false
    }
  }

  // Render the Element
  def paint(canvas: Canvas): Unit = {
    if (!this.isDeleted) {
      val g = canvas.graphicsContext2D
      if (this.useFill) {
        // Fill the inside of the Element with this.fillColor
        g.fill = this.fillColor
        this.shapeType match {
          case Rectangle | Square => g.fillRect(origin.x, origin.y, this.width, this.height)
          case Ellipse | Circle => g.fillOval(origin.x, origin.y, this.width, this.height)
          case _ =>
        }
      }

      if (this.useBorder) {
        // Paint the border of the Element with this.color
        g.stroke = this.color
        g.setLineWidth(borderWidth)
        this.shapeType match {
          case Rectangle | Square => g.strokeRect(origin.x, origin.y, this.width, this.height)
          case Ellipse | Circle => g.strokeOval(origin.x, origin.y, this.width, this.height)
          case _ =>
        }
      }
    }
  }
}

object Shape {

  // Counts the number of each kind of Shape created during this run of the program
  // The count is used to name new Shapes uniquely.
  var rectangleCount = 0
  var squareCount = 0
  var circleCount = 0
  var ellipseCount = 0

  def apply(stype: ShapeType, width: Double, height: Double, borderWidth: Double,
            color: Color, borderColor: Color, useBorder: Boolean, useFill: Boolean, origin: Point2D,
            name: String = "", previousVersion: Option[Element] = None, deleted: Boolean = false) = {

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

    new Shape(stype, width, height, borderWidth,
      color, borderColor, useBorder, useFill, origin,
      nameToUse, previousVersion, deleted)
  }

}