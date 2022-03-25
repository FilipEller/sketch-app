package logic

import scalafx.geometry.Point2D
import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color

case class ElementGroup(val elements: Vector[Element], id: String, name: String, color: Color,
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
    val newElements = this.elements.map {
      case e: Shape => e.copy(origin = new Point2D(e.origin.x + xDiff, e.origin.y + yDiff))
      case e if e.isInstanceOf[Stroke] => e
      case e if e.isInstanceOf[TextBox] => e
    }
    this.copy(origin = newOrigin, elements = newElements)
  }

  def rotate(angle: Int) = this.copy(rotation = this.rotation + angle, previousVersion = Some(this))

  def paint(canvas: Canvas) = {
    this.elements.foreach( _.paint(canvas) )
  }

  def collidesWith(point: Point2D): Boolean = this.elements.exists( _.collidesWith(point) )


}
