package logic

import javafx.scene.input.MouseEvent
import scalafx.geometry.Point2D
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.rgb

import scala.math.min

object BrushTool extends Tool {

  var currentElement = Stroke( "temp", rgb(0, 0, 0), new Point2D(0, 0), Vector(new Point2D(0, 0)), new Brush(1, 100))
  var clickPoint = new Point2D(0, 0)

    def updateCurrentElement(drawing: Drawing, eventPoint: Point2D): Unit = {
      val originX = min(this.currentElement.origin.x, eventPoint.x)
      val originY = min(this.currentElement.origin.y, eventPoint.y)
      this.currentElement = this.currentElement.copy(path = this.currentElement.path :+ eventPoint)
  }

  def use(drawing: Drawing, event: MouseEvent, eventPoint: Point2D): Unit = {
    println("brushing")
  }
}
