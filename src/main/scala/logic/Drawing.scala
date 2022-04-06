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
  var config =                        // Default settings
    new Configurations(layers.head,   // active layer
      RectangleTool,                  // active tool
      rgb(0, 0, 0),                   // primary color
      rgb(255, 255, 255),             // secondary color
      true,                           // use border
      false,                          // use fill
      new Brush(30, 50),              // active brush
      3,                              // border width
      Vector(),                       // selected elements
      12)                             // font size

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

  def paintSelection(pane: StackPane) = {
    val borderColor = rgb(255, 230, 0)
    val fillColor = rgb(0, 0, 0)
    val selections =
      this.config.selectedElements.map( {
        case e: Shape => new Shape(Rectangle, e.width + e.borderWidth, e.height + e.borderWidth, 2, borderColor, fillColor, true, false, new Point2D(e.origin.x - 0.5 * e.borderWidth, e.origin.y - 0.5 * e.borderWidth), "selection")
        case e: Stroke => new Shape(Rectangle, e.width, e.height, 2, borderColor, fillColor, true, false, new Point2D(e.origin.x - 0.5 * e.brush.size, e.origin.y - 0.5 * e.brush.size), "selection")
        case e: Element => new Shape(Rectangle, e.width, e.height, 2, borderColor, fillColor, true, false, e.origin, "selection")
      } )
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

  def undo() = {
    val elementOption = ActionHistory.undo()
    elementOption match {
      case Some(element: Element) => {
        this.config = this.config.copy(selectedElements = Seq())
        element.previousVersion.foreach( e => this.config = this.config.copy(selectedElements = Seq(e)) )
        this.layers.filter( _.contains(element) )
          .foreach( _.restoreElement(element) )
      }
      case _ => {
        println("history is empty")
      }
    }
  }

  def select(point: Point2D) = {
    val selected = this.layers.to(LazyList)
                      .filter(!_.hidden)
                      .map(_.select(point))
                      .find(_.isDefined).flatten
    selected.foreach( e => this.config = this.config.copy(selectedElements = Vector(e)) )
  }

  def moveSelected(xDiff: Double, yDiff: Double): Seq[Element] = {
    val newElements = this.config.selectedElements.map( _.move(xDiff, yDiff) )
    this.config = this.config.copy(selectedElements = newElements)
    this.config.activeLayer
      .updateElements(newElements)
  }

  def updateSelected(elements: Seq[Element]): Unit = {
    this.config.activeLayer.updateElements(elements)
    this.config = this.config.copy(selectedElements = elements)
    elements.foreach(ActionHistory.add)
  }

  def groupSelected(): Element = {
    this.config.selectedElements.foreach( this.config.activeLayer.removeElement(_) )
    val group = ElementGroup(this.config.selectedElements)
    this.config.activeLayer.addElement(group)
    this.config = this.config.copy(selectedElements = Seq(group))
    group
  }

}
