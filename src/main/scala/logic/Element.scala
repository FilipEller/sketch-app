package logic

import scalafx.geometry.Point2D
import scalafx.scene.paint.Color
import scalafx.scene.canvas.Canvas

abstract class Element {
  // Element and its subclasses are immutable

  def name: String

  // Position of the Element on the canvas.
  def origin: Point2D
  def width: Double
  def height: Double
  def color: Color

  // The version of this Element prior to the latest change to it.
  // ThIs property not saved to a file.
  // Is None if the Element has not been changed since it was created.
  def previousVersion: Option[Element]

  // If true, the Element will not be rendered
  // nor will it be saved to a file
  def isDeleted: Boolean

  // Render the Element on the given Canvas
  def paint(canvas: Canvas): Unit

  // Checks if the Element encloses the given point on the canvas.
  def collidesWith(point: Point2D): Boolean

  // Returns a new version of this Element with the origin changed to the given one
  def move(newOrigin: Point2D): Element = {
    this match {
      case e: Shape => e.copy(origin = newOrigin, previousVersion = Some(this))
      case e: Stroke => e.copy(origin = newOrigin, path = e.path.move(newOrigin.x - e.origin.x, newOrigin.y - e.origin.y), previousVersion = Some(this))
      case e: TextBox => e.copy(origin = newOrigin, previousVersion = Some(this))
      case e: Element => e // ElementGroup overrides this method
    }
  }

  // Returns a new version of this Element with the origin changed according to the offset
  def move(xDiff: Double, yDiff: Double): Element = this.move(new Point2D(this.origin.x + xDiff, this.origin.y + yDiff))

}