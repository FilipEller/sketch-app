package logic
import javafx.scene.input.MouseEvent
import scalafx.geometry.Point2D
import scalafx.scene.paint.Color.rgb

object TextTool extends ShapeTool(Rectangle) {

  override def use(drawing: Drawing, event: MouseEvent, eventPoint: Point2D): Unit = {
    val originalConfig = drawing.config
    drawing.config = drawing.config.copy(primaryColor = rgb(255, 230, 0), secondaryColor = rgb(0, 0, 0, 0))
    super.use(drawing, event, eventPoint)
    drawing.config = originalConfig
    event.getEventType match {
      case MouseEvent.MOUSE_RELEASED => {
        val rectangle = this.currentElement
        val newElement = TextBox("", rectangle.width, rectangle.height, drawing.config.fontSize, originalConfig.primaryColor, rectangle.origin)
        ActionHistory.undo()
        ActionHistory.add(newElement)
        drawing.config.activeLayer.updateElement(rectangle, newElement)
        drawing.config = drawing.config.copy(selectedElements = Vector(newElement))
      }
      case _ =>
    }
  }

}
