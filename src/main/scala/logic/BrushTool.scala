package logic

import javafx.scene.input.MouseEvent
import scalafx.geometry.Point2D
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.rgb

import scala.math.min

sealed abstract class StrokeTool extends Tool {

  var currentElement = Stroke( "temp", rgb(0, 0, 0), new Point2D(0, 0), Path(new Point2D(0, 0)), new Brush(1, 100))
  var clickPoint = new Point2D(0, 0)

  def setCurrentElement(eventPoint: Point2D, origin: Point2D): Unit

  def updateCurrentElement(drawing: Drawing, eventPoint: Point2D): Unit = {
    val originX = min(this.currentElement.origin.x, eventPoint.x)
    val originY = min(this.currentElement.origin.y, eventPoint.y)
    val origin = new Point2D(originX, originY)
    setCurrentElement(eventPoint, origin)
  }

  def use(drawing: Drawing, event: MouseEvent, eventPoint: Point2D): Unit = {
    val config = drawing.config
    event.getEventType match {
      case MouseEvent.MOUSE_PRESSED => {
        println("MOUSE_PRESSED")
        this.clickPoint = eventPoint
        this.currentElement = Stroke("temp", config.primaryColor, this.clickPoint, Path(this.clickPoint), config.activeBrush)
      }
      case MouseEvent.MOUSE_DRAGGED => {
        println("MOUSE_DRAGGED")
        config.activeLayer.removeElement(this.currentElement)
        updateCurrentElement(drawing, eventPoint)
        config.activeLayer.addElement(this.currentElement)
      }
      case MouseEvent.MOUSE_RELEASED => {
        println("MOUSE_RELEASED")
        config.activeLayer.removeElement(this.currentElement)
        updateCurrentElement(drawing, eventPoint)
        config.activeLayer.addElement(currentElement)
      }
      case _ => {
        println("unrecognized mouseEvent type: " + event.getEventType)
      }
    }
  }

  def useAnyway(drawing: Drawing): Unit = {
    println("brushing with tool")
  }
}

object BrushTool extends StrokeTool {
  def setCurrentElement(eventPoint: Point2D, origin: Point2D) = {
    this.currentElement = this.currentElement.copy(path = this.currentElement.path :+ eventPoint, origin = origin)
  }
}

object LineTool extends StrokeTool {
  def setCurrentElement(eventPoint: Point2D, origin: Point2D) = {
    this.currentElement = this.currentElement.copy(path = Path(this.currentElement.path.head, eventPoint), origin = origin)
  }
}