package logic

import scalafx.geometry.Point2D
import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.{Color, CycleMethod, RadialGradient, Stop}

case class Stroke(color: Color,
                  origin: Point2D,
                  path: Path,
                  brush: Brush,
                  name: String,
                  previousVersion: Option[Element] = None,
                  isDeleted: Boolean = false) extends Element {

  val width = this.path.map( p => p.x - this.origin.x).max + this.brush.size
  val height = this.path.map( p => p.y - this.origin.y).max + this.brush.size

  override def toString: String = this.name

  // Render the Element
  // Stroke is rendered by painting a circle at each Point in this.Path
  def paint(canvas: Canvas): Unit = {
    if (!this.isDeleted) {
      val g = canvas.graphicsContext2D
      val hardness = this.brush.hardness / 100.0

      // The opacity of a single circle
      val targetOpacity = math.pow(this.color.opacity, 1.5) / (1.25 * math.pow(this.brush.size, 0.5))
      // The opacity must be between 0.01 and 1
      val opacity = if (targetOpacity > 1) 1 else if (targetOpacity < 0.01) 0.01 else targetOpacity
      val usedColor = new Color(this.color.opacity(opacity))
      // The diameter of a circle must be greater than 0
      val size = if (this.brush.size > 0) this.brush.size else 1
      // A radial gradient to soften the circles
      val gradient =
        new RadialGradient(
          0, 0,                // focus angle, focus distance
          0.5, 0.5,            // center x, y
          0.5,                 // radius
          true,                // proportional
          CycleMethod.NoCycle,
          if (hardness > 0 && hardness <= 1) {
            List(Stop(0.0, usedColor), Stop(hardness, usedColor), Stop(1.0, Color.Transparent))
          } else {
            List(Stop(0.0, usedColor), Stop(1.0, Color.Transparent))
          }
      )
      g.fill = if (this.brush.hardness == 100 && this.color.opacity == 1) this.color else gradient
      this.path.foreach(point => g.fillOval(point.x - 0.5 * size, point.y - 0.5 * size, size, size))
    }
  }

  def collidesWith(point: Point2D): Boolean = // Only takes the bounding box into account
    (point.x >= this.origin.x - 0.5 * this.brush.size
      && point.x <= this.origin.x - 0.5 * this.brush.size + this.width
      && point.y >= this.origin.y - 0.5 * this.brush.size
      && point.y <= this.origin.y - 0.5 * this.brush.size + this.height)

}

object Stroke {

  // Counts the number of Strokes created during this run of the program
  // The count is used to name new Strokes uniquely.
  var strokeCount = 0

  def apply(color: Color, origin: Point2D, path: Path, brush: Brush, name: String = "",
                 previousVersion: Option[Element] = None, deleted: Boolean = false) = {
    val nameToUse = {
      if (name == "") {
        strokeCount += 1
        s"Stroke $strokeCount"
      } else {
        name
      }
    }

    new Stroke(color, origin, path, brush, nameToUse, previousVersion, deleted)
  }

}
