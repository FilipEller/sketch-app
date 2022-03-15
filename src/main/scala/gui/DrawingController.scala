package gui

import javafx.fxml.FXML
import javafx.event.ActionEvent
import scalafx.scene.layout.{Background, BackgroundFill, CornerRadii, StackPane}
import javafx.scene.layout.StackPane
import logic.{Configurations, Drawing}
import scalafx.Includes._
import scalafx.geometry.{Insets, Point2D}
import scalafx.scene.Node
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.Button
import scalafx.scene.input.{MouseDragEvent, MouseEvent}
import scalafx.scene.paint.Color.Gray

class DrawingController {


  @FXML var drawingBackground: javafx.scene.layout.StackPane = _

  var drawing: Drawing = _
  var config: Configurations = _
  var baseCanvas: Node = _

  @FXML protected def changeToRectangleTool(event: ActionEvent): Unit = {
    println("changing to RectangleTool")
  }

  @FXML def testFunction(value: Int): Unit = {
    println("test function ran with parameter " + value)
    println(drawing)
    println(config)
    println(drawingBackground)
  }

  @FXML def getUpdatedCanvas: scalafx.scene.layout.StackPane = {
    println("making a new canvas for GUI")
    // paint a new pane
    val drawingPane = drawing.paint(new scalafx.scene.layout.StackPane)

    // set event listeners to pane
    drawingPane.setOnMousePressed(this.draw(_))
    drawingPane.setOnMouseDragged(this.draw(_))
    drawingPane.setOnMouseReleased(this.draw(_))

    drawingPane.setOnDragDetected(e => drawingPane.startFullDrag())

    drawingPane
  }

    @FXML def updateCanvas(): Unit = {
    println("making a new canvas for GUI")
    // paint a new pane
    drawing.paint(this.drawingBackground)

    drawingBackground.children.foreach( canvas => {
      canvas.setOnMousePressed(this.newDraw(_))
      canvas.setOnMouseDragged(this.newDraw(_))
      canvas.setOnMouseReleased(this.newDraw(_))
      canvas.setOnDragDetected(e => canvas.startFullDrag())
    })
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
    //val offset = new Point2D((drawingBackground.getLayoutBounds.width - drawing.width - 2 * this.drawing.currentImage.getLayoutX) / 2, (drawingBackground.getLayoutBounds.height - drawing.height - this.drawing.currentImage.getLayoutY) / 2)
    //val eventPoint = new Point2D(drawing.currentImage.screenToLocal(event.getScreenX, event.getScreenY).subtract(offset))

    val localBounds = this.drawingBackground.localToScene(this.drawingBackground.getLayoutBounds)
    val clickPoint = new Point2D(event.getSceneX, event.getSceneY)
    val boundsOrigin = new Point2D(localBounds.getMinX, localBounds.getMinX)
    val drawingBounds = this.drawingBackground.children(0).getLayoutBounds
    val drawingOffset =  new Point2D(drawingBounds.getMinX, drawingBounds.getMinX) // new Point2D((localBounds.width - drawing.width) / 2.0, 1.5 * (localBounds.height - drawing.height))
    val eventPoint = clickPoint.subtract(boundsOrigin).subtract(drawingOffset)
    println(localBounds)
    println(clickPoint)
    println(boundsOrigin)
    println(drawingBounds)
    println(drawingOffset)
    println(eventPoint)

    // println("offset: " + offset)
    config.activeTool.use(drawing, config, event, eventPoint)
    println("elements of active layer: " + config.activeLayer.name + " " + config.activeLayer.elements.mkString(", "))
    //println("elements of drawing's first layer:")
    //println(this.drawing.layers.head.elements.mkString("\n"))
    drawingBackground.getChildren.clear()
    val drawingPane = getUpdatedCanvas
    drawingBackground.children += drawingPane
    // println("children of drawing background: " + drawingBackground.getChildren) // Should always be a single StackPane
  }

