package logic

import scalafx.geometry.Point2D
import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.rgb

case class ElementGroup(elements: Seq[Element],
                        color: Color,
                        name: String,
                        previousVersion: Option[Element] = None,
                        isDeleted: Boolean = false) extends Element {

  // The origin closes to the top-left corner among this Group's Elements
  // If the Group is empty, a default value of (0, 0) is returned
  val origin = {
    if (this.elements.nonEmpty) {
      new Point2D (this.elements.map(_.origin.x).min, this.elements.map(_.origin.y).min)
    } else {
      this.previousVersion.map(_.origin).getOrElse(new Point2D(0, 0))
    }
  }

  // The width of bounding box that encloses all this Group's Elements
  // If the Group is empty, the previous version's width is returned.
  // If that does not exist, 0 is returned.
  val width = {
    if (this.elements.nonEmpty) {
      this.elements.map( e => e.origin.x + e.width ).max - this.origin.x
    } else {
      this.previousVersion.map(_.width).getOrElse(0)
    }
  }

  // The height of bounding box that encloses all this Group's Elements
  // If the Group is empty, the previous version's height is returned.
  // If that does not exist, 0 is returned.
  val height = {
    if (this.elements.nonEmpty) {
      this.elements.map( e => e.origin.y + e.height ).max - this.origin.y
    } else {
      this.previousVersion.map(_.height).getOrElse(0)
    }
  }

  override def toString = s"$name $elements"

  def add(element: Element) = {
    if (element != this) {
      this.copy(elements = this.elements :+ element, previousVersion = Some(this))
    } else {
      this
    }
  }

  def add(elements: Seq[Element]): ElementGroup = {
    val elementsToAdd = elements.filter( _ != this )
    this.copy(elements = this.elements ++ elementsToAdd, previousVersion = Some(this))
  }

  def remove(element: Element): ElementGroup = {
    this.copy(elements = this.elements.filter( _ != element), previousVersion = Some(this))
  }

  def remove(elements: Seq[Element]): ElementGroup = {
    this.copy(elements = this.elements.filter(!elements.contains(_)), previousVersion = Some(this))
  }

  def remove(name: String): ElementGroup = {
    this.copy(elements = this.elements.filter( _.name != name), previousVersion = Some(this))
  }

  def find(name: String): Option[Element] = {
    this.elements.find(_.name == name)
  }

  def find(names: Seq[String]): Seq[Element] = {
    names.flatMap(find)
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

  // Render the Group
  def paint(canvas: Canvas) = {
    if (!this.isDeleted) {
      this.elements.foreach( _.paint(canvas) )
    }
  }

  def collidesWith(point: Point2D): Boolean = this.elements.exists( _.collidesWith(point) )

}

object ElementGroup {
  // Counts the number of Groups created during this run of the program
  // The count is used to name new Groups uniquely.
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

}
