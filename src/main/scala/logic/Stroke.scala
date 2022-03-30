package logic

import scalafx.geometry.Point2D
import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color.rgb
import scalafx.scene.paint.{Color, CycleMethod, RadialGradient, Stop}

import scala.math.abs

case class Stroke(color: Color, origin: Point2D, path: Path, brush: Brush, name: String,
                 rotation: Int = 0, previousVersion: Option[Element] = None, hidden: Boolean = false, deleted: Boolean = false) extends Element {

  val width = this.path.map( p => this.origin.x - p.x).max
  val height = this.path.map( p => this.origin.y - p.y).max

  override def toString: String = "Stroke"

  def paint(canvas: Canvas): Unit = { // there's a problem with drawing less opaque brush strokes.
    val g = canvas.graphicsContext2D  // Brush images are painted too closely for the opacity to have much effect, but painting them further apart would leave each brush image distinctly visible.
    val targetOpacity = math.pow(this.color.opacity, 1.5) / (this.brush.hardness / 100.0 * 0.3 * this.brush.size) // this is fixing opacity with brush size 30 but other sizes have to be tested. With this 5 % opacity is actually invisible though.
    val opacity = if (targetOpacity > 1) 1 else if (targetOpacity < 0.01) 0.01 else targetOpacity
    val usedColor = new Color(this.color.opacity(opacity))
    val gradient = new RadialGradient( // this should take brush hardness into account somewhere.
       0, 0,  // focus angle, focus distance
       0.5, 0.5,  // center x, y
       0.5, // radius
       true,  // proportional
       CycleMethod.NoCycle,
       List(Stop(0.0, usedColor), Stop(1.0, Color.Transparent))
    )
    // rotation not implemented
    g.fill = gradient
    this.path.foreach(point => g.fillOval(point.x - 0.5 * this.brush.size, point.y - 0.5 * this.brush.size, this.brush.size, this.brush.size))
  }

  def move(newOrigin: Point2D) = this.copy(origin = newOrigin, previousVersion = Some(this))
  def move(xDiff: Double, yDiff: Double) = this.copy(origin = new Point2D(this.origin.x + xDiff, this.origin.y + yDiff), previousVersion = Some(this))
  def rotate(angle: Int) = this.copy(rotation = this.rotation + angle, previousVersion = Some(this))

  def collidesWith(point: Point2D): Boolean = // only takes the bounding box into account
    (point.x >= this.origin.x - 0.5 * this.brush.size
      && point.x <= this.origin.x + this.width + 0.5 * this.brush.size
      && point.y >= this.origin.y - 0.5 * this.brush.size
      && point.y <= this.origin.y + this.height + 0.5 * this.brush.size)
      // does not take rotation into account yet

}

object Stroke {

  var strokeCount = 0

  def apply(color: Color, origin: Point2D, path: Path, brush: Brush, name: String = "",
                 rotation: Int = 0, previousVersion: Option[Element] = None,
                  hidden: Boolean = false, deleted: Boolean = false) = {


    val nameToUse = {
      if (name == "") {
        strokeCount += 1
        s"Stroke $strokeCount"
      } else {
        name
      }
    }

    new Stroke(color, origin, path, brush, nameToUse, rotation, previousVersion, hidden, deleted)
  }

}
