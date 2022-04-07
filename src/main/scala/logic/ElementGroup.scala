package logic

import scalafx.geometry.Point2D
import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.rgb

case class ElementGroup(elements: Seq[Element], origin: Point2D, color: Color, name: String,
                        rotation: Int = 0, previousVersion: Option[Element] = None, hidden: Boolean = false, deleted: Boolean = false) extends Element {

  val width = this.elements.map( e => e.origin.x + e.width ).max - this.origin.x
  val height = this.elements.map( e => e.origin.y + e.height ).max - this.origin.y

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
      case e: ElementGroup => e.move(xDiff, yDiff)
      case e: Element => e
    }
    this.copy(origin = newOrigin, elements = newElements, previousVersion = Some(this))
  }

  override def rotate(angle: Int) = this.copy(rotation = this.rotation + angle, previousVersion = Some(this))

  def paint(canvas: Canvas) = {
    this.elements.foreach( _.paint(canvas) )
  }

  def collidesWith(point: Point2D): Boolean = this.elements.exists( _.collidesWith(point) )

}

object ElementGroup {

  var groupCount = 0
  
  def nameToUse(name: String) = {
    if (name == "") {
      groupCount += 1
      s"Group $groupCount"
    } else {
      name
    }
  }

  def apply(elements: Seq[Element], origin: Point2D, color: Color, name: String = "", rotation: Int = 0, previousVersion: Option[Element] = None, hidden: Boolean = false, deleted: Boolean = false): ElementGroup = {
    new ElementGroup(elements, origin, color, nameToUse(name), rotation, previousVersion, hidden, deleted)
  }
  
  def apply(elements: Seq[Element]): ElementGroup = {
    
    val originX = elements.map( _.origin.x ).min
    val originY = elements.map( _.origin.y ).min
    val origin = new Point2D(originX, originY)

    val color = elements.headOption.map( _.color ).getOrElse(rgb(0, 0, 0))

    ElementGroup(elements, origin, color)
  }
}
