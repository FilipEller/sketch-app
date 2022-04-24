package logic

import scalafx.scene.layout.StackPane
import scalafx.geometry.Point2D
import scalafx.scene.canvas.Canvas
import scalafx.scene.input.MouseEvent
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.rgb

import scala.collection.mutable.Buffer

class Drawing(val width: Int = 1000, val height: Int = 600, private val mLayers: Buffer[Layer] = Buffer(Layer("Layer 1"))) {

  val defaultConfig =
    new Configurations(layers.head,   // active layer
      BrushTool,                      // active tool
      rgb(0, 0, 0),                   // primary color
      rgb(255, 255, 255),             // secondary color
      true,                           // use border
      false,                          // use fill
      new Brush(20, 50),              // active brush
      3,                              // border width
      Seq(),                          // selected elements
      12)                             // font size

  private var mConfig = this.defaultConfig

  def layers = this.mLayers.toSeq
  def config = this.mConfig
  def selectedElements = this.config.selectedElements
  def activeLayer = this.config.activeLayer

  // The most recently selected Group in selected Elements
  def selectedGroup: Option[ElementGroup] =
    this.selectedElements
      .findLast(_.isInstanceOf[ElementGroup])
      .map(_.asInstanceOf[ElementGroup])

  def selectLayer(layer: Layer): Unit = {
    if (layer != this.activeLayer && this.layers.contains(layer)) {
      this.deselectAll()
      this.mConfig = this.config.copy(activeLayer = layer)
    }
  }

  // Add an empty Layer
  def addLayer(): Unit = {
    var index = this.layers.length + 1
    val names = this.layers.map(_.name)
    while (names.contains(s"Layer ${index}")) {
      index += 1
    }

    val newName = s"Layer ${index}"
    this.mLayers += Layer(newName)
  }

  // Add an existing Layer. Used when loading a Drawing from a file
  def addLayer(layer: Layer): Unit = {
    if (!this.layers.map(_.name).contains(layer.name)) {
      this.mLayers += layer
    }
  }

  def findLayer(name: String): Option[Layer] = {
    this.layers.find(_.name == name)
  }

  private def removeLayer(layer: Layer): Unit = {
    this.mLayers -= layer
  }

  // Remove a layer by its name
  // It will not be removed if it is the last remaining Layer
  // If active Layer is the one to be removed, another Layer is selected
  def removeLayer(name: String): Unit = {
    if (this.layers.length > 1) {
      val layer = this.findLayer(name)
      if (layer.contains(this.activeLayer)) {
        val index = layer.map(this.layers.reverse.indexOf).getOrElse(0)
        layer.foreach(removeLayer)
        val indexToUse = math.min(this.layers.length - 1, math.max(index, 0))
        this.selectLayer(this.layers.reverse(indexToUse))
      } else {
        layer.foreach(removeLayer)
      }
    }
  }

