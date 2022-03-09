package logic

import scalafx.scene.paint.Color

case class Configurations(activeLayer: Layer, activeTool: Tool, primaryColor: Color, secondaryColor: Color, activeBrush: Long, selectedElement: Option[Element], fontSize: Int)
