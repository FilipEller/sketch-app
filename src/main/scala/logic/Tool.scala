package logic

import javafx.scene.input.MouseEvent
import scalafx.geometry.Point2D

// A simple interface for all Tools
trait Tool {
  def use(drawing: Drawing, event: MouseEvent, eventPoint: Point2D): Unit
}