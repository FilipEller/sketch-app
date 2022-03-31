package logic
import javafx.scene.input.MouseEvent
import scalafx.geometry.Point2D
import scalafx.scene.paint.Color.rgb

import scala.math.{abs, max, min}

object TextTool extends ShapeTool(Rectangle) {

  override def use(drawing: Drawing, event: MouseEvent, eventPoint: Point2D): Unit = {
    val originalConfig = drawing.config
    drawing.config = drawing.config.copy(primaryColor = rgb(0, 0, 0, 0), secondaryColor = rgb(0, 0, 0))
    super.use(drawing, event, eventPoint)
    drawing.config = originalConfig
    event.getEventType match {
      case MouseEvent.MOUSE_RELEASED => {
        val rectangle = this.currentElement
        val newElement = TextBox("Lorem ipsum", rectangle.width, rectangle.height, 12, originalConfig.primaryColor, rectangle.origin)
        drawing.config.activeLayer.updateElement(rectangle, newElement)
        drawing.config = drawing.config.copy(selectedElements = Vector(newElement))
      }
      case _ =>
    }
  }

}
