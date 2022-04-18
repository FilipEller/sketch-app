package logic

import scalafx.scene.layout.StackPane
import scalafx.geometry.Point2D
import scalafx.scene.canvas.Canvas
import scalafx.scene.input.MouseEvent
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.rgb

import scala.collection.mutable.Buffer

class Drawing(val width: Int, val height: Int, val layers: Buffer[Layer] = Buffer(Layer("Layer 1"))) {

  var config =                        // Default settings
    new Configurations(layers.head,   // active layer
      BrushTool,                      // active tool
      rgb(0, 0, 0),                   // primary color
      rgb(255, 255, 255),             // secondary color
      true,                           // use border
      false,                          // use fill
      new Brush(30, 50),              // active brush
      3,                              // border width
      Vector(),                       // selected elements
      12)                             // font size

  def selectedElements = this.config.selectedElements

  def selectedGroup: Option[ElementGroup] =  this.config.selectedElements.findLast(_.isInstanceOf[ElementGroup]).map(_.asInstanceOf[ElementGroup])

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

  def addLayers(layers: Seq[Layer]): Unit = {
    layers.foreach(addLayer)
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
      if (layer.contains(this.config.activeLayer)) {
        val index = layer.map(this.layers.reverse.indexOf).getOrElse(0)
        val indexToUse = math.min(this.layers.length - 1, math.max(index, 0))
        layer.foreach(removeLayer)
        this.selectLayer(this.layers.reverse(indexToUse))
      } else {
        layer.foreach(removeLayer)
      }
    }
  }

  def renameLayer(layer: Layer, newName: String) = {
    if (this.layers.forall(_.name != newName)) {
      layer.rename(newName)
    } else {
      var index = 2
      val names = this.layers.map(_.name)
      while (names.contains(s"$newName ${index}")) {
        index += 1
      }
      val nameToUse = s"$newName ${index}"
      layer.rename(nameToUse)
    }
  }

  def paintSelection(pane: StackPane) = {
    val borderColor = rgb(255, 230, 0)
    val fillColor = rgb(0, 0, 0)
    val selections =
      this.config.selectedElements.map{
        case e: Shape =>
          new Shape(
            Rectangle,
            e.width + e.borderWidth,
            e.height + e.borderWidth,
            2,
            borderColor,
            fillColor,
            true, false,
            new Point2D(
              e.origin.x - 0.5 * e.borderWidth,
              e.origin.y - 0.5 * e.borderWidth
            ),
            "selection"
          )
        case e: Stroke =>
          new Shape(
            Rectangle,
            e.width,
            e.height,
            2,
            borderColor,
            fillColor,
            true, false,
            new Point2D(
              e.origin.x - 0.5 * e.brush.size,
              e.origin.y - 0.5 * e.brush.size
            ),
            "selection"
          )
        case e: Element =>
          new Shape(
            Rectangle,
            e.width, e.height, 2,
            borderColor, fillColor,
            true, false, e.origin,
            "selection"
          )
      }
    val canvas = new Canvas(width, height)
    selections.foreach(_.paint(canvas))
    pane.children += canvas
  }

  def paint(pane: StackPane): StackPane = {
    this.layers.filter(!_.hidden).foreach(pane.children += _.paint(width, height))
    this.paintSelection(pane)
    pane
  }

  def useTool(event: MouseEvent, localPoint: Point2D) = {
    this.config.activeTool.use(this, event, localPoint)
  }

  def select(element: Element): Unit = {
    this.config = this.config.copy(selectedElements = Seq(element))
  }

  def select(elements: Seq[Element]): Unit = {
    this.config = this.config.copy(selectedElements = elements)
  }

  def selectAdd(element: Element): Unit = {
    this.config = this.config.copy(selectedElements = this.config.selectedElements :+ element)
  }

  def selectAdd(elements: Seq[Element]): Unit = {
    this.config = this.config.copy(selectedElements = this.config.selectedElements ++ elements)
  }

  def deselect(element: Element): Unit = {
    this.config = this.config.copy(selectedElements = this.config.selectedElements.filter(_ != element))
  }

  def selectAll(): Unit = {
    this.config = this.config.copy(selectedElements = this.config.activeLayer.elements.filter(!_.deleted).toSeq)
  }

  def deselectAll(): Unit = {
    this.config = this.config.copy(selectedElements = Seq())
  }

  def undo() = {
    val elements = ActionHistory.undo()
    if (elements.nonEmpty) {
      this.select(this.config.selectedElements.filter(!elements.contains(_)))
      this.deselectAll()
      elements.foreach{
        case group: ElementGroup if (group.previousVersion.isEmpty) => {
          this.selectAdd(group.elements)
        }
        case e: Element => {
          e.previousVersion.foreach(this.selectAdd)
        }
      }

      this.layers.filter( l => elements.forall( e => l.contains(e) ) )
        .foreach( _.restore(elements) )
    }
  }

  def select(point: Point2D): Unit = {
    val selected = this.layers.to(LazyList)
                      .filter(!_.hidden)
                      .map(_.select(point))
                      .find(_.isDefined).flatten
    selected.foreach(this.select)
  }

  def moveSelected(xDiff: Double, yDiff: Double): Seq[Element] = {
    val newElements = this.config.selectedElements.map( _.move(xDiff, yDiff) )
    this.select(newElements)
    this.config.activeLayer
      .update(newElements)
  }

  def contains(element: Element): Boolean = {
    this.layers.exists(_.contains(element))
  }

  def updateSelected(newElements: Seq[Element]): Unit = {
    val toUpdate = newElements.filter(!this.contains(_))
    this.config.activeLayer.update(toUpdate)
    this.select(newElements)
    toUpdate.foreach(ActionHistory.add)
  }

  def groupSelected(): Unit = {
    val selected = this.config.selectedElements
    if (selected.nonEmpty) {
      val layer = this.config.activeLayer
      val index = layer.elements.indexOf(selected.last) - (selected.length - 1)
      val selectedSorted = selected.sortBy(layer.elements.indexOf(_))
      selected.foreach(layer.remove)
      val group = ElementGroup(selectedSorted)
      this.config.activeLayer.addAtIndex(group, index)
      this.select(group)
      ActionHistory.add(group)
    }
  }

  def ungroupSelected(): Unit = {
    if (this.config.selectedElements.nonEmpty) {
      this.selectedGroup match {
        case Some(group: ElementGroup) => {
          val (newGroup, newElements) = this.config.activeLayer.removeElementsFromGroup(group, group.elements)
          ActionHistory.add(newGroup +: newElements)
          this.select(newElements)
        }
        case _ =>
      }
    }
  }

  def addSelectedToGroup(): Unit = {
    if (this.selectedElements.nonEmpty) {
      this.selectedGroup match {
        case Some(group: ElementGroup) => {
          val layer = this.config.activeLayer
          val newGroup = group.addElements(this.selectedElements)
          layer.update(newGroup)
          val newElements = layer.delete(this.selectedElements.filter(_ != group))
          ActionHistory.add(newGroup +: newElements)
          this.select(newGroup)
        }
        case _ =>
      }
    }
  }

  private def updateProperty(elements: Seq[Element], brushSize: Int, hardness: Int, borderWidth: Int, fontSize: Int): Seq[Element] = {
    elements.map{
      case stroke: Stroke if (brushSize >= 0) =>
        stroke.copy(brush = stroke.brush.copy(size = brushSize), previousVersion = Some(stroke))
      case stroke: Stroke if (hardness >= 0) =>
        stroke.copy(brush = stroke.brush.copy(hardness = hardness), previousVersion = Some(stroke))
      case shape: Shape if (borderWidth >= 0) =>
        shape.copy(borderWidth = borderWidth, previousVersion = Some(shape))
      case textBox: TextBox if (fontSize >= 0) =>
        textBox.copy(fontSize = fontSize, previousVersion = Some(textBox))
      case group: ElementGroup => {
        val updated = updateProperty(group.elements, brushSize, hardness, width, fontSize)
        if (updated != group.elements)
          group.copy(elements = updated, previousVersion = Some(group))
        else
          group
      }
      case e: Element => e
    }
  }

  def changeProperty(brushSize: Int = -1, hardness: Int = -1, borderWidth: Int = -1, fontSize: Int = -1) = {
    if (this.config.selectedElements.nonEmpty) {
      val newElements = updateProperty(this.config.selectedElements, brushSize, hardness, borderWidth, fontSize)
      this.updateSelected(newElements)
    } else {
      if (brushSize >= 0) {
        this.config = this.config.copy(activeBrush = this.config.activeBrush.copy(size = brushSize))
      } else if (hardness >= 0) {
        this.config = this.config.copy(activeBrush = this.config.activeBrush.copy(hardness = hardness))
      } else if (borderWidth >= 0) {
        this.config = this.config.copy(borderWidth = borderWidth)
      } else if (fontSize >= 0) {
        this.config = this.config.copy(fontSize = fontSize)
      }
    }
  }

  private def updatePrimaryColor(elements: Seq[Element], color: Color): Seq[Element] = {
    elements.map{
      case e: Shape => e.copy(color = color, previousVersion = Some(e))
      case e: Stroke => e.copy(color = color, previousVersion = Some(e))
      case e: TextBox => e.copy(color = color, previousVersion = Some(e))
      case group: ElementGroup => {
        val updated = updatePrimaryColor(group.elements, color)
        if (updated != group.elements)
          group.copy(elements = updated, color = color, previousVersion = Some(group))
        else
          group
      }
      case e: Element => e
    }
  }

  def changePrimaryColor(color: Color) = {
    if (this.config.selectedElements.nonEmpty) {
      val newElements = this.updatePrimaryColor(this.config.selectedElements, color)
      this.updateSelected(newElements)
    } else {
      this.config = this.config.copy(primaryColor = color)
    }
  }

  private def updateSecondaryColor(elements: Seq[Element], color: Color): Seq[Element] = {
    elements.map{
      case e: Shape => e.copy(fillColor = color, previousVersion = Some(e))
      case group: ElementGroup => {
        val updated = updateSecondaryColor(group.elements, color)
        if (updated != group.elements)
          group.copy(elements = updated, color = color, previousVersion = Some(group))
        else
          group
      }
      case e: Element => e
    }
  }

  def changeSecondaryColor(color: Color) = {
    if (this.config.selectedElements.nonEmpty) {
      val newElements = updateSecondaryColor(this.config.selectedElements, color)
      this.updateSelected(newElements)
    } else {
      this.config = this.config.copy(secondaryColor = color)
    }
  }

  def changeTool(tool: Tool) = {
    this.config = this.config.copy(activeTool = tool)
  }

  def updateUseBorderOrFill(elements: Seq[Element], newValue: Boolean, changeUseBorder: Boolean): Seq[Element] = {
    elements.map{
      case e: Shape => {
        if (changeUseBorder)
          e.copy(useBorder = newValue, previousVersion = Some(e))
        else
          e.copy(useFill = newValue, previousVersion = Some(e))
      }
      case group: ElementGroup => {
        val updated = updateUseBorderOrFill(group.elements, newValue, changeUseBorder)
        if (updated != group.elements)
          group.copy(elements = updated, previousVersion = Some(group))
        else
          group
      }
      case e: Element => e
    }
  }

  def changeUseBorder(newValue: Boolean) = {
    if (this.config.selectedElements.nonEmpty) {
      val newElements = updateUseBorderOrFill(this.config.selectedElements, newValue, true)
      this.updateSelected(newElements)
    } else {
      this.config = this.config.copy(useBorder = newValue)
    }
  }

  def changeUseFill(newValue: Boolean) = {
    if (this.config.selectedElements.nonEmpty) {
      val newElements = updateUseBorderOrFill(this.config.selectedElements, newValue, false)
      this.updateSelected(newElements)
    } else {
      this.config = this.config.copy(useFill = newValue)
    }
  }

  def selectLayer(layer: Layer) = {
    if (layer != this.config.activeLayer) {
      this.deselectAll()
      this.config = this.config.copy(activeLayer = layer)
    }
  }

  def deleteSelected(): Unit = {
    val deleted = this.config.activeLayer.delete(this.config.selectedElements)
    ActionHistory.add(deleted)
    this.deselectAll()
  }

  def removeElementsFromSelectedGroup(names: Seq[String]) = {
    val layer = this.config.activeLayer
    this.selectedGroup match {
      case Some(group: ElementGroup) => {
        if (names.length >= group.elements.length) {
          this.ungroupSelected()
        } else if (layer.contains(group) && group.elements.length > 1) {
          val (newGroup, newElements) = layer.removeElementsFromGroupByName(group, names)
          this.select(newGroup +: newElements)
        }
      }
      case _ =>
    }
  }

  def toggleActiveLayerHidden() = {
    this.deselectAll()
    this.config.activeLayer.hidden = !this.config.activeLayer.hidden
  }

  def renameElement(element: Element, newName: String): Unit = {
    val newElement = this.config.activeLayer.renameElement(element, newName)
    this.select(this.config.selectedElements.filter(_ != element) :+ newElement)
    ActionHistory.add(newElement)
  }

  def rewriteTextBox(textBox: TextBox, newText: String): Element = {
    val layer = this.config.activeLayer
    if (layer.contains(textBox) && !layer.hidden && textBox.text != newText) {
      val newTextBox = textBox.rewrite(newText)
      ActionHistory.add(newTextBox)
      layer.update(newTextBox)
    } else {
      textBox
    }
  }

}
