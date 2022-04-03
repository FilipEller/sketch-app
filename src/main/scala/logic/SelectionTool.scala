package logic
import javafx.scene.input.MouseEvent
import scalafx.geometry.Point2D

object SelectionTool extends Tool {

  def use(drawing: Drawing, event: MouseEvent, eventPoint: Point2D): Unit = {
    event.getEventType match {
      case MouseEvent.MOUSE_PRESSED => {
        val target = drawing.config.activeLayer.select(eventPoint)
        if (event.isShiftDown) {
          target.foreach( e => drawing.config = drawing.config.copy(selectedElements = drawing.config.selectedElements.filter( _ != e) :+ e))
        } else {
          drawing.config = drawing.config.copy(selectedElements = target.toSeq)
        }
        println("selected " + drawing.config.selectedElements)
      }
      case _ =>
    }
  }

}
