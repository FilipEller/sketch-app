package gui

import javafx.fxml.FXMLLoader
import javafx.scene.layout.AnchorPane
import logic._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
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

  /*
  Creation of a new primary stage (Application window).
  We can use Scala's anonymous subclass syntax to get quite
  readable code.
  */

  //val resource = getClass.getResource("/AdoptionForm.fxml")
  //val root: jfxs.Parent = jfxf.FXMLLoader.load(resource)
/*
  class TestController(input: TextField,
                     create: Button,
                     recentInputs: ListView[String]) {

	// event handlers are simple public methods:
	def onCreate(event: ActionEvent): Unit = {
		println("creating")
	  }
  }


  trait UnitConverterInterface {
  def setInitialValue(value: Double): Unit
}

@sfxml
class UnitConverterPresenter(unit: Int
                            )
  extends UnitConverterInterface {

  def setInitialValue(value: Double): Unit = {
    println(value)
  }
}
*/

  println("loading FXML")
  val loader = new FXMLLoader(getClass.getResource("/sketch-app-gui.fxml"))
  val root: AnchorPane = loader.load()

  println("getting Controller")
  val controller = loader.getController[DrawingController]
  controller.testFunction(1)

  println("setting up stage")
  stage = new JFXApp.PrimaryStage {
    title.value = "Sketch App"
    width = 1920
    height = 1080
  }

  val scene = new Scene(root) //Scene acts as a container for the scene graph
  stage.scene = scene

  println("setting up drawing")
  val drawing = new Drawing(500, 500)
  val layer = drawing.layers.head
  val rectangle = Shape(Rectangle, "rectangle0", 100, 50, 2, rgb(100, 50, 160), rgb(160, 50, 80), Point(10, 20), 0)
  layer.addElement(rectangle)

  println("setting drawing and config to controller")
  controller.drawing = drawing
  controller.config = drawing.config

  controller.testFunction(2)

  println("calling createCanvas")
  controller.createCanvas()

  //val root: jfxs.Parent = jfxf.FXMLLoader.load(resource)

  /*
  stage = new PrimaryStage() {
    title = "FXML GridPane Demo"
    scene = new Scene(root)
  }

   */






  /*
  val loader = new FXMLLoader(
  getClass.getResource("unitconverter.fxml"),
  // ...
  )

  loader.load()

  val root = loader.getRoot[jfxs.Parent]





   */

  /*
  Create root gui component, add it to a Scene
  and set the current window scene.
  */

  //val root = new GridPane

  //Create some components to fill the grid with
  /*
  val topPanel = new HBox
  val bottomPanel = new HBox //Horizontal box is like VBox except children are laid in a row.
  val leftPanel = new VBox
  val rightPanel = new VBox
  val stackPane = new StackPane
  val drawing = new Drawing(900, 600)



  */


  //Add child components to grid
  //Method usage: add(child, columnIndex, rowIndex, columnSpan, rowSpan)
  /*
  root.add(leftPanel, 0, 1, 1, 2)
  root.add(rightPanel, 2, 1, 1, 2)
  root.add(topPanel, 0, 0, 3, 1)
  root.add(bottomPanel, 1, 2, 1, 1)
  root.add(drawing.paint(stackPane), 1, 1, 1, 1)

  //Define grid row and column size
  val column0 = new ColumnConstraints
  val column1 = new ColumnConstraints
  val column2 = new ColumnConstraints
  val row0 = new RowConstraints
  val row1 = new RowConstraints
  val row2 = new RowConstraints

  column0.percentWidth = 4
  column1.percentWidth = 76
  column2.percentWidth = 20
  row0.percentHeight = 17
  row1.percentHeight = 80
  row2.percentHeight = 3

  //root.columnConstraints = Array(column0, column1, column2) //Add constraints in order
  //root.rowConstraints = Array(row0, row1, row2)

   */

  /*
  leftPanel.background = new Background(Array(new BackgroundFill((Gray), CornerRadii.Empty, Insets.Empty))) //Set sideBox background color
  rightPanel.background = new Background(Array(new BackgroundFill((Gray), CornerRadii.Empty, Insets.Empty)))
  topPanel.background = new Background(Array(new BackgroundFill((Blue), CornerRadii.Empty, Insets.Empty)))
  bottomPanel.background = new Background(Array(new BackgroundFill((Blue), CornerRadii.Empty, Insets.Empty))) //Set bottomBox background color
   */

}
