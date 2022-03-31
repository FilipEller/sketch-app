package logic

import javafx.scene.input.MouseEvent
import scalafx.geometry.Point2D
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.rgb

import scala.math.{abs, max, min}

class ShapeTool(stype: ShapeType) extends Tool {
  // type, id, width, height, border width, color, border color, origin)
  var currentElement = new Shape(this.stype, 0, 0, 0, rgb(0, 0, 0), rgb(0, 0, 0, 0), new Point2D(0, 0), "")
  var clickPoint = new Point2D(0, 0)

  def updateCurrentElement(drawing: Drawing, eventPoint: Point2D): Element = {
    val xDiff =  min(max(0, eventPoint.x) - clickPoint.x, drawing.width - clickPoint.x) // does not yet completely take care of not drawing over the lines with square and circle
    val yDiff = min(max(0, eventPoint.y) - clickPoint.y, drawing.height - clickPoint.y)

    val (width: Double, height, origin) = this.stype match {
      case s: ShapeType if s == Rectangle || s == Ellipse => {
        val width = abs(xDiff) // takes care of not drawing over the lines. But should it?
        val height = abs(yDiff)
        val origin = new Point2D(min(abs(clickPoint.x), max(0, eventPoint.x)), min(abs(clickPoint.y), max(0, eventPoint.y)))
        (width, height, origin)
      }
      case _ => { // Square and Circle
        val smallerDiff = if (abs(xDiff) > abs(yDiff)) xDiff else yDiff
        val width = abs(smallerDiff)
        val height = width
        val origin = new Point2D(max(0, min(clickPoint.x, clickPoint.x + xDiff.sign * width)), max(0, min(clickPoint.y, clickPoint.y + yDiff.sign * height)))
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
        println("MOUSE_PRESSED")
        this.clickPoint = eventPoint
        this.currentElement = Shape(this.stype, 0, 0, 3, drawing.fillColor, drawing.borderColor, this.clickPoint)
      }
      case MouseEvent.MOUSE_DRAGGED => {
        println("MOUSE_DRAGGED")
        config.activeLayer.updateElement(this.currentElement, this.updateCurrentElement(drawing, eventPoint))
      }
      case MouseEvent.MOUSE_RELEASED => {
        println("MOUSE_RELEASED")
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
