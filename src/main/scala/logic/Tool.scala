package logic

import javafx.scene.input.MouseEvent
import javafx.scene.input.MouseDragEvent
import scalafx.geometry.Point2D
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.rgb

import scala.math.{abs, min, max}

trait Tool {
  def use(drawing: Drawing, config: Configurations, event: MouseEvent, eventPoint: Point2D): Unit
}

abstract class DrawsShapes(stype: ShapeType) extends Tool {
  // type, id, width, height, border width, color, border color, origin)
  var currentElement: Element = Shape(this.stype, "temp", 0, 0, 0, rgb(0, 0, 0), rgb(0, 0, 0, 0), new Point2D(0, 0))
  var clickPoint = new Point2D(0, 0)

  def getUpdated(drawing: Drawing, config: Configurations, eventPoint: Point2D) = {
    val (width: Double, height, origin) = this.stype match {
      case s: ShapeType if s == Rectangle || s == Ellipse => {
        val width = abs(clickPoint.x - max(0, eventPoint.x))
        val height = abs(clickPoint.y - max(0, eventPoint.y))
        val origin = new Point2D(min(abs(clickPoint.x), max(0, eventPoint.x)), min(abs(clickPoint.y), max(0, eventPoint.y)))
        (width, height, origin)
      }
      case _ => { // Square and Circle
        val xDiff =  max(0, eventPoint.x) - clickPoint.x
        val yDiff = max(0, eventPoint.y) - clickPoint.y
        val smallerDiff = if (abs(xDiff) > abs(yDiff)) xDiff else yDiff
        val width = abs(smallerDiff)
        val height = width
        val origin = new Point2D(max(0, min(clickPoint.x, clickPoint.x + xDiff.sign * width)), max(0, min(clickPoint.y, clickPoint.y + yDiff.sign * height)))
        (width, height, origin)
      }
    }
    Shape(this.stype, "temp", width, height, 3, config.primaryColor, config.secondaryColor, origin)
  }

  def drawShape(drawing: Drawing, config: Configurations, event: MouseEvent, eventPoint: Point2D): Unit = {

    event.getEventType match {
      case MouseEvent.MOUSE_PRESSED => {
        println("MOUSE_PRESSED")
        this.clickPoint = eventPoint
        println("clicked at " + clickPoint)
        this.currentElement = Shape(this.stype, "temp", 0, 0, 0, config.primaryColor, config.secondaryColor, this.clickPoint)
      }
      case MouseEvent.MOUSE_DRAGGED => {
        println("MOUSE_DRAGGED")
        config.activeLayer.removeElement(this.currentElement)
        this.currentElement = getUpdated(drawing, config, eventPoint)
        println("dragging at " + eventPoint)
        config.activeLayer.addElement(this.currentElement)
      }
      case MouseEvent.MOUSE_RELEASED => {
        println("MOUSE_RELEASED")
        config.activeLayer.removeElement(this.currentElement)
        this.currentElement = getUpdated(drawing, config, eventPoint)
        println("released at " + eventPoint)
        config.activeLayer.addElement(currentElement)
      }
      case _ => {
        println("unrecognized mouseDragEvent type: " + event.getEventType)
      }
    }
  }
}

object RectangleTool extends DrawsShapes(Rectangle) {
  def use(drawing: Drawing, config: Configurations, event: MouseEvent, eventPoint: Point2D): Unit = {
    this.drawShape(drawing, config, event, eventPoint)
  }
}

object SquareTool extends DrawsShapes(Square) {
  def use(drawing: Drawing, config: Configurations, event: MouseEvent, eventPoint: Point2D): Unit = {
    this.drawShape(drawing, config, event, eventPoint)
  }
}

object EllipseTool extends DrawsShapes(Ellipse) {
  def use(drawing: Drawing, config: Configurations, event: MouseEvent, eventPoint: Point2D): Unit = {
    this.drawShape(drawing, config, event, eventPoint)
  }
}