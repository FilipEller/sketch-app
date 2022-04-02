package gui

import javafx.beans.value.ObservableValue
import javafx.fxml.FXML
import javafx.event.ActionEvent
import javafx.scene.input.MouseEvent
import scalafx.scene.layout.{Background, BackgroundFill, CornerRadii, StackPane}
import javafx.scene.layout.StackPane
import logic._
import scalafx.Includes._
import scalafx.geometry.{Insets, Point2D}
import scalafx.scene.Node
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.{Button, Slider}
import scalafx.scene.input.MouseDragEvent
import scalafx.scene.paint.Color.{Blue, White, rgb}
import javafx.scene.control.{ColorPicker, ListView}
import scalafx.animation.AnimationTimer
import scalafx.collections.ObservableBuffer
import javafx.beans.value.ChangeListener
import javafx.scene.input.DragEvent

import scala.math

class Controller {
  // this should be split into multiple objects whose methods are called from here.

  @FXML var pane: javafx.scene.layout.StackPane = _
  @FXML var layerView: javafx.scene.control.ListView[String] = _
  @FXML var groupView: javafx.scene.control.ListView[String] = _
  @FXML var selectedView: javafx.scene.control.ListView[String] = _

  @FXML var borderCheckBox: javafx.scene.control.CheckBox = _
  @FXML var fillCheckBox: javafx.scene.control.CheckBox = _
  @FXML var brushSizeSlider: javafx.scene.control.Slider = _
  @FXML var hardnessSlider: javafx.scene.control.Slider = _
  @FXML var borderWidthSlider: javafx.scene.control.Slider = _
  @FXML var fontSizeSlider: javafx.scene.control.Slider = _

  var drawing: Drawing = _
  var baseCanvas: Canvas = _


  def updateCanvas(): Unit = {
    pane.children.tail.foreach(pane.children -= _) // empties background except for white base canvas
    drawing.paint(this.pane)

    pane.children.tail.foreach(canvas => {
      canvas.setOnMousePressed(this.useTool(_))
      canvas.setOnMouseDragged(this.useTool(_))
      canvas.setOnMouseReleased(this.useTool(_))
    })
  }

  def updateLayerView(): Unit = {
    val selectedLayer = this.layerView.getSelectionModel.getSelectedItem
    this.layerView.getItems.clear()
    this.drawing.layers.reverse
      .foreach( l => this.layerView.getItems.add(l.name) )
    this.layerView.getSelectionModel.select(selectedLayer)
  }

  def selectLayer(new_val: String) = {
    println("selecting " + new_val)
    val layer = this.drawing.findLayer(new_val)
    this.drawing.config = this.drawing.config.copy(activeLayer = layer.getOrElse(this.drawing.config.activeLayer))
  }

  def initializeLayerView(): Unit = {
    this.layerView.getSelectionModel.selectedItemProperty.addListener(new ChangeListener[String]() {
      override def changed(observableValue: ObservableValue[_ <: String], old_val: String, new_val: String): Unit = {
        /*
        new_val is null after a layer is removed
        Drawing.findLayer returns Option so this will not cause an error
        Controller's removeLayer also selects another layer
        */
        selectLayer(new_val)
      }
    })
    updateLayerView()
    layerView.getSelectionModel.select(0)
  }

  def handleBorderCheckBox(event: ActionEvent): Unit = {
    this.drawing.config = this.drawing.config.copy(useBorder = borderCheckBox.isSelected)
  }

  def handleFillCheckBox(event: ActionEvent): Unit = {
    this.drawing.config = this.drawing.config.copy(useFill = fillCheckBox.isSelected)
  }

  @FXML protected def changeBrushSize(event: javafx.scene.input.MouseEvent): Unit = {
    println("Sliding")
    val newSize = this.brushSizeSlider.getValue.ceil.toInt
    val newBrush = this.drawing.config.activeBrush.copy(size = newSize)
    this.drawing.config = this.drawing.config.copy(activeBrush = newBrush)
  }

  @FXML protected def changeHardness(event: javafx.scene.input.MouseEvent): Unit = {
    println("Sliding")
    val newHardness = this.hardnessSlider.getValue.ceil.toInt
    val newBrush = this.drawing.config.activeBrush.copy(hardness = newHardness)
    this.drawing.config = this.drawing.config.copy(activeBrush = newBrush)
  }

