package logic

import scalafx.scene.layout.StackPane
import scalafx.Includes._
import scalafx.geometry.Point2D
import scalafx.scene.canvas.Canvas
import scalafx.scene.input.{MouseDragEvent, MouseEvent}
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.rgb

import scala.collection.mutable

class Drawing(val width: Int, val height: Int) {

  val backgroundLayer = Layer("Layer 1")
  var currentImage = new StackPane
  val layers = mutable.Buffer[Layer](backgroundLayer)
  var config = new Configurations(layers.head, RectangleTool, rgb(0, 0, 0), rgb(255, 255, 255), true, false, new Brush(30, 50), Vector(), 12)  // Default settings

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
    if (this.layers.length > 1) {
      val layer = this.findLayer(name)
      // should also change config's selected layer if removed was selected
      // though Controller already makes sure another layer is selected
      layer.foreach(removeLayer)
    }
  }

  def renameLayer(layer: Layer, newName: String) = {
    if (this.layers.forall(_.name != newName) && this.layers.contains(layer)) {
      layer.rename(newName)
      true
    } else {
      false
    }
  }

  def moveSelected(xDiff: Double, yDiff: Double) = {
    val newElements = this.config.selectedElements.map( _.move(xDiff, yDiff) )
    this.config = this.config.copy(selectedElements = newElements)
    this.config.activeLayer.updateElements(newElements)
  }

  def fillColor: Color = new Color(this.config.secondaryColor.opacity(if (this.config.useFill) this.config.secondaryColor.opacity else 0))
  def borderColor: Color = new Color(config.primaryColor.opacity(if (config.useBorder) config.primaryColor.opacity else 0 ))

  def paintSelection(pane: StackPane) = {
    val selections = this.config.selectedElements
                      .map( e => new Shape(Rectangle, e.width, e.height, 2, rgb(0, 0, 0, 0), rgb(255, 230, 0), e.origin, "selection") )
    val canvas = new Canvas(width, height)
    selections.foreach(_.paint(canvas))
    pane.children += canvas
  }

  def paint(pane: StackPane): StackPane = {
    println(pane.children)
    this.layers.foreach(pane.children += _.paint(width, height))
    this.paintSelection(pane)
    this.currentImage = pane
    pane
  }

  def useTool(event: MouseEvent, localPoint: Point2D) = {
    this.config.activeTool.use(this, event, localPoint)
  }

  def select(point: Point2D) = {
    val selected = this.layers.to(LazyList).filter(!_.hidden).map(_.select(point)).find(_.isDefined).flatten
    selected.foreach( e => this.config = this.config.copy(selectedElements = Vector(e)) )
  }

}
