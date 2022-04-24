package gui

import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.{Alert, Button, ButtonType, ColorPicker, Label, SelectionMode}
import logic._
import scalafx.Includes._
import scalafx.geometry.Point2D
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.TextInputDialog
import scalafx.scene.paint.Color.White

class Controller {

  // All of these variables will be given values when the FXML file is loaded in Main.
  // When the Controller's methods can be called, none of these will be null anymore.
  @FXML var pane: javafx.scene.layout.StackPane = _ // The stack of canvases where the Drawing is rendered
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

  // These variables are set in this.initController
  var drawing: Drawing = _
  var baseCanvas: Canvas = _

  // Renders the Drawing on the StackPane
  def updateCanvas(): Unit = {
    // Empties the StackPane except for white base Canvas
    this.pane.children.tail.foreach(this.pane.children -= _)
    drawing.paint(this.pane)

    // Adds the event listeners back
    this.pane.children.tail.foreach(canvas => {
      canvas.setOnMousePressed(this.useTool(_))
      canvas.setOnMouseDragged(this.useTool(_))
      canvas.setOnMouseReleased(this.useTool(_))
    })
  }

  // Makes a cell to LayerView for each Layer in Drawing
  def updateLayerView(): Unit = {
    val selectedIndex = this.layerView.getSelectionModel.getSelectedIndex
    this.layerView.getItems.clear()
    this.drawing.layers.reverse
      .foreach( l => this.layerView.getItems.add(l.name) )
    this.layerView.getSelectionModel.select(selectedIndex)
  }

  // Selects a Layer cell in LayerView
  def selectLayer(new_val: String) = {
    val layerOption = this.drawing.findLayer(new_val)
    layerOption.foreach(this.drawing.selectLayer)
    updateCanvas()
    update()
  }

  def initializeLayerView(): Unit = {
    this.layerView.getSelectionModel.selectedItemProperty.addListener(new ChangeListener[String]() {
      override def changed(observableValue: ObservableValue[_ <: String], old_val: String, new_val: String): Unit = {
        selectLayer(new_val)
      }
    })
    updateLayerView()
    layerView.getSelectionModel.select(0)
  }

  @FXML protected def changeUseBorder(event: ActionEvent): Unit = {
    this.drawing.changeUseBorder(borderCheckBox.isSelected)
    updateCanvas()
  }

  @FXML protected def changeUseFill(event: ActionEvent): Unit = {
    this.drawing.changeUseFill(fillCheckBox.isSelected)
    updateCanvas()
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
    picker.getId match {
      case "primaryColorPicker" => drawing.changePrimaryColor(color)
      case "secondaryColorPicker" => drawing.changeSecondaryColor(color)
    }
    updateCanvas()
  }

  // Makes a cell in SelectedView for each selected Element of Drawing
  def updateSelectedView(): Unit = {
    val index = this.selectedView.getSelectionModel.getSelectedIndex
    this.selectedView.getItems.clear()
    this.drawing.selectedElements.reverse
      .foreach( e => this.selectedView.getItems.add(e.name) )
    // Keeps one of the Elements focused in SelectedView for renaming
    if (this.selectedView.getItems.nonEmpty) {
      val targetIndex = math.max(math.min(index, this.selectedView.getItems.length - 1), 0)
      this.selectedView.getSelectionModel.select(targetIndex)
    }
  }

  // Makes a cell in GroupView for each Element inside selected ElementGroup
  // selected Group is the most recently selected ElementGroup in Drawing's selected Elements
  def updateGroupView(): Unit = {
    this.groupView.getItems.clear()
    this.drawing.selectedGroup match {
      case Some(group: ElementGroup) => group.elements.foreach( e => this.groupView.getItems.add(e.name) )
      case _ =>
    }
  }

