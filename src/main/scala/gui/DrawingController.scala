package gui

import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.fxml.FXML
import javafx.event.ActionEvent
import scalafx.scene.layout.{Background, BackgroundFill, CornerRadii, StackPane}
import javafx.scene.layout.StackPane
import logic.{CircleTool, Configurations, Drawing, EllipseTool, RectangleTool, SquareTool, BrushTool}
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

  var drawing: Drawing = _
  var baseCanvas: Canvas = _


  @FXML def updateCanvas(): Unit = {
    pane.children.tail.foreach(pane.children -= _) // empties background except for white base canvas
    drawing.paint(this.pane)

    pane.children.tail.foreach(canvas => {
      canvas.setOnMousePressed(this.draw(_))
      canvas.setOnMouseDragged(this.draw(_))
      canvas.setOnMouseReleased(this.draw(_))
    })
  }

  def updateLayerView(): Unit = {
    val selectedLayer = this.layerView.getSelectionModel.getSelectedItem
    this.layerView.getItems.clear()
    this.drawing.layers.reverse.foreach( l => this.layerView.getItems.add(l.name) )
    this.layerView.getSelectionModel.select(selectedLayer)
  }

  def selectLayer(new_val: String) = {
    println("selecting " + new_val)
    val layer = this.drawing.findLayer(new_val)
    this.drawing.config = this.drawing.config.copy(activeLayer = layer.getOrElse(this.drawing.config.activeLayer))
  }

  def initializeLayerView(): Unit = {
    import javafx.beans.value.ChangeListener
    this.layerView.getSelectionModel.selectedItemProperty.addListener(new ChangeListener[String]() {
      override def changed(observableValue: ObservableValue[_ <: String], old_val: String, new_val: String): Unit = {
        /*
        new_val is null after a layer is removed
        Drawing.findLayer returns Option so this will not cause an error
        Controller's removeLayer also selects another layer
        */
        selectLayer(new_val)
      }
    })
    updateLayerView()
    layerView.getSelectionModel.select(0)
  }

  @FXML def draw(event: MouseEvent): Unit = {
    val localPoint = new Point2D(baseCanvas.screenToLocal(event.getScreenX, event.getScreenY))
    this.drawing.useTool(event, localPoint)
    println("elements of active layer: " + drawing.config.activeLayer.name + " " + drawing.config.activeLayer.elements.mkString(", "))
    updateCanvas()
  }

  def initController(): Unit = {
    println("initializing canvas")

    this.baseCanvas = new Canvas(drawing.width, drawing.height)
    pane.children += baseCanvas

    val g = baseCanvas.graphicsContext2D
    g.fill = White
    g.fillRect(0, 0, this.drawing.width, this.drawing.height)

    initializeLayerView()
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
      case "Brush" => BrushTool
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
    this.drawing.addLayer()
    updateLayerView()
  }

  // Bug: If all layers are removed, drawing no longer works even after readding layers.
  @FXML protected def removeLayer(event: ActionEvent) = {
    println("removing layer")
    val layerIndex = this.layerView.getSelectionModel.getSelectedIndex
    val layerName = this.layerView.getSelectionModel.getSelectedItem
    println(layerName)
    val layer = this.drawing.removeLayer(layerName)
    updateLayerView()
    this.layerView.getSelectionModel.select(math.max(layerIndex, 0)) // select the layer under the removed one
  }

  // not implemented
  @FXML protected def renameLayer(event: ActionEvent) = {
    println("renaming layer")
    // dialogue for new name
    updateLayerView()
  }

}
