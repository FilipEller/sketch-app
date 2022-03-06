package logic

import scalafx.scene.layout.StackPane
import scalafx.Includes._
import scala.collection.mutable

class Drawing(val width: Int, val height: Int) {

  val layers = mutable.Buffer[Layer](Layer("Layer 1"))

  def addLayer(): Unit = {
    var index = this.layers.length + 1
    val names = this.layers.map(_.name)
    while (names.contains(s"Layer ${index}")) {
      index += 1
    }

    val newName = s"Layer ${index}"
    this.layers += Layer(newName)
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

}
