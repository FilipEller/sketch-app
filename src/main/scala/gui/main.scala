package gui

import scalafx.application.JFXApp
import scalafx.scene.Scene
// import scalafx.scene.layout.Pane
import scalafx.scene.layout.GridPane
import scalafx.scene.layout.VBox
import scalafx.scene.control.{Button, CheckBox, Label, MenuBar, TextField}
import scalafx.scene.paint.Color._
import scalafx.scene.shape.Rectangle
import scalafx.Includes._
import scalafx.scene.text.Font

object Main extends JFXApp {

  /*
  Creation of a new primary stage (Application window).
  We can use Scala's anonymous subclass syntax to get quite
  readable code.
  */

  stage = new JFXApp.PrimaryStage {
      title.value = "Sketch App"
      width = 600
      height = 450
  }

  /*
  Create root gui component, add it to a Scene
  and set the current window scene.
  */

  val root = new GridPane
  val scene = new Scene(root) //Scene acts as a container for the scene graph
  stage.scene = scene


}
