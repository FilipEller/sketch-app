package logic

import javafx.scene.input.MouseEvent
import scalafx.geometry.Point2D
import scalafx.scene.paint.Color.rgb

import scala.math.{abs, min, max}

trait Tool {
  def use(drawing: Drawing, event: MouseEvent, eventPoint: Point2D): Unit
}