  // Rename a Layer but add an index if the new name is already in use
  def renameLayer(layer: Layer, newName: String) = {
    if (this.layers.contains(layer)) {
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
  }

  // Hide or unhide the active Layer
  def toggleActiveLayerHidden() = {
    this.deselectAll()
    this.activeLayer.isHidden = !this.activeLayer.isHidden
  }

  // Paint a yellow rectangle around the selected Elements
  private def paintSelection(pane: StackPane) = {
    val rectangle =
      new Shape(
        Rectangle,
        0, 0, 2,
        rgb(255, 230, 0), rgb(0, 0, 0),
        true, false, new Point2D(0, 0),
        "selection"
      )
    val selections =
      this.selectedElements.map{
        case e: Shape =>
          rectangle.copy(
            width = e.width + e.borderWidth,
            height = e.height + e.borderWidth,
            origin = new Point2D(
              e.origin.x - 0.5 * e.borderWidth,
              e.origin.y - 0.5 * e.borderWidth
            ),
          )
        case e: Stroke =>
          rectangle.copy(
            width = e.width,
            height = e.height,
            origin = new Point2D(
              e.origin.x - 0.5 * e.brush.size,
              e.origin.y - 0.5 * e.brush.size
            )
          )
        case e: Element =>
          rectangle.copy(
            width = e.width,
            height = e.height,
            origin = e.origin
          )
      }
    val canvas = new Canvas(width, height)
    selections.foreach(_.paint(canvas))
    pane.children += canvas
  }

  // Paint each of this Drawing's Layers on a StackPane
  def paint(pane: StackPane): Unit = {
    this.layers.filter(!_.isHidden).foreach(pane.children += _.paint(new Canvas(width, height)))
    this.paintSelection(pane)
  }

  // Use the active tool
  def useTool(event: MouseEvent, localPoint: Point2D) = {
    this.config.activeTool.use(this, event, localPoint)
  }

  // Select the topmost Element at the given point in the active Layer
  def select(point: Point2D): Unit = {
    if (!this.activeLayer.isHidden) {
      val selected = this.activeLayer.select(point)
      selected.foreach(this.select)
    }
  }

  // Select only the given Element
  def select(element: Element): Unit = {
    this.mConfig = this.config.copy(selectedElements = Seq(element))
  }

  def select(elements: Seq[Element]): Unit = {
    this.mConfig = this.config.copy(selectedElements = elements)
  }

  // Select the given Element but keep other selected Elements still selected
  def selectAdd(element: Element): Unit = {
    this.mConfig = this.config.copy(
      selectedElements = this.selectedElements :+ element
    )
  }

  def selectAdd(elements: Seq[Element]): Unit = {
    this.mConfig = this.config.copy(
      selectedElements = this.selectedElements ++ elements
    )
  }

  // Deselect the given Element but keep other selected Elements still selected
  def deselect(element: Element): Unit = {
    this.mConfig = this.config.copy(
      selectedElements = this.selectedElements.filter(_ != element)
    )
  }

  def selectAll(): Unit = {
    this.mConfig = this.config.copy(
      selectedElements = this.activeLayer.elements.filter(!_.isDeleted).toSeq
    )
  }

  def deselectAll(): Unit = {
    this.mConfig = this.config.copy(selectedElements = Seq())
  }

  // Undo the most recent action that relates to Elements
  def undo(): Unit = {
    val elements = ElementHistory.undo()
    if (elements.nonEmpty) {
      this.select(this.selectedElements.filter(!elements.contains(_)))
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

  // Move all the selected Elements by the given offset
  // Called by the Move Tool
  def moveSelected(xDiff: Double, yDiff: Double): Seq[Element] = {
    val newElements = this.selectedElements.map( _.move(xDiff, yDiff) )
    this.select(newElements)
    this.activeLayer
      .update(newElements)
  }

  // Check if any Layer contains the given Element
  def contains(element: Element): Boolean = {
    this.layers.exists(_.contains(element))
  }

  // Update the given Elements on the active Layer
  // and add them to ElementHistory.
  // Called by the Drawing's methods that change the selected Elements' properties.
  // It is assumed that the older versions of the Given elements are currently selected
  // so the selection is updated to contain the new versions.
  private def updateSelected(newElements: Seq[Element]): Unit = {
    val toUpdate = newElements.filter(!this.contains(_))
    this.activeLayer.update(toUpdate)
    this.select(newElements)
    toUpdate.foreach(ElementHistory.add)
  }

  // Make a group out of the selected Elements
  def groupSelected(): Unit = {
    val selected = this.selectedElements
    if (selected.nonEmpty) {
      val layer = this.activeLayer
      val index = layer.elements.indexOf(selected.last) - (selected.length - 1)
      val selectedSorted = selected.sortBy(layer.elements.indexOf(_))
      selected.foreach(layer.remove)
      val group = ElementGroup(selectedSorted)
      layer.addAtIndex(group, index)
      this.select(group)
      ElementHistory.add(group)
    }
  }

  // Dismantle the selected Group.
  // Its Elements are added back to the active Layer
  def ungroupSelected(): Unit = {
    if (this.selectedElements.nonEmpty) {
      this.selectedGroup match {
        case Some(group: ElementGroup) => {
          val (newGroup, newElements) = this.activeLayer.removeFromGroup(group, group.elements)
          ElementHistory.add(newGroup +: newElements)
          this.select(newElements)
        }
        case _ =>
      }
    }
  }

  // add new Elements to the selected Group
  def addSelectedToGroup(): Unit = {
    if (this.selectedElements.nonEmpty) {
      this.selectedGroup match {
        case Some(group: ElementGroup) => {
          val layer = this.activeLayer
          val newGroup = group.add(this.selectedElements)
          layer.update(newGroup)
          val newElements = layer.delete(this.selectedElements.filter(_ != group))
          ElementHistory.add(newGroup +: newElements)
          this.select(newGroup)
        }
        case _ =>
      }
    }
  }

  // remove some Elements from the selected Group
  def removeFromSelectedGroup(names: Seq[String]) = {
    val layer = this.activeLayer
    this.selectedGroup match {
      case Some(group: ElementGroup) => {
        if (names.length >= group.elements.length) {
          this.ungroupSelected()
        } else if (layer.contains(group) && group.elements.length > 1) {
          val (newGroup, newElements) = layer.removeFromGroupByName(group, names)
          this.select(newGroup +: newElements)
        }
      }
      case _ =>
    }
  }

  // Delete the selected Elements by making their isDeleted value true
  def deleteSelected(): Unit = {
    val toDelete = this.selectedElements.filter(!_.isDeleted)
    val deleted = this.activeLayer.delete(toDelete)
    ElementHistory.add(deleted.filter(_.isDeleted))
    this.deselectAll()
  }

  def renameElement(element: Element, newName: String): Unit = {
    val newElement = this.activeLayer.rename(element, newName)
    this.select(this.selectedElements.filter(_ != element) :+ newElement)
    if (newElement != element) {
      ElementHistory.add(newElement)
    }
  }

  // Replace a TextBox's text with the given text
  def rewriteTextBox(textBox: TextBox, newText: String): Element = {
    val layer = this.activeLayer
    if (layer.contains(textBox) && !layer.isHidden && textBox.text != newText) {
      val newTextBox = textBox.rewrite(newText)
      ElementHistory.add(newTextBox)
      layer.update(newTextBox)
    } else {
      textBox
    }
  }

  // A common method for updating any of the Element properties
  // among brush size, hardness, border width and font size.
  // Only one property is updated with a single call and others are left unchanged.
  // Groups are handled recursively
  private def updateProperty(elements: Seq[Element],
                             brushSize: Int, hardness: Int,
                             borderWidth: Int, fontSize: Int): Seq[Element] = {
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

  // Updates the selected Elements' properties if there are any.
  // Otherwise, changes the Configurations
  // Only one property / configuration is updated with a single call.
  def changeProperty(brushSize: Int = -1, hardness: Int = -1, borderWidth: Int = -1, fontSize: Int = -1) = {
    if (this.selectedElements.nonEmpty) {
      val newElements = updateProperty(this.selectedElements, brushSize, hardness, borderWidth, fontSize)
      this.updateSelected(newElements)
    } else {
      if (brushSize >= 0) {
        this.mConfig = this.config.copy(activeBrush = this.config.activeBrush.copy(size = brushSize))
      } else if (hardness >= 0) {
        this.mConfig = this.config.copy(activeBrush = this.config.activeBrush.copy(hardness = hardness))
      } else if (borderWidth >= 0) {
        this.mConfig = this.config.copy(borderWidth = borderWidth)
      } else if (fontSize >= 0) {
        this.mConfig = this.config.copy(fontSize = fontSize)
      }
    }
  }

  // Update the primary color of the given Elements.
  // Groups are handled recursively
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

  // Update the primary color of the selected Elements if there are any.
  // Otherwise, change the Configurations.
  def changePrimaryColor(color: Color) = {
    if (this.selectedElements.nonEmpty) {
      val newElements = this.updatePrimaryColor(this.selectedElements, color)
      this.updateSelected(newElements)
    } else {
      this.mConfig = this.config.copy(primaryColor = color)
    }
  }

  // Update the secondary color of the given Elements.
  // Groups are handled recursively
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

  // Update the secondary color of the selected Elements if there are any.
  // Otherwise, change the Configurations.
  def changeSecondaryColor(color: Color) = {
    if (this.selectedElements.nonEmpty) {
      val newElements = updateSecondaryColor(this.selectedElements, color)
      this.updateSelected(newElements)
    } else {
      this.mConfig = this.config.copy(secondaryColor = color)
    }
  }

  // change the active Tool
  def changeTool(tool: Tool) = {
    this.mConfig = this.config.copy(activeTool = tool)
  }

  // A common method for updating either the useFill or the useBorder value of the given Elements.
  // Groups are handled recursively
  private def updateUseBorderOrFill(elements: Seq[Element], newValue: Boolean, changeUseBorder: Boolean): Seq[Element] = {
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

  // Update the useBorder value of the selected Elements if there are any.
  // Otherwise, change the configurations.
  def changeUseBorder(newValue: Boolean) = {
    if (this.selectedElements.nonEmpty) {
      val newElements = updateUseBorderOrFill(this.selectedElements, newValue, true)
      this.updateSelected(newElements)
    } else {
      this.mConfig = this.config.copy(useBorder = newValue)
    }
  }

  // Update the useFill value of the selected Elements if there are any.
  // Otherwise, change the configurations.
  def changeUseFill(newValue: Boolean) = {
    if (this.selectedElements.nonEmpty) {
      val newElements = updateUseBorderOrFill(this.selectedElements, newValue, false)
      this.updateSelected(newElements)
    } else {
      this.mConfig = this.config.copy(useFill = newValue)
    }
  }

}