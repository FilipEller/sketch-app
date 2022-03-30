package logic
import javafx.scene.input.MouseEvent
import scalafx.geometry.Point2D

object TransformTool extends Tool {

  var isActive = false
  var lastPoint = new Point2D(0, 0)

  def move(drawing: Drawing, eventPoint: Point2D): Unit = {
    if (this.isActive) {
          val xDiff = eventPoint.x - lastPoint.x
          val yDiff = eventPoint.y - lastPoint.y
          val newElements = drawing.config.selectedElements.map( _.move(xDiff, yDiff) )
          drawing.config = drawing.config.copy(selectedElements = newElements)
          drawing.config.activeLayer.updateElements(newElements)
    }
  }

  def use(drawing: Drawing, event: MouseEvent, eventPoint: Point2D): Unit = {
    event.getEventType match {
      case MouseEvent.MOUSE_PRESSED => {
        this.isActive = drawing.config.selectedElements.exists( _.collidesWith(eventPoint) )
        this.lastPoint = eventPoint
        println("MOUSE_PRESSED")
      }
      case MouseEvent.MOUSE_DRAGGED => {
        println("MOUSE_DRAGGED")
        this.move(drawing, eventPoint)
        this.lastPoint = eventPoint
      }
      case MouseEvent.MOUSE_RELEASED => {
        println("MOUSE_RELEASED")
        this.move(drawing, eventPoint)
        this.lastPoint = eventPoint
        this.isActive = false
      }
      case _ => {
        println("unrecognized mouseEvent type: " + event.getEventType)
      }
    }
  }
}
