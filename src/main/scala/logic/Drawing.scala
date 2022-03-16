package logic

import scalafx.scene.layout.StackPane
import scalafx.Includes._
import scalafx.geometry.Point2D
import scalafx.scene.canvas.Canvas
import scalafx.scene.input.{MouseDragEvent, MouseEvent}
import scalafx.scene.paint.Color.rgb

import scala.collection.mutable

class Drawing(val width: Int, val height: Int) {

  val backgroundLayer = Layer("Layer 1")
  var currentImage = new StackPane
  val layers = mutable.Buffer[Layer](backgroundLayer)
  var config = new Configurations(layers.head, RectangleTool, rgb(0, 0, 0), rgb(0, 0, 0), 1, None, 12)  // Default settings

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

  def findLayer(name: String): Option[Layer] = {
    this.layers.find(_.name == name)
  }

  def removeLayer(layer: Layer): Unit = {
    this.layers -= layer
  }

  def removeLayer(name: String): Unit = {
    val layer = this.findLayer(name)
    layer.foreach(removeLayer)
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
    println(pane.children)

    println(pane.children)
    this.layers.foreach(pane.children += _.paint(width, height))
    this.currentImage = pane
    pane
  }

  /*
  def useTool(event: MouseDragEvent, pane: StackPane) = {
    this.config.activeTool.use(this, this.config, event, )
    this.paint(pane)
  }
   */

}
