package logic

import javafx.scene.input.MouseEvent
import scalafx.geometry.Point2D
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.rgb

import scala.math.{abs, max, min}

class ShapeTool(stype: ShapeType) extends Tool {
  var currentElement = new Shape(this.stype, 0, 0, 0, rgb(0, 0, 0), rgb(0, 0, 0, 0), true, true, new Point2D(0, 0), "")
  var clickPoint = new Point2D(0, 0)

  def updateCurrentElement(drawing: Drawing, eventPoint: Point2D): Element = {
    val xDiff = eventPoint.x - clickPoint.x
    val yDiff = eventPoint.y - clickPoint.y

    val (width: Double, height, origin) = this.stype match {
      case Rectangle | Ellipse => {
        val width = abs(xDiff)
        val height = abs(yDiff)
        val origin = new Point2D(min(clickPoint.x, eventPoint.x), min(clickPoint.y, eventPoint.y))
        (width, height, origin)
      }
      case _ => { // Square and Circle
        val smallerDiff = if (abs(xDiff) > abs(yDiff)) xDiff else yDiff
        val width = abs(smallerDiff)
        val height = width
        val origin = new Point2D(min(clickPoint.x, clickPoint.x + xDiff.sign * width), min(clickPoint.y, clickPoint.y + yDiff.sign * height))
        (width, height, origin)
      }
    }
    this.currentElement = this.currentElement.copy(width = width, height = height, origin = origin)
    this.currentElement
  }

  def use(drawing: Drawing, event: MouseEvent, eventPoint: Point2D): Unit = {
    val config = drawing.config
    event.getEventType match {
      case MouseEvent.MOUSE_PRESSED => {
        this.clickPoint = eventPoint
        this.currentElement = Shape(this.stype, 0, 0, config.borderWidth, drawing.config.primaryColor, drawing.config.secondaryColor, drawing.config.useBorder, drawing.config.useFill, this.clickPoint)
      }
      case MouseEvent.MOUSE_DRAGGED => {
        config.activeLayer.updateElement(this.currentElement, this.updateCurrentElement(drawing, eventPoint))
      }
      case MouseEvent.MOUSE_RELEASED => {
        config.activeLayer.updateElement(this.currentElement, this.updateCurrentElement(drawing, eventPoint))
        ActionHistory.add(this.currentElement)
      }
      case _ => {
        println("unrecognized mouseEvent type: " + event.getEventType)
      }
    }
  }
}

object RectangleTool extends ShapeTool(Rectangle)

object SquareTool extends ShapeTool(Square)

object EllipseTool extends ShapeTool(Ellipse)

object CircleTool extends ShapeTool(Circle)
