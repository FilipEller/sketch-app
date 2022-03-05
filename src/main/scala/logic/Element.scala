package logic

import scalafx.scene.paint.Color
import scalafx.scene.canvas.Canvas

abstract case class Element(id: String, origin: Point, rotation: Int, color: Color,
 group: Option[Long], previousVersion: Option[Element], hidden: Boolean, deleted: Boolean) {

  def paint(canvas: Canvas): Unit

  def move(newOrigin: Point): Element

  def rotate(angle: Int): Element

}
