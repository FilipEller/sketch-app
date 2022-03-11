package gui

import javafx.fxml.FXML
import javafx.event.ActionEvent
import scalafx.scene.layout.{Background, BackgroundFill, CornerRadii, StackPane}
import javafx.scene.layout.GridPane
import logic.{Configurations, Drawing}
import scalafx.Includes._
import scalafx.geometry.{Insets, Point2D}
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
    val gridBounds = drawingBackground.getLayoutBounds
    println("bounds of grid: " + gridBounds)
    println("scene bounds: " + drawingBackground.localToScene(gridBounds))
    println("screen: " + drawingBackground.localToScreen(gridBounds.getMinX, gridBounds.getMinX))

    val drawBounds = drawing.currentImage.getLayoutBounds
    println("bounds of drawing: " + drawBounds)
    println("scene bounds: " + drawing.currentImage.localToScene(drawBounds))
    println("scene: " + drawing.currentImage.localToScene(drawBounds.getMinX, drawBounds.getMinX))
    println("screen: " + drawing.currentImage.localToScreen(drawBounds.getMinX, drawBounds.getMinX))

    val localPoint = new Point2D(drawing.currentImage.screenToLocal(event.getScreenX, event.getScreenY))
    println("click point in drawing local: " + localPoint)
    val scenePoint = drawing.currentImage.localToScene(localPoint)
    println("scene point by drawing: " + scenePoint)
    //println("scene poin by grid: " + drawingBackground.localToScene(localPoint))

    // Event coordinates in drawing's local coordinate system.
    val offset = new Point2D((drawingBackground.getLayoutBounds.width - drawing.width - 2 * this.drawing.currentImage.getLayoutX) / 2, (drawingBackground.getLayoutBounds.height - drawing.height - this.drawing.currentImage.getLayoutY) / 2)
    val eventPoint = new Point2D(drawing.currentImage.screenToLocal(event.getScreenX, event.getScreenY).subtract(offset))

    println("offset: " + offset)
    config.activeTool.use(drawing, config, event, eventPoint)
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
