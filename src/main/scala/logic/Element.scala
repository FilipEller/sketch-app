package logic

import scalafx.geometry.Point2D
import scalafx.scene.paint.Color
import scalafx.scene.canvas.Canvas

abstract class Element {

  def name: String
  def origin: Point2D
  def width: Double
  def height: Double
  def rotation: Int   // TODO: Remove rotation from Element
  def color: Color
  def previousVersion: Option[Element]  // will not be saved. History is bunk.
  def hidden: Boolean // TODO: Remove hidden from Element
  def deleted: Boolean  // if true, element will not be saved

  def paint(canvas: Canvas): Unit
  def collidesWith(point: Point2D): Boolean

  def move(newOrigin: Point2D): Element = {
    this match {
      case e: Shape => e.copy(origin = newOrigin, previousVersion = Some(this))
      case e: Stroke => e.copy(origin = newOrigin, path = e.path.move(newOrigin.x - e.origin.x, newOrigin.y - e.origin.y), previousVersion = Some(this))
      case e: TextBox => e.copy(origin = newOrigin, previousVersion = Some(this))
      case e: Element => e // ElementGroup overrides this method
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