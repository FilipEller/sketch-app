package logic

import javafx.scene.input.MouseEvent
import javafx.scene.input.MouseDragEvent
import scalafx.geometry.Point2D
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.rgb

import scala.math.{abs, min, max}

trait Tool {
  def use(drawing: Drawing, config: Configurations, event: MouseEvent, eventPoint: Point2D): Unit
}

object RectangleTool extends Tool {
  // type, id, width, height, border width, color, border color, origin)
  var currentElement: Element = Shape(Rectangle, "temp", 0, 0, 0, rgb(0, 0, 0), rgb(0, 0, 0, 0), new Point2D(0, 0))
  var clickPoint = new Point2D(0, 0)

  def getUpdated(drawing: Drawing, config: Configurations, eventPoint: Point2D) = {
    val width = abs(clickPoint.x - max(0, eventPoint.x))
    val height = abs(clickPoint.y - max(0, eventPoint.y))
    val origin = new Point2D(min(abs(clickPoint.x), max(0, eventPoint.x)), min(abs(clickPoint.y), max(0, eventPoint.y)))
    println("clickPoint: " + clickPoint)
    println("eventPoint: " + eventPoint)
    println("width: " + width)
    println("height: " + height)
    println("origin: " + origin)
    Shape(Rectangle, "temp", width, height, 0, config.primaryColor, config.secondaryColor, origin)
  }

  def use(drawing: Drawing, config: Configurations, event: MouseEvent, eventPoint: Point2D): Unit = {

    println(drawing.currentImage.getLayoutBounds)
    // val targetOffset = new Point2D(325, 56)
     // No idea where these Int literals come from
    event.getEventType match {
      case MouseEvent.MOUSE_PRESSED => {
        println("MOUSE_PRESSED")
        this.clickPoint = eventPoint  // javafx.geometry.Point2D cast to Scalafx through making a new object
        println("clicked at " + clickPoint)
        this.currentElement = Shape(Rectangle, "temp", 0, 0, 0, config.primaryColor, config.secondaryColor, this.clickPoint)
      }
      case MouseEvent.MOUSE_DRAGGED => { // This is actually called when going over the border not when dragging
        println("MOUSE_DRAGGED")
        config.activeLayer.removeElement(this.currentElement)
        this.currentElement = getUpdated(drawing, config, eventPoint)
        println("dragging at " + eventPoint)
        config.activeLayer.addElement(this.currentElement)
        /*val workingLayer = Layer("temp")
        workingLayer.addElement(this.currentElement)
        drawing.removeLayer(
        drawing.addLayer(workingLayer)*/
      }
      case MouseEvent.MOUSE_RELEASED => {
        println("MOUSE_RELEASED")
        config.activeLayer.removeElement(this.currentElement)
        this.currentElement = getUpdated(drawing, config, eventPoint)
        println("released at " + eventPoint)
        config.activeLayer.addElement(currentElement)
      }
      case _ => {
        println("unrecognized mouseDragEvent type: " + event.getEventType)
      }
    }
    println(drawing.layers.head.currentImage.get.getLayoutBounds)
  }
}