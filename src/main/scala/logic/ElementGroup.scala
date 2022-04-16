package logic

import scalafx.geometry.Point2D
import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.rgb

case class ElementGroup(elements: Seq[Element],
                        color: Color,
                        name: String,
                        previousVersion: Option[Element] = None,
                        deleted: Boolean = false) extends Element {

  // Functionality:
  // selected group is the most recently selected group in selected elements
  // group view contains elements of the selected group

  // Group selected elements (Button "Group" in Selected View)
  // Disassemble a group (Button "Ungroup" in Selected View)

  // Add selected elements (other than the group itself) to selected group (Button "Add" in Group view)
  // Remove elements selected in group view from selected group (Button "Remove" in Group view)
  // Rename selected group (Button "Rename" in Group view)

  val origin = {
    if (this.elements.nonEmpty) {
      new Point2D (this.elements.map(_.origin.x).min, this.elements.map(_.origin.y).min)
    } else {
      this.previousVersion.map(_.origin).getOrElse(new Point2D(0, 0))
    }
  }
  val width = {
    if (this.elements.nonEmpty) {
      this.elements.map( e => e.origin.x + e.width ).max - this.origin.x
    } else {
      this.previousVersion.map(_.width).getOrElse(0)
    }
  }
  val height = {
    if (this.elements.nonEmpty) {
      this.elements.map( e => e.origin.y + e.height ).max - this.origin.y
    } else {
      this.previousVersion.map(_.height).getOrElse(0)
    }
  }

  override def toString = s"$name $elements"

  def addElement(element: Element) = {
    if (element != this) {
      this.copy(elements = this.elements :+ element, previousVersion = Some(this))
    } else {
      this
    }
  }

  def addElements(elements: Seq[Element]): ElementGroup = {
    val elementsToAdd = elements.filter( _ != this )
    this.copy(elements = this.elements ++ elementsToAdd, previousVersion = Some(this))
  }

  def removeElement(element: Element): ElementGroup = {
    this.copy(elements = this.elements.filter( _ != element), previousVersion = Some(this))
  }

  def removeElements(elements: Seq[Element]): ElementGroup = {
    this.copy(elements = this.elements.filter(!elements.contains(_)), previousVersion = Some(this))
  }

  def removeByName(name: String): ElementGroup = {
    this.copy(elements = this.elements.filter( _.name != name), previousVersion = Some(this))
  }

  def findByName(name: String): Option[Element] = {
    this.elements.find(_.name == name)
  }

  def findManyByName(names: Seq[String]): Seq[Element] = {
    names.flatMap(findByName)
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
    this.copy(elements = newElements, previousVersion = Some(this))
  }

  def paint(canvas: Canvas) = {
    if (!this.deleted) {
      this.elements.foreach( _.paint(canvas) )
    }
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

  def apply(elements: Seq[Element], color: Color, name: String = "",
            previousVersion: Option[Element] = None, deleted: Boolean = false): ElementGroup = {
    new ElementGroup(elements, color, nameToUse(name), previousVersion, deleted)
  }
  
  def apply(elements: Seq[Element]): ElementGroup = {
    val color = elements.headOption.map( _.color ).getOrElse(rgb(0, 0, 0))
    ElementGroup(elements, color)
  }

  // TODO: Undo not working with grouping
}
