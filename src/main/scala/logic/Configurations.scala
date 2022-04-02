package logic

import scalafx.scene.paint.Color

case class Configurations(activeLayer: Layer,
                          activeTool: Tool,
                          primaryColor: Color,
                          secondaryColor: Color,
                          useBorder: Boolean,
                          useFill: Boolean,
                          activeBrush: Brush,
                          borderWidth: Int,
                          selectedElements: Seq[Element],
                          fontSize: Int)
