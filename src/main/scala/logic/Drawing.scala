package logic

import scalafx.scene.layout.StackPane
import scalafx.Includes._
import scalafx.scene.input.MouseEvent
import scalafx.scene.paint.Color.rgb

import scala.collection.mutable

class Drawing(val width: Int, val height: Int) {

  val layers = mutable.Buffer[Layer](Layer("Layer 1"))

  var config = new Configurations(layers.head, RectangleTool, rgb(0, 0, 0), rgb(255, 255, 255), 1, None, 12)  // Default settings

  def addLayer(): Unit = {
    var index = this.layers.length + 1
    val names = this.layers.map(_.name)
    while (names.contains(s"Layer ${index}")) {
      index += 1
    }

    val newName = s"Layer ${index}"
    this.layers += Layer(newName)
  }

  def addLayer(layer: Layer): Unit = {
    if (!this.layers.map(_.name).contains(layer.name)) {
      this.layers += layer
    }
  }

  def removeLayer(layer: Layer): Unit = {
    this.layers -= layer
  }

  def renameLayer(layer: Layer, newName: String) = {
    if (this.layers.forall(_.name != newName) && this.layers.contains(layer)) {
      layer.rename(newName)
      true
    } else {
      false
    }
  }

  def paint(pane: StackPane): StackPane = {
    pane.children.foreach(pane.children -= _)
    this.layers.foreach(pane.children += _.paint(width, height))
    pane
  }

  def useTool(mouseEvent: MouseEvent, pane: StackPane) = {
    this.config.activeTool.use(this, this.config, mouseEvent)
    this.paint(pane)
  }

}
