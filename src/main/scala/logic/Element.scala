package logic

import scalafx.geometry.Point2D
import scalafx.scene.paint.Color
import scalafx.scene.canvas.Canvas

abstract class Element {

  def name: String
  def origin: Point2D
  def width: Double
  def height: Double
  def rotation: Int
  def color: Color
  def previousVersion: Option[Element]
  def hidden: Boolean
  def deleted: Boolean

  def paint(canvas: Canvas): Unit
  def collidesWith(point: Point2D): Boolean

  def move(newOrigin: Point2D): Element = {
    this match {
      case e: Shape => e.copy(origin = newOrigin, previousVersion = Some(this))
      case e: Stroke => e.copy(origin = newOrigin, previousVersion = Some(this))
      case e: TextBox => e.copy(origin = newOrigin, previousVersion = Some(this))
      case e: Element => e
    }
  }

  def move(xDiff: Double, yDiff: Double): Element = this.move(new Point2D(this.origin.x + xDiff, this.origin.y + yDiff))

  def rotate(angle: Int): Element = {
    this match {
      case e: Shape => e.copy(rotation = this.rotation + angle, previousVersion = Some(this))
      case e: Stroke => e.copy(rotation = this.rotation + angle, previousVersion = Some(this))
      case e: TextBox => e.copy(rotation = this.rotation + angle, previousVersion = Some(this))
      case e: Element => e
    }
  }

}