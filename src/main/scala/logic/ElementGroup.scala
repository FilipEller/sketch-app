package logic

import scalafx.geometry.Point2D
import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color

case class ElementGroup(val elements: Vector[Element], id: String, color: Color,
                 origin: Point2D, rotation: Int = 0, previousVersion: Option[Element] = None, hidden: Boolean = false, deleted: Boolean = false) extends Element {

  def addElement(element: Element) = {
    val newGroup = this.copy(elements = this.elements :+ element, previousVersion = Some(this))
    newGroup
  }

  def removeElement(element: Element) = {
    val newGroup = this.copy(elements = this.elements.filter( _ != element), previousVersion = Some(this))
    newGroup
  }

  def move(newOrigin: Point2D) = {
    val xDiff = newOrigin.x - this.origin.x
    val yDiff = newOrigin.y - this.origin.y
    // cant make element copies with new origin. maybe save offset to elementgroup and give that as parameter
    // to a new paint method of element
    this.copy(origin = newOrigin)
  }

  def rotate(angle: Int) = this.copy(rotation = this.rotation + angle, previousVersion = Some(this))

  def paint(canvas: Canvas) = {
    this.elements.foreach( _.paint(canvas) )
  }

  def collidesWith(point: Point2D): Boolean = this.elements.exists( _.collidesWith(point) )


}
