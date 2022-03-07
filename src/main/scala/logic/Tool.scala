package logic

import javafx.scene.input.MouseEvent

trait Tool {
  def use(layer: Layer, mouseEvent: MouseEvent): Unit
}

object RectangleTool extends Tool {
  def use(layer: Layer, mouseEvent: MouseEvent) = {
    println("using rectangle tool")
  }
}