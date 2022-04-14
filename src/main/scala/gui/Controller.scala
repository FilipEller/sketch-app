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
import scalafx.scene.paint.Color
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter

import scala.math

class Controller {
  // this should be split into multiple objects whose methods are called from here.

  @FXML var pane: javafx.scene.layout.StackPane = _
  @FXML var layerView: javafx.scene.control.ListView[String] = _
  @FXML var groupView: javafx.scene.control.ListView[String] = _
  @FXML var selectedView: javafx.scene.control.ListView[String] = _

  @FXML var primaryColorPicker: javafx.scene.control.ColorPicker = _
  @FXML var secondaryColorPicker: javafx.scene.control.ColorPicker = _
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

  @FXML protected def changeUseBorder(event: ActionEvent): Unit = {
    println("border checkbox: " + borderCheckBox.isSelected)
    this.drawing.config = this.drawing.config.copy(useBorder = borderCheckBox.isSelected)
  }

  @FXML protected def changeUseFill(event: ActionEvent): Unit = {
    println("fill checkbox: " + fillCheckBox.isSelected)
    this.drawing.config = this.drawing.config.copy(useFill = fillCheckBox.isSelected)
  }

  @FXML protected def changeBrushSize(event: javafx.scene.input.MouseEvent): Unit = {
    val newSize = this.brushSizeSlider.getValue.ceil.toInt
    this.drawing.changeProperty(brushSize = newSize)
    updateCanvas()
  }

  @FXML protected def changeHardness(event: javafx.scene.input.MouseEvent): Unit = {
    val newHardness = this.hardnessSlider.getValue.ceil.toInt
    this.drawing.changeProperty(hardness = newHardness)
    updateCanvas()
  }

  @FXML protected def changeBorderWidth(event: javafx.scene.input.MouseEvent): Unit = {
    val newBorderWidth = this.borderWidthSlider.getValue.ceil.toInt
    this.drawing.changeProperty(borderWidth = newBorderWidth)
    updateCanvas()
  }

  @FXML protected def changeFontSize(event: javafx.scene.input.MouseEvent): Unit = {
    val newFontSize = this.fontSizeSlider.getValue.ceil.toInt
    this.drawing.changeProperty(fontSize = newFontSize)
    updateCanvas()
  }

  @FXML protected def changeColor(event: ActionEvent): Unit = {
    val picker = event.getTarget.asInstanceOf[ColorPicker]
    val color = picker.getValue
    val id = picker.getId
    if (this.drawing.config.selectedElements.nonEmpty) {
      val newElements = id match {
        case "primaryColorPicker" => {
          // this should be done in a method of the Drawing class tbh.
          this.drawing.config.selectedElements.map({
            case e: Shape => e.copy(color = color, previousVersion = Some(e))
            case e: Stroke => e.copy(color = color, previousVersion = Some(e))
            case e: TextBox => e.copy(color = color, previousVersion = Some(e))
            case e: Element => e
          })
        }
        case "secondaryColorPicker" => {
          this.drawing.config.selectedElements.map({
            case e: Shape => e.copy(fillColor = color, previousVersion = Some(e))
            case e: Stroke => e.copy(previousVersion = Some(e))
            case e: TextBox => e.copy(previousVersion = Some(e))
            case e: Element => e
          })
        }
        case _ => this.drawing.config.selectedElements
      }
      this.drawing.updateSelected(newElements)
      updateCanvas()
    } else {
      id match {
        case "primaryColorPicker" => this.drawing.config = this.drawing.config.copy(primaryColor = color)
        case "secondaryColorPicker" => this.drawing.config = this.drawing.config.copy(secondaryColor = color)
      }
    }
  }

  def updateSelectedView(): Unit = {
    this.selectedView.getItems.clear()
    this.drawing.config.selectedElements.reverse
      .foreach( e => this.selectedView.getItems.add(e.name) )
  }

