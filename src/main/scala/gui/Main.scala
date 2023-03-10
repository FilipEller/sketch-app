package gui

import javafx.fxml.FXMLLoader
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.{Alert, Button, ButtonType, Label}
import javafx.scene.input.KeyEvent
import javafx.scene.layout.AnchorPane
import logic._
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.scene.input.KeyCode
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter
import ujson.ParseException
import ujson.Value.InvalidData

import java.util.NoSuchElementException

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

  def saveDrawing(): Boolean = {
    val fileChooser = new FileChooser {
      title = "Save Drawing"
    }
    fileChooser.getExtensionFilters.addOne(new ExtensionFilter("JSON", "*.json"))

    val file = fileChooser.showSaveDialog(stage)
    if (file != null) {
      FileManager.save(drawing, file)
      true
    } else {
      false
    }
  }

  def newDrawing(): Unit = {
    def createNewDrawing(): Unit = {
      this.drawing = new Drawing(1000, 600)
      ElementHistory.clear()
      this.controller.initController()
      this.controller.update()
    }

    if (this.drawing.layers.exists(_.elements.exists(!_.isDeleted))) {
      val alert = new Alert(AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)
      alert.getDialogPane.setContent(new Label("Are you sure you want to create a new drawing? Any unsaved changes will be lost."))
      val noButton = alert.getDialogPane.lookupButton(ButtonType.NO).asInstanceOf[Button]
      noButton.setText("Save and create new")
      alert.showAndWait()

      if (alert.getResult == ButtonType.YES) {
        createNewDrawing()
      } else if (alert.getResult == ButtonType.NO) {
        val saved = this.saveDrawing()
        if (saved) {
          createNewDrawing()
        }
      }
    } else {
      createNewDrawing()
    }
  }

  def loadDrawing(): Unit = {
    def showAlert(): Unit = {
      val alert = new Alert(AlertType.ERROR, "Chosen file did not contain a valid drawing.", ButtonType.OK)
      alert.show()
    }

    def load(): Unit = {
      val fileChooser = new FileChooser {
        title = "Load Drawing"
      }

      val file = fileChooser.showOpenDialog(stage)
      if (file != null) {
        try {
          this.drawing = FileManager.load(file)
          ElementHistory.clear()
          this.controller.initController()
          this.controller.update()
        } catch {
          case ParseException(_, _) => showAlert()
          case InvalidData(_, _) => showAlert()
          case e: NoSuchElementException => showAlert()
          case e: IllegalArgumentException => showAlert()
        }
      }
    }

    if (this.drawing.layers.exists(_.elements.exists(!_.isDeleted))) {
      val alert = new Alert(AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)
      alert.getDialogPane.setContent(new Label("Are you sure you want to open a drawing? Any unsaved changes will be lost."))
      val noButton = alert.getDialogPane.lookupButton(ButtonType.NO).asInstanceOf[Button]
      noButton.setText("Save and open")
      alert.showAndWait()

      if (alert.getResult == ButtonType.YES) {
        load()
      } else if (alert.getResult == ButtonType.NO) {
        val saved = this.saveDrawing()
        if (saved) {
          load()
        }
      }
    } else {
      load()
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

  // Write into all the selected TextBoxes
  def updateTextBoxes(event: KeyEvent): Unit = {
    if (!event.isAltDown && !event.isControlDown) {
      def write(textBox: TextBox): Unit = {
        if (this.drawing.activeLayer.contains(textBox)) {
          val newText = {
            event.getEventType match {
              case KeyEvent.KEY_TYPED => {
                if (event.getCharacter == "\b") {
                  textBox.text
                } else {
                  textBox.text + event.getCharacter
                }
              }
              case KeyEvent.KEY_PRESSED => {
                event.code match {
                  case KeyCode.BackSpace => textBox.text.dropRight(1)
                  case _ => textBox.text
                }
              }
            }
          }
          if (newText != textBox.text) {
            val newTextBox = this.drawing.rewriteTextBox(textBox, newText)
            this.drawing.deselect(textBox)
            this.drawing.select(this.drawing.selectedElements :+ newTextBox)
          }
        }
      }
      val textBoxes =
        this.drawing.selectedElements
          .filter(_.isInstanceOf[TextBox])
          .map(_.asInstanceOf[TextBox])
      textBoxes.foreach(write)
      this.controller.updateCanvas()
    }
  }

  // Shortcuts for the user
  def handleKeyEvent(event: KeyEvent): Unit = {
    if (event.isControlDown) {
      event.code match {
        // Actions
        case KeyCode.Z => this.undo()
        case KeyCode.X => this.deleteSelected()
        case KeyCode.N => this.newDrawing()
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
    } else if (event.isAltDown) {
      event.code match {
        // Changing tools
        case KeyCode.V => this.drawing.changeTool(SelectionTool)
        case KeyCode.M => this.drawing.changeTool(MoveTool)
        case KeyCode.B => this.drawing.changeTool(BrushTool)
        case KeyCode.L => this.drawing.changeTool(LineTool)
        case KeyCode.R => this.drawing.changeTool(RectangleTool)
        case KeyCode.E => this.drawing.changeTool(EllipseTool)
        case KeyCode.S => this.drawing.changeTool(SquareTool)
        case KeyCode.C => this.drawing.changeTool(CircleTool)
        case KeyCode.T => this.drawing.changeTool(TextTool)
        case _ =>
      }
    } else {
      if (!this.drawing.selectedElements.exists(_.isInstanceOf[TextBox])) {
        event.code match {
          // Simpler alternatives for changing tools
          case KeyCode.V => this.drawing.changeTool(SelectionTool)
          case KeyCode.M => this.drawing.changeTool(MoveTool)
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

  // Listen for KeyEvents
  // KEY_PRESSED is used for recognizing shortcuts
  // as well as recognizing backspace when writing into TextBoxes
  // KEY_TYPED is used for writing into TextBoxes
  scene.addEventFilter(KeyEvent.KEY_PRESSED, (event: KeyEvent) => this.handleKeyEvent(event))
  scene.addEventFilter(KeyEvent.KEY_PRESSED, (event: KeyEvent) => this.updateTextBoxes(event))
  scene.addEventFilter(KeyEvent.KEY_TYPED, (event: KeyEvent) => this.updateTextBoxes(event))

}
