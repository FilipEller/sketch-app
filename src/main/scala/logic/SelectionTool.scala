package logic
import javafx.scene.input.MouseEvent
import scalafx.geometry.Point2D

object SelectionTool extends Tool {

  def use(drawing: Drawing, event: MouseEvent, eventPoint: Point2D): Unit = {
    event.getEventType match {
      case MouseEvent.MOUSE_PRESSED => {
        val target = drawing.config.activeLayer.select(eventPoint)
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
          case None => {
            drawing.deselectAll()
          }
        }
        println("selected " + drawing.config.selectedElements)
      }
      case _ =>
    }
  }

}
