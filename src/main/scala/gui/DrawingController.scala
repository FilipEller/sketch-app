package gui

import javafx.fxml.FXML
import javafx.event.ActionEvent
import scalafx.scene.layout.{Background, BackgroundFill, CornerRadii, StackPane}
import javafx.scene.layout.StackPane
import logic.{Configurations, Drawing, RectangleTool}
import scalafx.Includes._
import scalafx.geometry.{Insets, Point2D}
import scalafx.scene.Node
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.Button
import scalafx.scene.input.{MouseDragEvent, MouseEvent}
import scalafx.scene.paint.Color.Gray

class DrawingController {


  @FXML var pane: javafx.scene.layout.StackPane = _

  var drawing: Drawing = _
  var config: Configurations = _
  var baseCanvas: Node = _


    @FXML def updateCanvas(): Unit = {
    pane.children.tail.foreach(pane.children -= _) // empties background except for empty base canvas
    drawing.paint(this.pane)

    pane.children.tail.foreach(canvas => {
      canvas.setOnMousePressed(this.draw(_))
      canvas.setOnMouseDragged(this.draw(_))
      canvas.setOnMouseReleased(this.draw(_))
      canvas.setOnDragDetected(e => canvas.startFullDrag())
    })
  }

  @FXML def draw(event: MouseEvent): Unit = {
    val localPoint = new Point2D(baseCanvas.screenToLocal(event.getScreenX, event.getScreenY))
    config.activeTool.use(drawing, config, event, localPoint)
    println("elements of active layer: " + config.activeLayer.name + " " + config.activeLayer.elements.mkString(", "))
    updateCanvas()
  }

  @FXML def newCreateCanvas(): Unit = {
    println("initializing canvas")
    pane.children += new Canvas(drawing.width, drawing.height)
    this.baseCanvas = pane.children.head

    updateCanvas()
  }


  // Configurations


  @FXML protected def changeTool(event: ActionEvent): Unit = {
    val button: scalafx.scene.control.Button = new Button(event.getTarget.asInstanceOf[javafx.scene.control.Button])
    val label = button.getText
    println("button pressed: " + label)
    val targetTool = label match {
      case "Select" => RectangleTool
      case "Transform" => RectangleTool
      case "Brush" => RectangleTool
      case "Rectangle" => RectangleTool
      case "Square" => RectangleTool
      case "Ellipse" => RectangleTool
      case "Circle" => RectangleTool
      case "Text" => RectangleTool
      case _ => RectangleTool
    }
    drawing.config = drawing.config.copy(activeTool = targetTool)
  }

  // activeLayer
  // activeTool
  // primaryColor
  // secondaryColor
  // activeBrush
  // selectedElement
  // fontSize

}
