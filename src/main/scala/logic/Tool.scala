package logic

import javafx.scene.input.MouseEvent
import javafx.scene.input.MouseDragEvent
import scalafx.geometry.Point2D
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.rgb

import scala.math.{abs, min}

trait Tool {
  def use(drawing: Drawing, config: Configurations, event: MouseDragEvent): Unit
}

object RectangleTool extends Tool {
  // type, id, width, height, border width, color, border color, origin)
  var currentElement: Element = Shape(Rectangle, "temp", 0, 0, 0, rgb(0, 0, 0), rgb(0, 0, 0, 0), new Point2D(0, 0))
  var clickPoint = new Point2D(0, 0)

  def getUpdated(drawing: Drawing, config: Configurations, event: MouseDragEvent) = {
    val width = abs(clickPoint.x - event.getX)
    val height = abs(clickPoint.y - event.getY)
    val origin = new Point2D(min(clickPoint.x, event.getX), min(clickPoint.y, event.getY))
    Shape(Rectangle, "temp", width, height, 0, config.primaryColor, config.secondaryColor, origin)
  }

  def use(drawing: Drawing, config: Configurations, event: MouseDragEvent) = {
    event.getEventType match {
      case MouseDragEvent.MOUSE_DRAG_ENTERED => {
        println("MOUSE_DRAG_ENTERED")
        this.clickPoint = new Point2D(drawing.currentImage.sceneToLocal(new scalafx.geometry.Point2D(event.getX, event.getY)))  // javafx.geometry.Point2D cast to Scalafx through making a new object
        println("clicked at " + clickPoint)
        this.currentElement = Shape(Rectangle, "temp", 0, 0, 0, config.primaryColor, config.secondaryColor, this.clickPoint)
      }
      case MouseDragEvent.MOUSE_DRAG_OVER => {
        println("MOUSE_DRAG_OVER")
        config.activeLayer.removeElement(this.currentElement)
        this.currentElement = getUpdated(drawing, config, event)
        println("dragging at " + event.getX + ", " + event.getY)
        config.activeLayer.addElement(this.currentElement)
        /*val workingLayer = Layer("temp")
        workingLayer.addElement(this.currentElement)
        drawing.removeLayer(
        drawing.addLayer(workingLayer)*/
      }
      case MouseDragEvent.MOUSE_DRAG_EXITED => {
        println("MOUSE_DRAG_EXITED")
        config.activeLayer.removeElement(this.currentElement)
        this.currentElement = getUpdated(drawing, config, event)
        println("released at " + event.getX + ", " + event.getY)
        config.activeLayer.addElement(currentElement)
        // config.activeLayer.addElement(Shape(Rectangle, "test", 30, 40, 0, rgb(40, 255, 40), rgb(0, 0, 0, 0), Point(50, 50), 0)) // test rectangle
      }
      case _ => {
        println("unrecognized mouseDragEvent type: " + event.getEventType)
      }
    }
  }
}