  def updateGroupView(): Unit = {
    this.groupView.getItems.clear()
    this.drawing.selectedGroup match {
      case Some(group: ElementGroup) => group.elements.foreach( e => this.groupView.getItems.add(e.name) )
      case _ =>
    }
  }

  def updateSelectedProperties(): Unit = {
    drawing.config.selectedElements.headOption match {
      case Some(e: Element) => {
        borderCheckBox.setSelected(e match {
          case shape: Shape => shape.useBorder
          case _ => true
        } )
        fillCheckBox.setSelected(e match {
          case shape: Shape => shape.useFill
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
        primaryColorPicker.setValue(e match {
          case element: Element => element.color
        } )
        secondaryColorPicker.setValue(e match {
          case shape: Shape => shape.fillColor
          case _ => White
        } )
      }
      case None => {
        borderCheckBox.setSelected(drawing.config.useBorder)
        fillCheckBox.setSelected(drawing.config.useFill)
        brushSizeSlider.setValue(drawing.config.activeBrush.size)
        hardnessSlider.setValue(drawing.config.activeBrush.hardness)
        borderWidthSlider.setValue(drawing.config.borderWidth)
        fontSizeSlider.setValue(drawing.config.fontSize)
        primaryColorPicker.setValue(drawing.config.primaryColor)
        secondaryColorPicker.setValue(drawing.config.secondaryColor)
      }
    }
  }

  def update(): Unit = {
    updateSelectedView()
    updateGroupView()
    updateSelectedProperties()
    updateCanvas()
  }

  @FXML protected def makeGroup(event: ActionEvent): Unit = {
    this.drawing.groupSelected()
    update()
  }

  @FXML protected def ungroup(event: ActionEvent): Unit = {
    this.drawing.ungroupSelected()
    update()
  }

  @FXML protected def addToGroup(event: ActionEvent): Unit = {
    this.drawing.addSelectedToGroup()
    update()
  }

  def useTool(event: javafx.scene.input.MouseEvent): Unit = {
    val localPoint = new Point2D(baseCanvas.screenToLocal(event.getScreenX, event.getScreenY))
    this.drawing.useTool(event, localPoint)
    update()
  }

  def initController(): Unit = {
    println("initializing canvas")

    this.baseCanvas = new Canvas(this.drawing.width, this.drawing.height)
    pane.children.clear()
    pane.children += baseCanvas

    val g = baseCanvas.graphicsContext2D
    g.fill = White
    g.fillRect(0, 0, this.drawing.width, this.drawing.height)

    initializeLayerView()
    // this.borderCheckBox.setOnAction(this.handleBorderCheckBox(_))
    // this.fillCheckBox.setOnAction(this.handleFillCheckBox(_))
    updateCanvas()
  }

  @FXML protected def changeTool(event: ActionEvent): Unit = {
    ConfigControls.changeTool(event, this.drawing)
  }

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

  @FXML protected def newDrawing(event: ActionEvent): Unit = {
    val newDrawing = new Drawing(1000, 600)
    this.drawing = newDrawing
    Main.drawing = newDrawing
    initController()
    update()
  }

  @FXML protected def saveDrawing(event: ActionEvent): Unit = {
    val fileChooser = new FileChooser {
      title = "Save Drawing"
    }
    fileChooser.getExtensionFilters.addOne(new ExtensionFilter("JSON", "*.json"))

    val file = fileChooser.showSaveDialog(Main.stage)
    if (file != null) {
      FileManager.save(drawing, file)
    }
  }

  @FXML protected def loadDrawing(event: ActionEvent): Unit = {
    val fileChooser = new FileChooser {
      title = "Load Drawing"
    }

    val file = fileChooser.showOpenDialog(Main.stage)
    if (file != null) {
      val loaded = FileManager.load(file)
      println(loaded)
      this.drawing = loaded
      Main.drawing = loaded
      println(this.drawing)
      println(Main.drawing)
      initController()
      update()
    }
  }
}
