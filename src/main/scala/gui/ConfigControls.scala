package gui

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.ColorPicker
import logic._
import scalafx.scene.control.Button
import scalafx.scene.paint.Color.rgb

object ConfigControls {

  def changeTool(event: ActionEvent, drawing: Drawing): Unit = {
    val button: scalafx.scene.control.Button = new Button(event.getTarget.asInstanceOf[javafx.scene.control.Button])
    val label = button.getId
    println("button pressed: " + label)
    val targetTool = label match {
      case "Select" => SelectionTool
      case "Transform" => TransformTool
      case "Brush" => BrushTool
      case "Line" => LineTool
      case "Rectangle" => RectangleTool
      case "Square" => SquareTool
      case "Ellipse" => EllipseTool
      case "Circle" => CircleTool
      case "Text" => TextTool
      case _ => SelectionTool
    }
    drawing.config = drawing.config.copy(activeTool = targetTool)
  }




}
