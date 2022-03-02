package gui

import scalafx.application.JFXApp
import scalafx.scene.Scene
// import scalafx.scene.layout.Pane
// import scalafx.scene.layout.GridPane
import scalafx.scene.layout.VBox
import scalafx.scene.control.{Button, CheckBox, Label, MenuBar, TextField}
import scalafx.scene.paint.Color._
import scalafx.scene.shape.Rectangle
import scalafx.Includes._
import scalafx.scene.text.Font

object testApp extends JFXApp {

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

  val root = new VBox //Simple pane component
  val scene = new Scene(root) //Scene acts as a container for the scene graph
  stage.scene = scene

  val rectangle = new Rectangle {
      x = 100
      y = 100
      width = 50
      height = 50
      fill = Blue //scalafx.scene.paint.Color
  }

  val menuBar = new MenuBar

  root.children += rectangle //+= syntax needs import scalafx.Includes._

  //Create button component
  val button = new Button("I'm a button!")

  //Set button action
  button.onAction = (event) => {
      println("Click!")
  }

  //Add button to the GUI.
  root.children += button //Needs scalafx.Includes._ import

  val label = new Label("This is text!") //Create a Label

  label.textFill = Blue //Set text color
  label.font = Font.font(36) //Text font size. It is also possible to set font family.

  root.children += label //Add label to GUI.

  //Creation of a CheckBox.
  val checkBox = new CheckBox("TestBox") //Optionally give label as parameter

  val checkLabel = new Label("I am text!")    //Create a text element

  /*
  ScalaFX property magic. CheckBox.selected is a BooleanProperty. So is label.visible.
  We can bind checkbox selected value to the label visibility with a bidirectional binding.
  Binding can also be unidirectional if <== is used. http://www.scalafx.org/docs/properties/
  */
  checkBox.selected <==> checkLabel.visible

  /*
  Another option is to define a change listener:
  checkBox.selected.onChange{(source, oldValue, newValue) => label.visible = newValue}
  label.visible.onChange{(source, oldValue, newValue) => checkBox.selected = newValue}
  */

  //Root component is a VBox in this case. It places children next to each other vertically.
  root.children += checkBox //Add CheckBox to GUI.
  root.children += checkLabel

  val textLabel = new Label("Write some text")
  val textInput = new TextField
  root.children += textLabel
  root.children += textInput

  //Accessing textinput text.
  textInput.text = "type here"
}
