package logic

import scalafx.scene.paint.Color

case class Configurations(activeLayer: Layer, activeTool: Tool, primaryColor: Color, secondaryColor: Color, activeBrush: Brush, selectedElement: Option[Element], fontSize: Int)
