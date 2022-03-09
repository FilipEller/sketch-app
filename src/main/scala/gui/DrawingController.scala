package gui

import javafx.fxml.FXML
import javafx.event.ActionEvent
import scalafx.scene.layout.{Background, BackgroundFill, CornerRadii, StackPane}
import javafx.scene.layout.GridPane
import scalafx.Includes._
import scalafx.geometry.Insets
import scalafx.scene.paint.Color.Gray

class DrawingController {

  @FXML var drawingPane: StackPane = _

  @FXML var drawingBackground: GridPane = _

  @FXML protected def handleSubmitButtonAction(event: ActionEvent): Unit = {
    println("clicked button")
    drawingBackground.add(drawingPane, 1, 1, 1, 1)
    // drawingPane.background = new Background(Array(new BackgroundFill((Gray), CornerRadii.Empty, Insets.Empty)))
  }

  @FXML def testFunction(value: Int): Unit = {
    println("function ran")
    println(drawingPane)
    println(drawingBackground)
  }

}
