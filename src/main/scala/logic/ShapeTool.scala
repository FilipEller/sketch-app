package logic

import javafx.scene.input.MouseEvent
import scalafx.geometry.Point2D
import scalafx.scene.paint.Color.rgb

import scala.math.{abs, min}

class ShapeTool(shapeType: ShapeType) extends Tool {
  protected var currentElement =
    new Shape(this.shapeType, 0, 0, 0, rgb(0, 0, 0), rgb(0, 0, 0, 0), true, true, new Point2D(0, 0), "")
  private var clickPoint = new Point2D(0, 0)

  private def updateCurrentElement(drawing: Drawing, eventPoint: Point2D): Element = {
    val xDiff = eventPoint.x - clickPoint.x
    val yDiff = eventPoint.y - clickPoint.y

    val (width: Double, height, origin) = this.shapeType match {
      case Rectangle | Ellipse => {
        val width = abs(xDiff)
        val height = abs(yDiff)
        val origin = new Point2D(min(clickPoint.x, eventPoint.x), min(clickPoint.y, eventPoint.y))
        (width, height, origin)
      }
      case _ => { // Square and Circle
        val smallerDiff = if (abs(xDiff) > abs(yDiff)) xDiff else yDiff
        val width = abs(smallerDiff)
        val height = width
        val originX = min(clickPoint.x, clickPoint.x + xDiff.sign * width)
        val originY = min(clickPoint.y, clickPoint.y + yDiff.sign * height)
        val origin = new Point2D(originX, originY)
        (width, height, origin)
      }
    }
    this.currentElement =
      this.currentElement.copy(width = width, height = height, origin = origin)
    this.currentElement
  }

  def use(drawing: Drawing, event: MouseEvent, eventPoint: Point2D): Unit = {
    val config = drawing.config
    val layer = config.activeLayer
    event.getEventType match {
      case MouseEvent.MOUSE_PRESSED => {
        this.clickPoint = eventPoint
        this.currentElement =
          Shape(this.shapeType, 0, 0, config.borderWidth,
            config.primaryColor, config.secondaryColor,
            config.useBorder, config.useFill, this.clickPoint)
        layer.add(this.currentElement)
      }
      case MouseEvent.MOUSE_DRAGGED => {
        layer.update(this.currentElement, this.updateCurrentElement(drawing, eventPoint))
      }
      case MouseEvent.MOUSE_RELEASED => {
        layer.update(this.currentElement, this.updateCurrentElement(drawing, eventPoint))
        if (this.currentElement.width > 0 && this.currentElement.height > 0) {
          ElementHistory.add(this.currentElement)
        } else {
          layer.remove(this.currentElement)
        }
      }
      case _ => {
        println("unrecognized mouseEvent type: " + event.getEventType)
      }
    }
  }
}

object RectangleTool extends ShapeTool(Rectangle)

object SquareTool extends ShapeTool(Square)

object EllipseTool extends ShapeTool(Ellipse)

object CircleTool extends ShapeTool(Circle)
