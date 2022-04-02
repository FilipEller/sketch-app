package logic

import scalafx.geometry.Point2D
import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color

case class ElementGroup(val elements: Vector[Element], id: String, name: String, color: Color,
                 origin: Point2D, rotation: Int = 0, previousVersion: Option[Element] = None, hidden: Boolean = false, deleted: Boolean = false) extends Element {

  val width = 100
  val height = 100

  def addElement(element: Element) = {
    val newGroup = this.copy(elements = this.elements :+ element, previousVersion = Some(this))
    newGroup
  }

  def removeElement(element: Element) = {
    val newGroup = this.copy(elements = this.elements.filter( _ != element), previousVersion = Some(this))
    newGroup
  }

  override def move(newOrigin: Point2D) = {
    val xDiff = newOrigin.x - this.origin.x
    val yDiff = newOrigin.y - this.origin.y
    val newElements = this.elements.map {
      case e: Shape => e.move(xDiff, yDiff)
      case e: Stroke => e.move(xDiff, yDiff)
      case e: TextBox => e.move(xDiff, yDiff)
      case e: Element => e
    }
    this.copy(origin = newOrigin, elements = newElements)
  }

  override def rotate(angle: Int) = this.copy(rotation = this.rotation + angle, previousVersion = Some(this))

  def paint(canvas: Canvas) = {
    this.elements.foreach( _.paint(canvas) )
  }

  def collidesWith(point: Point2D): Boolean = this.elements.exists( _.collidesWith(point) )


}
