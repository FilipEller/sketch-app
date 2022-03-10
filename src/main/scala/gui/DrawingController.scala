package gui

import javafx.fxml.FXML
import javafx.event.ActionEvent
import scalafx.scene.layout.{Background, BackgroundFill, CornerRadii, StackPane}
import javafx.scene.layout.GridPane
import logic.{Configurations, Drawing}
import scalafx.Includes._
import scalafx.geometry.Insets
import scalafx.scene.control.Button
import scalafx.scene.input.{MouseDragEvent, MouseEvent}
import scalafx.scene.paint.Color.Gray

class DrawingController {


  @FXML var drawingBackground: GridPane = _

  var drawing: Drawing = _
  var config: Configurations = _

  @FXML protected def changeToRectangleTool(event: ActionEvent): Unit = {
    println("changing to RectangleTool")
  }

  @FXML def testFunction(value: Int): Unit = {
    println("test function ran with parameter " + value)
    println(drawing)
    println(config)
    println(drawingBackground)
  }

  @FXML def getUpdatedCanvas: StackPane = {
    println("making a new canvas for GUI")
    // paint a new pane
    val drawingPane = drawing.paint(new StackPane)

    // set event listeners to pane
    drawingPane.setOnMousePressed(this.draw(_))
    drawingPane.setOnMouseDragged(this.draw(_))
    drawingPane.setOnMouseReleased(this.draw(_))

    drawingPane.setOnDragDetected(e => drawingPane.startFullDrag())

    drawingPane
  }

  @FXML def draw(event: MouseEvent): Unit = {
    println("drawing")
    config.activeTool.use(drawing, config, event)
    println("elements of active layer: " + config.activeLayer.name + " " + config.activeLayer.elements.mkString(", "))
    //println("elements of drawing's first layer:")
    //println(this.drawing.layers.head.elements.mkString("\n"))
    drawingBackground.getChildren.clear()
    val drawingPane = getUpdatedCanvas
    drawingBackground.add(drawingPane, 1, 1, 1, 1)
    // println("children of drawing background: " + drawingBackground.getChildren) // Should always be a single StackPane
  }


  @FXML def createCanvas(): Unit = {
    println("creating canvas")
    println(drawing)
    val drawingPane = getUpdatedCanvas
    drawingBackground.add(drawingPane, 1, 1, 1, 1)
  }

}
