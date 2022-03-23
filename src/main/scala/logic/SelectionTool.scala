package logic
import javafx.scene.input.MouseEvent
import scalafx.geometry.Point2D

object SelectionTool extends Tool {

  def use(drawing: Drawing, event: MouseEvent, eventPoint: Point2D): Unit = {
    event.getEventType match {
      case MouseEvent.MOUSE_PRESSED => {
        val target = drawing.config.activeLayer.select(eventPoint)
        drawing.config = drawing.config.copy(selectedElement = target)
        println("selected " + drawing.config.selectedElement)
      }
      case _ =>
    }
  }

}
