package logic

import scalafx.scene.paint.Color

case class Configurations(activeLayer: Layer,
                          activeTool: Tool,
                          primaryColor: Color,
                          secondaryColor: Color,
                          useBorder: Boolean,
                          useFill: Boolean,
                          activeBrush: Brush,
                          selectedElements: Vector[Element],
                          fontSize: Int)
