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
        drawing.config.activeLayer.removeElement(rectangle)
        val newElement = TextBox("Lorem ipsum", rectangle.width, rectangle.height, 12, originalConfig.primaryColor, rectangle.origin)
        drawing.config.activeLayer.addElement(newElement)
        drawing.config = drawing.config.copy(selectedElements = Vector(newElement))
      }
      case _ =>
    }
  }

  /*
  // text, id, width, height, color, origin, ... rotation, previousVersion, hidden, deleted
  var currentElement = TextBox("yeet", "temp", 0, 0, rgb(0, 0, 0), new Point2D(0, 0))
  var clickPoint = new Point2D(0, 0)

  def updateCurrentElement(drawing: Drawing, eventPoint: Point2D): Unit = {
    val xDiff =  min(max(0, eventPoint.x) - clickPoint.x, drawing.width - clickPoint.x) // does not yet completely take care of not drawing over the lines with square and circle
    val yDiff = min(max(0, eventPoint.y) - clickPoint.y, drawing.height - clickPoint.y)

    val width = abs(xDiff) // takes care of not drawing over the lines. But should it?
    val height = abs(yDiff)
    val origin = new Point2D(min(abs(clickPoint.x), max(0, eventPoint.x)), min(abs(clickPoint.y), max(0, eventPoint.y)))
    (width, height, origin)

    this.currentElement = this.currentElement.copy(width = width, height = height, origin = origin)
  }

  def use(drawing: Drawing, event: MouseEvent, eventPoint: Point2D): Unit = {
    val config = drawing.config
    event.getEventType match {
      case MouseEvent.MOUSE_PRESSED => {
        println("MOUSE_PRESSED")
        this.clickPoint = eventPoint
        this.currentElement = TextBox("hohohhoo", "temp", 0, 0, config.primaryColor, this.clickPoint)
      }
      case MouseEvent.MOUSE_DRAGGED => {
        println("MOUSE_DRAGGED")
        config.activeLayer.removeElement(this.currentElement)
        updateCurrentElement(drawing, eventPoint)
        config.activeLayer.addElement(this.currentElement)
      }
      case MouseEvent.MOUSE_RELEASED => {
        println("MOUSE_RELEASED")
        config.activeLayer.removeElement(this.currentElement)
        updateCurrentElement(drawing, eventPoint)
        config.activeLayer.addElement(currentElement)
      }
      case _ => {
        println("unrecognized mouseEvent type: " + event.getEventType)
      }
    }
  }*/

}
