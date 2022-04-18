package logic

import javafx.scene.input.MouseEvent
import scalafx.geometry.Point2D
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.rgb

import scala.math.min

sealed abstract class StrokeTool extends Tool {

  var currentElement = new Stroke(rgb(0, 0, 0), new Point2D(0, 0), Path(new Point2D(0, 0)), new Brush(1, 100), "")
  var clickPoint = new Point2D(0, 0)

  def setCurrentElement(eventPoint: Point2D, origin: Point2D): Unit

  def updateCurrentElement(drawing: Drawing, eventPoint: Point2D): Element = {
    val originX = min(this.currentElement.origin.x, eventPoint.x)
    val originY = min(this.currentElement.origin.y, eventPoint.y)
    val origin = new Point2D(originX, originY)
    this.setCurrentElement(eventPoint, origin)
    this.currentElement
  }

  def use(drawing: Drawing, event: MouseEvent, eventPoint: Point2D): Unit = {
    val config = drawing.config
    val layer = config.activeLayer
    event.getEventType match {
      case MouseEvent.MOUSE_PRESSED => {
        this.clickPoint = eventPoint
        this.currentElement = Stroke(config.primaryColor, this.clickPoint, Path(this.clickPoint), config.activeBrush)
        layer.addElement(this.currentElement)
      }
      case MouseEvent.MOUSE_DRAGGED => {
        layer.updateElement(this.currentElement, updateCurrentElement(drawing, eventPoint))
      }
      case MouseEvent.MOUSE_RELEASED => {
        layer.updateElement(this.currentElement, updateCurrentElement(drawing, eventPoint))
        ActionHistory.add(this.currentElement)
      }
      case _ => {
        println("unrecognized mouseEvent type: " + event.getEventType)
      }
    }
  }
}

object BrushTool extends StrokeTool {
  def setCurrentElement(eventPoint: Point2D, origin: Point2D): Unit = {
    this.currentElement = this.currentElement.copy(path = this.currentElement.path :+ eventPoint, origin = origin)
  }
}

object LineTool extends StrokeTool {
  def setCurrentElement(eventPoint: Point2D, origin: Point2D): Unit = {
    this.currentElement = this.currentElement.copy(path = Path(this.currentElement.path.head, eventPoint), origin = origin)
  }
}