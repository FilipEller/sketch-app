package logic
import javafx.scene.input.MouseEvent
import scalafx.geometry.Point2D

object TransformTool extends Tool {

  var isActive = false
  var lastPoint = new Point2D(0, 0)

  def move(drawing: Drawing, eventPoint: Point2D): Vector[Element] = {
    val xDiff = eventPoint.x - lastPoint.x
    val yDiff = eventPoint.y - lastPoint.y
    val updatedElements = drawing.moveSelected(xDiff, yDiff)
    updatedElements.foreach( ActionHistory.add(_) )
    updatedElements
    // currently moving makes a long chain of previous versions because each little movement creates new elements.
    // thus undoing has to go through all the same versions
    // fixing this would need enforcing a previous version while moving an element.
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
        if (this.isActive) {
          this.move(drawing, eventPoint)
          this.lastPoint = eventPoint
        }
      }
      case MouseEvent.MOUSE_RELEASED => {
        println("MOUSE_RELEASED")
        if (this.isActive) {
          this.move(drawing, eventPoint)
        }
        this.isActive = false
      }
      case _ => {
        println("unrecognized mouseEvent type: " + event.getEventType)
      }
    }
  }
}