  // Sets the GUI's controls to match the given Element's properties
  // or sets them to the user's current configuration if elementOption is None.
  // If the given Element does not have some property, a default value for that property is used
  def updateSelectedPropertiesByElement(elementOption: Option[Element]): Unit = {
    val default = this.drawing.defaultConfig
    elementOption match {
      case Some(group: ElementGroup) => {
        this.updateSelectedPropertiesByElement(group.elements.headOption)
      }
      case Some(e: Element) => {
        borderCheckBox.setSelected(e match {
          case shape: Shape => shape.useBorder
          case _ => default.useBorder
        } )
        fillCheckBox.setSelected(e match {
          case shape: Shape => shape.useFill
          case _ => default.useFill
        } )
        brushSizeSlider.setValue(e match {
          case stroke: Stroke => stroke.brush.size
          case _ => default.activeBrush.size
        } )
        hardnessSlider.setValue(e match {
          case stroke: Stroke => stroke.brush.hardness
          case _ => default.activeBrush.hardness
        } )
        borderWidthSlider.setValue(e match {
          case shape: Shape => shape.borderWidth
          case _ => default.borderWidth
        } )
        fontSizeSlider.setValue(e match {
          case textBox: TextBox => textBox.fontSize
          case _ => default.fontSize
        } )
        primaryColorPicker.setValue(e match {
          case element: Element => element.color
        } )
        secondaryColorPicker.setValue(e match {
          case shape: Shape => shape.fillColor
          case _ => default.secondaryColor
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

  def updateSelectedProperties(): Unit = {
    this.updateSelectedPropertiesByElement(
      drawing.selectedElements.headOption
    )
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

  @FXML protected def removeFromGroup(event: ActionEvent): Unit = {
    val names = this.groupView.getSelectionModel.getSelectedItems.toSeq
    this.drawing.removeFromSelectedGroup(names)
    updateCanvas()
    update()
  }

  // Rename the focused Element in SelectedView through a dialog
  @FXML protected def renameElement(event: ActionEvent): Unit = {
    val selected = if (this.selectedView.getItems.length == 1) {
      this.selectedView.getItems.head
    } else {
      this.selectedView.getSelectionModel.getSelectedItem
    }
    if (selected != null) {
      val dialog = new TextInputDialog()
      dialog.setTitle("")
      dialog.setHeaderText(s"Rename $selected")
      dialog.getDialogPane.setContentText("New name:")
      dialog.showAndWait()

      val input = dialog.getEditor.getText
      if (input != null && input.nonEmpty) {
        val elementOption = this.drawing.activeLayer.find(selected)
        elementOption match {
          case Some(e: Element) => {
            this.drawing.renameElement(e, input)
            updateCanvas()
            update()
          }
          case None =>
        }
      }
    }
  }

  @FXML protected def deleteSelected(event: ActionEvent): Unit = {
    Main.deleteSelected()
  }

  // Called whenever the user clicks on the Canvas
  def useTool(event: javafx.scene.input.MouseEvent): Unit = {
    if (!this.drawing.activeLayer.isHidden) {
      if (!Seq(SelectionTool, MoveTool).contains(this.drawing.config.activeTool)) {
        this.drawing.deselectAll()
      }
      val localPoint = new Point2D(baseCanvas.screenToLocal(event.getScreenX, event.getScreenY))
      this.drawing.useTool(event, localPoint)
      update()
    }
  }

  // Initialize the controller
  // This is called after the FXML is loaded and Main has created a Drawing
  def initController(): Unit = {
    println("initializing canvas")
    this.drawing = Main.drawing // Will not be null. DelayedInit is an oddity of this version of ScalaFX

    this.groupView.getSelectionModel.setSelectionMode(SelectionMode.MULTIPLE)

    // Paint a white rectangle as the background of the user's artwork
    this.baseCanvas = new Canvas(this.drawing.width, this.drawing.height)
    this.pane.children.clear()
    this.pane.children += baseCanvas
    val g = baseCanvas.graphicsContext2D
    g.fill = White
    g.fillRect(0, 0, this.drawing.width, this.drawing.height)

    initializeLayerView()
    updateCanvas()
  }

  // Change the active Tool when the user clicks on a button in the Tool bar
  @FXML protected def changeTool(event: ActionEvent): Unit = {
    val button = event.getTarget.asInstanceOf[javafx.scene.control.Button].getId
    val targetTool = button match {
      case "Select" => SelectionTool
      case "Transform" => MoveTool
      case "Brush" => BrushTool
      case "Line" => LineTool
      case "Rectangle" => RectangleTool
      case "Square" => SquareTool
      case "Ellipse" => EllipseTool
      case "Circle" => CircleTool
      case "Text" => TextTool
      case _ => SelectionTool
    }
    this.drawing.changeTool(targetTool)
  }

  @FXML protected def addLayer(event: ActionEvent) = {
    this.drawing.addLayer()
    updateLayerView()
  }

  // Remove a Layer from the Drawing
  // If the Layer is not empty, the action requires user confirmation through a dialog
  @FXML protected def removeLayer(event: ActionEvent) = {
    val layerIndex = this.layerView.getSelectionModel.getSelectedIndex
    val layerName = this.layerView.getSelectionModel.getSelectedItem
    val layerOption = this.drawing.findLayer(layerName)

    if (layerOption.exists(_.elements.exists(!_.isDeleted))) {
      val alert = new Alert(AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.CANCEL)
      alert.getDialogPane.setContent(new Label(s"Are you sure you want to remove $layerName? It is NOT empty. This action cannot be undone."))
      alert.showAndWait()

      if (alert.getResult == ButtonType.YES) {
        this.drawing.removeLayer(layerName)
      }
    } else {
      this.drawing.removeLayer(layerName)
    }
    updateLayerView()
    val targetIndex = math.min(this.drawing.layers.length - 1, math.max(layerIndex, 0))  // layer under the removed one
    this.layerView.getSelectionModel.select(targetIndex)
    updateCanvas()
  }

  // Rename the active Layer through a dialog
  @FXML protected def renameLayer(event: ActionEvent) = {
    val dialog = new TextInputDialog()
    dialog.setTitle("")
    dialog.setHeaderText(s"Rename ${this.drawing.activeLayer.name}")
    dialog.getDialogPane.setContentText("New name:")
    dialog.showAndWait()

    val input = dialog.getEditor.getText
    if (input != null && input.nonEmpty) {
        this.drawing.renameLayer(this.drawing.activeLayer, input)
    }
    updateLayerView()
  }

  @FXML protected def toggleLayerHidden(event: ActionEvent) = {
    this.drawing.toggleActiveLayerHidden()
    updateCanvas()
  }

  @FXML protected def newDrawing(event: ActionEvent): Unit = {
    Main.newDrawing()
  }

  @FXML protected def saveDrawing(event: ActionEvent): Unit = {
    Main.saveDrawing()
  }

  @FXML protected def loadDrawing(event: ActionEvent): Unit = {
    Main.loadDrawing()
  }

  @FXML protected def undo(event: ActionEvent): Unit = {
    Main.undo()
  }

  @FXML protected def selectAll(event: ActionEvent): Unit = {
    Main.selectAll()
  }

  @FXML protected def deselectAll(event: ActionEvent): Unit = {
    Main.deselectAll()
  }

}
