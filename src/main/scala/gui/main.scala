package gui

import javafx.fxml.FXMLLoader
import javafx.scene.layout.AnchorPane
import logic._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.Point2D
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
  val controller = loader.getController[DrawingController]
  controller.testFunction(1)

  println("setting up stage")
  stage = new JFXApp.PrimaryStage {
    title.value = "Sketch App"
    width = 1920
    height = 1080
  }
  val scene = new Scene(root)
  stage.scene = scene

  println("setting up drawing")
  val drawing = new Drawing(500, 500)
  val layer = drawing.layers.head
  val rectangle = Shape(Rectangle, "rectangle0", 100, 50, 2, rgb(100, 50, 160), rgb(160, 50, 80), new Point2D(0, 0), 0)
  layer.addElement(rectangle)
  controller.drawing = drawing
  controller.config = drawing.config
  controller.createCanvas()

}
