package logic

import scalafx.scene.paint.Color
import scalafx.scene.canvas.Canvas

abstract class Element {

  def id: String
  def origin: Point
  def rotation: Int
  def color: Color
  def group: Option[Long]
  def previousVersion: Option[Element]
  def hidden: Boolean
  def deleted: Boolean

  def paint(canvas: Canvas): Unit
  def move(newOrigin: Point): Element
  def rotate(angle: Int): Element

}
