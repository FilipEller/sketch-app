package logic

import scalafx.geometry.Point2D
import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color.rgb
import scalafx.scene.paint.{Color, CycleMethod, RadialGradient, Stop}

import scala.math.abs

case class Stroke(id: String, color: Color, origin: Point2D, path: Path, brush: Brush,
                 rotation: Int = 0, group: Option[Long] = None, previousVersion: Option[Element] = None, hidden: Boolean = false, deleted: Boolean = false) extends Element {

  override def toString: String = "Stroke"

  def paint(canvas: Canvas): Unit = { // there's a problem with drawing less opaque brush strokes.
    val g = canvas.graphicsContext2D  // Brush images are painted too closely for the opacity to have much effect, but painting them further apart would leave each brush image distinctly visible.
    g.setGlobalAlpha(this.color.getOpacity * 0.25) // setting the opacity this way doesn't really help
    val red = math.round(color.getRed * 255).toInt
    val green = math.round(color.getGreen * 255).toInt
    val blue = math.round(color.getBlue * 255).toInt
    val rgbColor = rgb(red, green, blue)
    val gradient = new RadialGradient(
       0, 0,  // focus angle, focus distance
       0.5, 0.5,  // center x, y
       0.5, // radius
       true,  // proportional
       CycleMethod.NoCycle,
       List(Stop(0.0, rgbColor), Stop(1.0, Color.Transparent))
    )
    // rotation not implemented
    g.fill = gradient
    this.path.foreach(point => g.fillOval(point.x, point.y, this.brush.size, this.brush.size))
    g.setGlobalAlpha(1)
  }

  def move(newOrigin: Point2D) = this.copy(origin = newOrigin)

  def rotate(angle: Int) = this.copy(rotation = this.rotation + angle)
}
