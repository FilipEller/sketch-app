package gui

import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.input.MouseEvent
import javafx.scene.layout.AnchorPane
import logic._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.Point2D
import scalafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import scalafx.scene.{Parent, Scene}
import scalafx.scene.layout.{Background, ColumnConstraints, CornerRadii, HBox, RowConstraints, StackPane}
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter
import scalafxml.core.FXMLView
import ujson.ParseException
// import scalafx.scene.layout.Pane
import scalafx.scene.layout.GridPane
import scalafx.scene.layout.VBox
import scalafx.scene.control.{Button, CheckBox, Label, MenuBar, TextField}
import scalafx.scene.paint.Color._
import scalafx.Includes._
import scalafx.scene.text.Font
import scalafx.scene.layout.BackgroundFill
import scalafx.scene.canvas.Canvas
import scalafx.geometry.Insets
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.ButtonType

object Main extends JFXApp {

  println("loading FXML")
  val loader = new FXMLLoader(getClass.getResource("/main.fxml"))
  val root: AnchorPane = loader.load()
  val controller = loader.getController[Controller]

  println("setting up stage")
  stage = new JFXApp.PrimaryStage {
    title.value = "Sketch App"
    width = 1440
    height = 900
  }
  val scene = new Scene(root)
  stage.scene = scene

  println("setting up drawing")
  var drawing = new Drawing(1000, 600)
  val layer = drawing.layers.head
  this.controller.drawing = drawing
  this.controller.initController()

  def newDrawing(): Unit = {
    this.drawing = new Drawing(1000, 600)
    this.controller.initController()
    this.controller.update()
  }

  def saveDrawing(): Unit = {
    val fileChooser = new FileChooser {
      title = "Save Drawing"
    }
    fileChooser.getExtensionFilters.addOne(new ExtensionFilter("JSON", "*.json"))

    val file = fileChooser.showSaveDialog(stage)
    if (file != null) {
      FileManager.save(drawing, file)
    }
  }

  def loadDrawing(): Unit = {
    val fileChooser = new FileChooser {
      title = "Load Drawing"
    }

    val file = fileChooser.showOpenDialog(stage)
    if (file != null) {
      try {
        this.drawing = FileManager.load(file)
        this.controller.initController()
        this.controller.update()
      } catch {
        case ParseException(_, _) => {
          val alert = new Alert(AlertType.ERROR, "Chosen file did not contain a valid drawing.", ButtonType.OK)
          alert.show()
        }
      }
    }
  }

  def undo(): Unit = {
    this.drawing.undo()
    this.controller.updateSelectedView()
    this.controller.updateCanvas()
    this.controller.update()
  }

  def deleteSelected(): Unit = {
    this.drawing.deleteSelected()
    this.controller.update()
    this.controller.updateCanvas()
  }

  def selectAll(): Unit = {
    this.drawing.selectAll()
    this.controller.updateCanvas()
    this.controller.update()
  }

  def deselectAll(): Unit = {
    this.drawing.deselectAll()
    this.controller.updateCanvas()
    this.controller.update()
  }

  def groupSelected(): Unit = {
    this.drawing.groupSelected()
    this.controller.updateCanvas()
    this.controller.update()
  }

  def ungroupSelected(): Unit = {
    this.drawing.ungroupSelected()
    this.controller.updateCanvas()
    this.controller.update()
  }

  def toggleLayerVisibility(): Unit = {
    this.drawing.toggleActiveLayerHidden()
    this.controller.updateCanvas()
    this.controller.update()
  }

  def toggleBorderCheckBox(): Unit = {
    val borderCheckBox = this.controller.borderCheckBox
    borderCheckBox.setSelected(!borderCheckBox.isSelected)
    this.drawing.changeUseBorder(borderCheckBox.isSelected)
    this.controller.updateCanvas()
  }

  def toggleFillCheckBox(): Unit = {
    val fillCheckBox = this.controller.fillCheckBox
    fillCheckBox.setSelected(!fillCheckBox.isSelected)
    this.drawing.changeUseFill(fillCheckBox.isSelected)
    this.controller.updateCanvas()
  }

  def updateTextBox(textBox: TextBox, event: KeyEvent) = {
    val layer = this.drawing.config.activeLayer
    if (layer.contains(textBox)) {
      val newText = {
        event.code match {
          case KeyCode.BackSpace => textBox.text.dropRight(1)
          case KeyCode.Space => textBox.text + " "
          case KeyCode.Enter => textBox.text + "\n"
          case _ if event.isShiftDown => textBox.text + event.text.toUpperCase
          case _ => textBox.text + event.text
        }
      }
      val newTextBox = layer.rewriteTextBox(textBox, newText)
      this.drawing.deselect(textBox)
      this.drawing.select(this.drawing.config.selectedElements :+ newTextBox)
      this.controller.updateCanvas()
    }
  }

  def handleKeyEvent(event: KeyEvent): Unit = {
    if (event.isControlDown) {
      event.code match {
        case KeyCode.Z => this.undo()
        case KeyCode.X => this.deleteSelected()
        case KeyCode.S => this.saveDrawing()
        case KeyCode.O => this.loadDrawing()
        case KeyCode.A => this.selectAll()
        case KeyCode.D => this.deselectAll()
        case KeyCode.G => this.groupSelected()
        case KeyCode.U => this.ungroupSelected()
        case KeyCode.H => this.toggleLayerVisibility()
        case KeyCode.B => this.toggleBorderCheckBox()
        case KeyCode.F => this.toggleFillCheckBox()
        case _ =>
      }
    } else {
      this.drawing.config.selectedElements.headOption match {
        case Some(textBox: TextBox) => this.updateTextBox(textBox, event)
        case _ => {
          event.code match {
            case KeyCode.V => this.drawing.changeTool(SelectionTool)
            case KeyCode.M => this.drawing.changeTool(TransformTool)
            case KeyCode.B => this.drawing.changeTool(BrushTool)
            case KeyCode.L => this.drawing.changeTool(LineTool)
            case KeyCode.R => this.drawing.changeTool(RectangleTool)
            case KeyCode.E => this.drawing.changeTool(EllipseTool)
            case KeyCode.S => this.drawing.changeTool(SquareTool)
            case KeyCode.C => this.drawing.changeTool(CircleTool)
            case KeyCode.T => this.drawing.changeTool(TextTool)
            case _ =>
          }
        }
      }
    }
  }

  scene.addEventFilter(KeyEvent.KEY_PRESSED, (event: KeyEvent) => handleKeyEvent(event))

}
