package logic

import scalafx.geometry.Point2D
import scalafx.scene.canvas.Canvas

import scala.collection.mutable.Buffer

case class Layer(private var mName: String) {

  // Mutable collection of the Elements on this Layer.
  // The last elements is rendered on top of all the other Elements.
  private val mElements = Buffer[Element]()
  var isHidden = false

  // public immutable version of the mElements collection
  def elements = this.mElements.toSeq

  // public immutable version of the Layer's name
  def name = this.mName

  def add(element: Element): Unit = {
    this.mElements += element
  }

  def add(elements: Seq[Element]): Unit = {
    elements.foreach(this.add)
  }

  // Add an Element at a specific index of this.elements
  // to maintain correct ordering when rendered
  def addAtIndex(element: Element, index: Int): Unit = {
    val indexToUse = math.max(0, math.min(index, this.elements.length))
    this.mElements.insert(indexToUse, element)
  }

  def addAtIndex(elements: Seq[Element], index: Int): Unit = {
    if (index < 0) {
      elements.reverse.foreach(this.mElements.prepend)
    } else if (index > this.elements.length) {
      elements.foreach(this.mElements.append)
    } else {
      elements.reverse.foreach(this.addAtIndex(_, index))
    }
  }

  def remove(element: Element): Unit = {
    this.mElements -= element
  }

  def remove(elements: Seq[Element]): Unit = {
    elements.foreach(this.remove)
  }

  def contains(element: Element) = this.elements.contains(element)

  def find(name: String): Option[Element] = {
    this.elements.find(_.name == name)
  }

  def find(names: Seq[String]): Seq[Element] = {
    names.flatMap(this.find)
  }

  // Returns the topmost Element of this.elements
  // that encloses the given Point on the canvas
  def select(point: Point2D): Option[Element] =
    this.elements.reverse.to(LazyList)
      .filter(!_.isDeleted)
      .find(_.collidesWith(point))

  // Renders the Elements on this layer
  // The last Element is painted last and thus on top of the other Elements
  def paint(canvas: Canvas): Canvas = {
    this.elements.foreach(_.paint(canvas))
    canvas
  }

  def rename(newName: String) = {
    this.mName = newName
  }

  // Adds the given Element and removes the previous version of it if there is one
  def update(element: Element): Element = {
    val index = element.previousVersion
                  .map( e => this.elements.indexOf(e) )
                  .map( i => math.max(0, i) )
                  .getOrElse(this.elements.length - 1)
    element.previousVersion.foreach(this.remove)
    this.addAtIndex(element, index)
    element
  }

  // Removes the oldElement and adds the newElement
  // oldElement is assumed to be the previous version of newElement
  // although there is no reference from newElement.previousVersion to oldElement
  def update(oldElement: Element, newElement: Element): Element = {
    val index = this.elements.indexOf(oldElement)
    this.addAtIndex(newElement, index)
    this.remove(oldElement)
    newElement
  }

  def update(elements: Seq[Element]): Seq[Element] = {
    elements.map(this.update)
  }

  // Removes the given Element and adds its previousVersion if there is one.
  def restore(element: Element): Unit = {
    if (this.contains(element)) {
      val index = this.elements.indexOf(element)
      this.remove(element)
      element.previousVersion.foreach( this.addAtIndex(_, index) )
      element match {
        case group: ElementGroup if group.previousVersion.isEmpty => {
          this.addAtIndex(group.elements, index)
        }
        case _ =>
      }
    }
  }

  def restore(elements: Seq[Element]): Unit = {
    elements.foreach(this.restore)
  }

  // Deletes the given Element by making its isDeleted value true
  def delete(element: Element): Element = {
    if (this.elements.contains(element) && !element.isDeleted) {
      val deleted = element match {
        case e: Shape => e.copy(isDeleted = true, previousVersion = Some(e))
        case e: Stroke => e.copy(isDeleted = true, previousVersion = Some(e))
        case e: TextBox => e.copy(isDeleted = true, previousVersion = Some(e))
        case e: ElementGroup => e.copy(isDeleted = true, previousVersion = Some(e))
        case e: Element => e
      }
      this.update(deleted)
    } else {
      element
    }
  }

  def delete(elements: Seq[Element]): Seq[Element] = {
    elements.map(this.delete)
  }

  // Renames the Element with the given name.
  // Adds an index to the end of the name if the name is already in use.
  def rename(element: Element, newName: String): Element = {
    if (this.contains(element)) {
      val nameToUse = {
        if (this.elements.forall(_.name != newName)) {
          newName
        } else {
          var index = 2
          val names = this.elements.map(_.name)
          while (names.contains(s"$newName ${index}")) {
            index += 1
          }
          s"$newName ${index}"
        }
      }

      val newElement = element match {
        case e: Shape => e.copy(name = nameToUse, previousVersion = Some(e))
        case e: Stroke => e.copy(name = nameToUse, previousVersion = Some(e))
        case e: TextBox => e.copy(name = nameToUse, previousVersion = Some(e))
        case e: ElementGroup => e.copy(name = nameToUse, previousVersion = Some(e))
        case e: Element => e
      }
      this.update(newElement)
    } else {
      element
    }
  }

  // Remove the given Elements from the given Group
  // The removed Elements are added to this Layer if succesfully removed from the Group
  def removeFromGroup(group: ElementGroup, elements: Seq[Element]): (ElementGroup, Seq[Element]) = {
    val index = this.elements.indexOf(group)
    val groupWithoutTarget = group.remove(elements)
    val newGroup = {
      if (groupWithoutTarget.elements.isEmpty)
        groupWithoutTarget.copy(isDeleted = true, previousVersion = Some(group))
      else
        groupWithoutTarget
    }
    this.update(newGroup)
    val newElements = elements.map{
      case e: Shape => e.copy(previousVersion = None)
      case e: Stroke => e.copy(previousVersion = None)
      case e: TextBox => e.copy(previousVersion = None)
      case e: ElementGroup => e.copy(previousVersion = None)
      case e: Element => e
    }
    this.addAtIndex(newElements, index + 1)
    (newGroup, newElements)
  }

  def removeFromGroupByName(group: ElementGroup, names: Seq[String]): (ElementGroup, Seq[Element]) = {
    val elements = group.find(names)
    removeFromGroup(group, elements)
  }

}
