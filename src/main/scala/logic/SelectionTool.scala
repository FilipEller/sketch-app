package logic
import javafx.scene.input.MouseEvent
import scalafx.geometry.Point2D

object SelectionTool extends Tool {

  def use(drawing: Drawing, event: MouseEvent, eventPoint: Point2D): Unit = {
    val target = drawing.config.activeLayer.select(eventPoint)
    event.getEventType match {
      case MouseEvent.MOUSE_PRESSED => {
        // Select the topmost Element at the point where the moouse was pressed on the active Layer.
        // If shift is down, the Element is added to the selection as the most recently selected.
        // Else, only the Element becomes the selection.
        // If control is down and the Element is selected, it becomes deselected instead.
        // If mouse is pressed on nothing, all elements become deselected, unless shift or control is down.
        target match {
          case Some(e: Element) if (event.isShiftDown) => {
            drawing.deselect(e)
            drawing.select(drawing.config.selectedElements :+ e)
          }
          case Some(e: Element) if (event.isControlDown) => {
            drawing.deselect(e)
          }
          case Some(e: Element) => {
            drawing.select(e)
          }
          case None if (event.isShiftDown) =>
          case None if (event.isControlDown) =>
          case None => {
            drawing.deselectAll()
          }
        }
      }
      case MouseEvent.MOUSE_DRAGGED => {
        // Keep selecting or deselcting Elements while dragging
        target match {
          case Some(e: Element) if (event.isControlDown) => {
            drawing.deselect(e)
          }
          case Some(e: Element) => {
            drawing.deselect(e)
            drawing.select(drawing.config.selectedElements :+ e)
          }
          case None =>
        }
      }
      case _ =>
    }
  }

}
