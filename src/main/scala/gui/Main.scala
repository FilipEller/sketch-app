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
import scalafxml.core.FXMLView
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

object Main extends JFXApp {

  println("loading FXML")
  val loader = new FXMLLoader(getClass.getResource("/sketch-app-gui.fxml"))
  val root: AnchorPane = loader.load()
  val controller = loader.getController[Controller]

  println("setting up stage")
  stage = new JFXApp.PrimaryStage {
    title.value = "Sketch App"
    width = 1920
    height = 1080
  }
  val scene = new Scene(root)
  stage.scene = scene

  println("setting up drawing")
  var drawing = new Drawing(1000, 600)
  val layer = drawing.layers.head
  controller.drawing = drawing
  controller.initController()

  // this is a bit funny place for this
  // considering other event handler are in DrawingController
  def handleKeyEvent(event: KeyEvent): Unit = {
    println(event.getCode)
    event.code match {
      case KeyCode.Z if event.isControlDown => {
        this.drawing.undo()
        this.controller.updateSelectedView()
        this.controller.updateCanvas()
      }
      case KeyCode.X if event.isControlDown => {
        this.drawing.deleteSelected()
        this.controller.update()
        this.controller.updateCanvas()
      }
      // CTRL + H for hiding/unhiding selected layer?
      // CTRL + S for saving?
      case _ => {
        this.drawing.config.selectedElements.headOption match {
          case Some(textBox: TextBox) => {
            val layer = this.drawing.config.activeLayer
            if (layer.contains(textBox)) {
              layer.removeElement(textBox)
              val newText = {
                event.code match {
                  case KeyCode.BackSpace => textBox.text.dropRight(1)
                  case KeyCode.Space => textBox.text + " "
                  case KeyCode.Enter => textBox.text + "\n"
                  case _ if event.isShiftDown => textBox.text + event.text.toUpperCase
                  case _ => textBox.text + event.text
                }
              }
              val element = textBox.copy(text = newText)
              layer.addElement(element)
              this.drawing.config = this.drawing.config.copy(selectedElements = this.drawing.config.selectedElements.filter( el => el != textBox) :+ element)
              this.controller.updateCanvas()
            }
          }
          case _ => // shortcuts for tools?
        }
      }
    }
  }

  scene.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler[KeyEvent] {
    def handle(event: KeyEvent): Unit = handleKeyEvent(event)
  })

}