  @FXML def newDraw(event: MouseEvent): Unit = {
    println("drawing")
    val gridBounds = drawingBackground.getLayoutBounds
    println("bounds of grid: " + gridBounds)
    println("scene bounds: " + drawingBackground.localToScene(gridBounds))
    println("screen: " + drawingBackground.localToScreen(gridBounds.getMinX, gridBounds.getMinX))

    val drawBounds = baseCanvas.getLayoutBounds
    println("bounds of drawing: " + drawBounds)
    println("scene bounds: " + baseCanvas.localToScene(drawBounds))
    println("scene: " + baseCanvas.localToScene(drawBounds.getMinX, drawBounds.getMinX))
    println("screen: " + baseCanvas.localToScreen(drawBounds.getMinX, drawBounds.getMinX))

    val localPoint = new Point2D(baseCanvas.screenToLocal(event.getScreenX, event.getScreenY))
    println("click point in drawing local: " + localPoint)
    //val scenePoint = drawing.currentImage.localToScene(localPoint)
    //println("scene point by drawing: " + scenePoint)
    //println("scene poin by grid: " + drawingBackground.localToScene(localPoint))

    // Event coordinates in drawing's local coordinate system.
    //val offset = new Point2D((drawingBackground.getLayoutBounds.width - drawing.width - 2 * this.drawing.currentImage.getLayoutX) / 2, (drawingBackground.getLayoutBounds.height - drawing.height - this.drawing.currentImage.getLayoutY) / 2)
    //val eventPoint = new Point2D(drawing.currentImage.screenToLocal(event.getScreenX, event.getScreenY).subtract(offset))
/*
    val localBounds = this.drawingBackground.localToScene(this.drawingBackground.getLayoutBounds)
    val clickPoint = new Point2D(event.getSceneX, event.getSceneY)
    val boundsOrigin = new Point2D(localBounds.getMinX, localBounds.getMinX)
    val drawingBounds = this.drawingBackground.children(0).getLayoutBounds
    val drawingOffset =  new Point2D(drawingBounds.getMinX, drawingBounds.getMinX) // new Point2D((localBounds.width - drawing.width) / 2.0, 1.5 * (localBounds.height - drawing.height))
    val eventPoint = clickPoint.subtract(boundsOrigin).subtract(drawingOffset)
    println(localBounds)
    println(clickPoint)
    println(boundsOrigin)
    println(drawingBounds)
    println(drawingOffset)
    println(eventPoint)

 */

    // println("offset: " + offset)
    config.activeTool.use(drawing, config, event, localPoint)
    println("elements of active layer: " + config.activeLayer.name + " " + config.activeLayer.elements.mkString(", "))
    //println("elements of drawing's first layer:")
    //println(this.drawing.layers.head.elements.mkString("\n"))
    updateCanvas()
    /*
    drawingBackground.getChildren.clear()
    val drawingPane = getUpdatedCanvas
    drawingBackground.children += drawingPane

     */
    // println("children of drawing background: " + drawingBackground.getChildren) // Should always be a single StackPane
  }


  @FXML def createCanvas(): Unit = {
    println("creating canvas")
    println(drawing)
    val drawingPane = getUpdatedCanvas
    drawingBackground.children += drawingPane
  }

  @FXML def newCreateCanvas(): Unit = {
    println("initializing canvas")
    println(drawing)

    drawingBackground.children += new Canvas(drawing.width, drawing.height)
    this.baseCanvas = drawingBackground.children.head

    /*
    drawingBackground.setOnMousePressed(this.newDraw(_))
    drawingBackground.setOnMouseDragged(this.newDraw(_))
    drawingBackground.setOnMouseReleased(this.newDraw(_))
    drawingBackground.setOnDragDetected(e => drawingBackground.startFullDrag())
     */
    updateCanvas()
    // add eventlisteners to drawingbackground
  }

}
