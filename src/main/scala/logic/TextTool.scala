package logic
import javafx.scene.input.MouseEvent
import scalafx.geometry.Point2D
import scalafx.scene.paint.Color.rgb

// The TextTool draws a Rectangle, then replaces it with a TextBox
object TextTool extends ShapeTool(Rectangle) {

  override def use(drawing: Drawing, event: MouseEvent, eventPoint: Point2D): Unit = {

    // While dragging, a yellow rectangle is drawn.
    val originalConfig = drawing.config
    drawing.deselectAll()
    // Change the config to use yellow borders and no fill
    drawing.changePrimaryColor(rgb(255, 230, 0))
    drawing.changeSecondaryColor(rgb(0, 0, 0, 0))
    drawing.changeProperty(borderWidth = 2)
    // Call ShapeTool(Rectangle).use to draw a yellow rectangle
    super.use(drawing, event, eventPoint)
    // Revert to the original config
    drawing.changePrimaryColor(originalConfig.primaryColor)
    drawing.changeSecondaryColor(originalConfig.secondaryColor)
    drawing.changeProperty(borderWidth = originalConfig.borderWidth)

    event.getEventType match {
      case MouseEvent.MOUSE_RELEASED => {
        // Only when the mouse is released, a TextBox replaces the yellow rectangle
        val rectangle = this.currentElement
        if (rectangle.width > 0 && rectangle.height > 0) {
          val newElement =
            TextBox("", rectangle.width, rectangle.height, drawing.config.fontSize,
              originalConfig.primaryColor, rectangle.origin)
          ElementHistory.undo()
          ElementHistory.add(newElement)
          drawing.config.activeLayer.update(rectangle, newElement)
          // The TextBox is selected, so it can be written in immediately
          drawing.select(newElement)
        }
      }
      case _ =>
    }
  }

}
