package logic
import javafx.scene.input.MouseEvent
import scalafx.geometry.Point2D
import scalafx.scene.paint.Color.rgb

object TextTool extends ShapeTool(Rectangle) {

  override def use(drawing: Drawing, event: MouseEvent, eventPoint: Point2D): Unit = {

    val originalConfig = drawing.config
    drawing.deselectAll()
    drawing.changePrimaryColor(rgb(255, 230, 0))
    drawing.changeSecondaryColor(rgb(0, 0, 0, 0))
    drawing.changeProperty(borderWidth = 2)
    super.use(drawing, event, eventPoint)
    drawing.changePrimaryColor(originalConfig.primaryColor)
    drawing.changeSecondaryColor(originalConfig.secondaryColor)
    drawing.changeProperty(borderWidth = originalConfig.borderWidth)

    event.getEventType match {
      case MouseEvent.MOUSE_RELEASED => {
        val rectangle = this.currentElement
        val newElement =
          TextBox("", rectangle.width, rectangle.height, drawing.config.fontSize,
            originalConfig.primaryColor, rectangle.origin)
        ElementHistory.undo()
        ElementHistory.add(newElement)
        drawing.config.activeLayer.update(rectangle, newElement)
        drawing.select(newElement)
      }
      case _ =>
    }
  }

}
