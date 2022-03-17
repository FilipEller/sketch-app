package logic

import scalafx.geometry.Point2D
import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.{Color, CycleMethod, RadialGradient, Stop}

case class Stroke(id: String, color: Color, origin: Point2D, path: Vector[Point2D], brush: Brush,
                 rotation: Int = 0, group: Option[Long] = None, previousVersion: Option[Element] = None, hidden: Boolean = false, deleted: Boolean = false) extends Element {

  def paint(canvas: Canvas): Unit = {
    val g = canvas.graphicsContext2D
    val gradient = new RadialGradient(
       0, 0,  // focus angle, focus distance
       0.5, 0.5,  // center x, y
       0.5, // radius
       true,  // proportional
       CycleMethod.NoCycle,
       List(Stop(0.0, this.color), Stop(1.0, Color.Transparent))
    )
    // rotation not implemented
    g.fill = gradient
    for (point <- this.path) {
      g.fillOval(origin.x, origin.y, this.brush.size, this.brush.size)
    }
  }

  def move(newOrigin: Point2D) = this.copy(origin = newOrigin)

  def rotate(angle: Int) = this.copy(rotation = this.rotation + angle)
}
