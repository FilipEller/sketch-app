package logic

import scalafx.scene.layout.StackPane
import scalafx.Includes._
import scalafx.geometry.Point2D
import scalafx.scene.input.{MouseDragEvent, MouseEvent}
import scalafx.scene.paint.Color.rgb

import scala.collection.mutable

class Drawing(val width: Int, val height: Int) {

  val backgroundLayer = Layer("Layer 1")
  val background = Shape(Rectangle, "background", width, height, 0, rgb(255, 255, 255), rgb(0, 0, 0, 0), new Point2D(0, 0), 0)
  backgroundLayer.addElement(background)
  var currentImage = new StackPane

  val layers = mutable.Buffer[Layer](backgroundLayer)

  var config = new Configurations(layers.head, RectangleTool, rgb(255, 50, 50), rgb(50, 255, 50), 1, None, 12)  // Default settings

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
    this.layers.foreach(pane.children += _.paint(width, height))
    this.currentImage = pane
    pane
  }

  def useTool(event: MouseDragEvent, pane: StackPane) = {
    this.config.activeTool.use(this, this.config, event)
    this.paint(pane)
  }

}
