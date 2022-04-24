package logic

import javafx.scene.input.MouseEvent
import scalafx.geometry.Point2D
import scalafx.scene.paint.Color.rgb

import scala.math.min

sealed abstract class StrokeTool extends Tool {

  protected var currentElement =
    new Stroke(rgb(0, 0, 0), new Point2D(0, 0), Path(new Point2D(0, 0)), new Brush(1, 100), "")
  private var clickPoint = new Point2D(0, 0)

  protected def setCurrentElement(eventPoint: Point2D, origin: Point2D): Unit

  private def updateCurrentElement(drawing: Drawing, eventPoint: Point2D): Element = {
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
        // Start a new Stroke
        this.clickPoint = eventPoint
        this.currentElement =
          Stroke(config.primaryColor, this.clickPoint, Path(this.clickPoint), config.activeBrush)
        layer.add(this.currentElement)
      }
      case MouseEvent.MOUSE_DRAGGED => {
        // Update the Stroke while dragging
        layer.update(this.currentElement, this.updateCurrentElement(drawing, eventPoint))
      }
      case MouseEvent.MOUSE_RELEASED => {
        // Finalize the Stroke
        layer.update(this.currentElement, this.updateCurrentElement(drawing, eventPoint))
        ElementHistory.add(this.currentElement)
      }
      case _ => {
        println("unrecognized mouseEvent type: " + event.getEventType)
      }
    }
  }
}

object BrushTool extends StrokeTool {
  protected def setCurrentElement(eventPoint: Point2D, origin: Point2D): Unit = {
    // Connect the new Point to the Stroke by making a line to it from the last point
    this.currentElement = this.currentElement.copy(
      path = this.currentElement.path :+ eventPoint,
      origin = origin
    )
  }
}

object LineTool extends StrokeTool {
  protected def setCurrentElement(eventPoint: Point2D, origin: Point2D): Unit = {
    // Only makes a line between the first Point and the new Point
    this.currentElement = this.currentElement.copy(
      path = Path(this.currentElement.path.head, eventPoint),
      origin = origin
    )
  }
}