  @FXML protected def changeBorderWidth(event: javafx.scene.input.MouseEvent): Unit = {
    println("Sliding")
    val newBorderWidth = this.borderWidthSlider.getValue.ceil.toInt
    this.drawing.config = this.drawing.config.copy(borderWidth = newBorderWidth)
  }

  @FXML protected def changeFontSize(event: javafx.scene.input.MouseEvent): Unit = {
    println("Sliding")
    val newFontSize = this.fontSizeSlider.getValue.ceil.toInt
    this.drawing.config = this.drawing.config.copy(fontSize = newFontSize)
  }

  def updateSelectedView(): Unit = {
    this.selectedView.getItems.clear()
    this.drawing.config.selectedElements.reverse
      .foreach( e => this.selectedView.getItems.add(e.name) )
  }

  def updateSelectedProperties(): Unit = {
    println("update")
    drawing.config.selectedElements.headOption match {
      case Some(e: Element) => {
        borderCheckBox.setSelected(e match {
          case shape: Shape => shape.borderColor.opacity > 0
          case _ => true
        } )
        fillCheckBox.setSelected(e match {
          case shape: Shape => shape.color.opacity > 0
          case _ => false
        } )
        brushSizeSlider.setValue(e match {
          case stroke: Stroke => stroke.brush.size
          case _ => 30
        } )
        hardnessSlider.setValue(e match {
          case stroke: Stroke => stroke.brush.hardness
          case _ => 50
        } )
        borderWidthSlider.setValue(e match {
          case shape: Shape => shape.borderWidth
          case _ => 3
        } )
        fontSizeSlider.setValue(e match {
          case textBox: TextBox => textBox.fontSize
          case _ => 12
        } )
      }
      case None =>
    }
  }

  def updateSelected(): Unit = {
    updateSelectedView()
    updateSelectedProperties()
  }

  @FXML protected def makeGroup(event: javafx.scene.input.MouseEvent): Unit = {
    println("making group")
    this.drawing.groupSelected()
    updateSelected()
    updateCanvas()
  }

  def useTool(event: javafx.scene.input.MouseEvent): Unit = {
    val localPoint = new Point2D(baseCanvas.screenToLocal(event.getScreenX, event.getScreenY))
    this.drawing.useTool(event, localPoint)
    updateSelected()
    // println("elements of active layer: " + drawing.config.activeLayer.name + " " + drawing.config.activeLayer.elements.mkString(", "))
    updateCanvas()
  }

  def initController(): Unit = {
    println("initializing canvas")

    this.baseCanvas = new Canvas(drawing.width, drawing.height)
    pane.children += baseCanvas

    val g = baseCanvas.graphicsContext2D
    g.fill = White
    g.fillRect(0, 0, this.drawing.width, this.drawing.height)

    initializeLayerView()
    this.borderCheckBox.setOnAction(this.handleBorderCheckBox(_))
    this.fillCheckBox.setOnAction(this.handleFillCheckBox(_))
    updateCanvas()
  }


  // Configurations

  // activeTool
  @FXML protected def changeTool(event: ActionEvent): Unit = {
    ConfigControls.changeTool(event, this.drawing)
  }

  // Colors
  @FXML protected def changeColor(event: ActionEvent): Unit = {
    ConfigControls.changeColor(event, this.drawing)
  }

  // activeBrush
  // fontSize

  // Layers

  @FXML protected def addLayer(event: ActionEvent) = {
    println("adding layer")
    this.drawing.addLayer()
    updateLayerView()
  }

  // Bug: If all layers are removed, drawing no longer works even after readding layers.
  @FXML protected def removeLayer(event: ActionEvent) = {
    println("removing layer")
    val layerIndex = this.layerView.getSelectionModel.getSelectedIndex
    val layerName = this.layerView.getSelectionModel.getSelectedItem
    println(layerName)
    val layer = this.drawing.removeLayer(layerName)
    updateLayerView()
    this.layerView.getSelectionModel.select(math.min(this.drawing.layers.length - 1, math.max(layerIndex, 0))) // select the layer under the removed one
    updateCanvas()
  }

  // not implemented
  @FXML protected def renameLayer(event: ActionEvent) = {
    println("renaming layer")
    // dialogue for new name
    updateLayerView()
  }

}
