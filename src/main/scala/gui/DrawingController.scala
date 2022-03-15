package gui

import javafx.fxml.FXML
import javafx.event.ActionEvent
import scalafx.scene.layout.{Background, BackgroundFill, CornerRadii, StackPane}
import javafx.scene.layout.StackPane
import logic.{CircleTool, Configurations, Drawing, EllipseTool, RectangleTool, SquareTool}
import scalafx.Includes._
import scalafx.geometry.{Insets, Point2D}
import scalafx.scene.Node
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.Button
import scalafx.scene.input.{MouseDragEvent, MouseEvent}
import scalafx.scene.paint.Color.{Blue, White, rgb}
import javafx.scene.control.{ColorPicker, ListView}
import scalafx.collections.ObservableBuffer

import scala.math

class DrawingController {


  @FXML var pane: javafx.scene.layout.StackPane = _
  @FXML var layerView: javafx.scene.control.ListView[String] = _
  // @FXML var primaryColorPicker: javafx.scene.control.ColorPicker = _

  var drawing: Drawing = _
  var baseCanvas: Canvas = _


    @FXML def updateCanvas(): Unit = {
    pane.children.tail.foreach(pane.children -= _) // empties background except for empty base canvas
    drawing.paint(this.pane)

    pane.children.tail.foreach(canvas => {
      canvas.setOnMousePressed(this.draw(_))
      canvas.setOnMouseDragged(this.draw(_))
      canvas.setOnMouseReleased(this.draw(_))
      // canvas.setOnDragDetected(e => canvas.startFullDrag())
    })
  }

  @FXML def draw(event: MouseEvent): Unit = {
    val localPoint = new Point2D(baseCanvas.screenToLocal(event.getScreenX, event.getScreenY))
    drawing.config.activeTool.use(drawing, drawing.config, event, localPoint)
    println("elements of active layer: " + drawing.config.activeLayer.name + " " + drawing.config.activeLayer.elements.mkString(", "))
    updateCanvas()
  }

  @FXML def newCreateCanvas(): Unit = {
    println("initializing canvas")

    this.baseCanvas = new Canvas(drawing.width, drawing.height)
    pane.children += baseCanvas

    val g = baseCanvas.graphicsContext2D
    g.fill = White
    g.fillRect(0, 0, this.drawing.width, this.drawing.height)

    this.drawing.layers.foreach( l => this.layerView.getItems.add(l.name) )
    updateCanvas()
  }


  // Configurations

  // activeTool
  @FXML protected def changeTool(event: ActionEvent): Unit = {
    val button: scalafx.scene.control.Button = new Button(event.getTarget.asInstanceOf[javafx.scene.control.Button])
    val label = button.getText
    println("button pressed: " + label)
    val targetTool = label match {
      case "Select" => RectangleTool
      case "Transform" => RectangleTool
      case "Brush" => RectangleTool
      case "Rectangle" => RectangleTool
      case "Square" => SquareTool
      case "Ellipse" => EllipseTool
      case "Circle" => CircleTool
      case "Text" => RectangleTool
      case _ => RectangleTool
    }
    drawing.config = drawing.config.copy(activeTool = targetTool)
  }

  // Colors
  @FXML protected def changeColor(event: ActionEvent): Unit = {
    println(event)
    println(event.getTarget)
    val picker = event.getTarget.asInstanceOf[ColorPicker]
    val color = picker.getValue
    val id = picker.getId
    println(id)
    println(color)
    val red = math.round(color.getRed * 255).toInt
    val green = math.round(color.getGreen * 255).toInt
    val blue = math.round(color.getBlue * 255).toInt
    val opacity = color.getOpacity
    val rgbColor = rgb(red, green, blue, opacity)
    println(red, green, blue, opacity)
    println(rgbColor)
    id match {
      case "primaryColorPicker" => drawing.config = drawing.config.copy(primaryColor = rgbColor)
      case "secondaryColorPicker" => drawing.config = drawing.config.copy(secondaryColor = rgbColor)
    }
  }

  // activeLayer
  // activeBrush
  // selectedElement
  // fontSize

  // Layers

  @FXML protected def addLayer(event: ActionEvent) = {
    println("adding layer")
  }

  @FXML protected def selectLayer(event: ActionEvent) = {
    println("selecting layer")
    // this.drawing.config.activeLayer = target
  }

  @FXML protected def removeLayer(event: ActionEvent) = {
    println("removing layer")
  }

  @FXML protected def renameLayer(event: ActionEvent) = {
    println("renaming layer")
  }

}
