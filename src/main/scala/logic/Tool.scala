package logic

import javafx.scene.input.MouseEvent
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.rgb

import scala.math.min

trait Tool {
  def use(drawing: Drawing, config: Configurations, mouseEvent: MouseEvent): Unit
}

object RectangleTool extends Tool {
  // type, id, width, height, border width, color, border color, origin)
  var currentElement: Element = Shape(Rectangle, "temp", 0, 0, 0, rgb(0, 0, 0), rgb(0, 0, 0, 0), Point(0, 0))
  var clickPoint = Point(0, 0)

  def getUpdated(event: MouseEvent) = {
    val width = clickPoint.x - event.getX
    val height = clickPoint.y - event.getY
    val origin = Point(min(clickPoint.x, event.getX), min(clickPoint.y, event.getY))
    Shape(Rectangle, "temp", width, height, 0, rgb(0, 0, 0), rgb(0, 0, 0), origin)
  }

  def use(drawing: Drawing, config: Configurations, mouseEvent: MouseEvent) = {
    mouseEvent.getEventType match {
      case MouseEvent.MOUSE_CLICKED => {
        println("clicked")
        this.clickPoint = Point(mouseEvent.getX, mouseEvent.getY)
        this.currentElement = Shape(Rectangle, "temp", 0, 0, 0, rgb(0, 0, 0), rgb(0, 0, 0), this.clickPoint)
      }
      case MouseEvent.MOUSE_DRAGGED => {
        println("dragging")
        config.activeLayer.removeElement(this.currentElement)
        this.currentElement = getUpdated(mouseEvent)
        config.activeLayer.addElement(this.currentElement)
        /*val workingLayer = Layer("temp")
        workingLayer.addElement(this.currentElement)
        drawing.removeLayer(
        drawing.addLayer(workingLayer)*/
      }
      case MouseEvent.MOUSE_RELEASED => {
        println("released")
        this.currentElement = getUpdated(mouseEvent)
        config.activeLayer.addElement(currentElement)
      }
    }
  }
}