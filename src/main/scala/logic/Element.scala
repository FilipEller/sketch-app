package logic

import scalafx.scene.paint.Color
import scalafx.scene.canvas.Canvas

abstract case class Element(id: String, origin: Point, rotation: Int, hidden: Boolean,
deleted: Boolean, group: Option[Long], color: Color, previousVersion: Option[Element]) {

  def paint(canvas: Canvas): Unit

  def move(newOrigin: Point): Element

  def rotate(angle: Int): Element

}
