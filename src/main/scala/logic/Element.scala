package logic

import scalafx.geometry.Point2D
import scalafx.scene.paint.Color
import scalafx.scene.canvas.Canvas

abstract class Element {

  def name: String
  def origin: Point2D
  def rotation: Int
  def color: Color
  def previousVersion: Option[Element]
  def hidden: Boolean
  def deleted: Boolean

  def paint(canvas: Canvas): Unit
  def move(newOrigin: Point2D): Element
  def rotate(angle: Int): Element

  def collidesWith(point: Point2D): Boolean

}
