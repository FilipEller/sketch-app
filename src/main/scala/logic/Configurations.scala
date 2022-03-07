package logic

import scalafx.scene.paint.Color

case class Configurations(activeLayer: Layer, activeTool: Long, activeColor: Color, activeBrush: Long, selectedElement: Option[Element], fontSize: Int)
