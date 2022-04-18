package logic

import scalafx.geometry.Point2D
import scalafx.scene.canvas.Canvas

import scala.collection.mutable.Buffer

case class Layer(var name: String) {

  private val mElements = Buffer[Element]()
  var hidden = false

  def elements = this.mElements.toSeq

  def add(element: Element): Unit = {
    this.mElements += element
  }

  def add(elements: Seq[Element]): Unit = {
    elements.foreach(this.add)
  }

  def addAtIndex(element: Element, index: Int): Unit = {
    val indexToUse = math.max(0, index)
    this.mElements.insert(indexToUse, element)
  }

  def addAtIndex(elements: Seq[Element], index: Int): Unit = {
    elements.foreach(this.mElements.insert(index, _))
  }

  def remove(element: Element): Unit = {
    this.mElements -= element
  }

  def remove(elements: Seq[Element]): Unit = {
    elements.foreach(this.remove)
  }

  def contains(element: Element) = this.elements.contains(element)

  def paint(width: Int, height: Int): Canvas = {
    def repaint(width: Int, height: Int) = {
      val canvas = new Canvas(width, height)
      this.elements.foreach(_.paint(canvas))
      canvas
    }
    repaint(width, height)
  }

  def rename(newName: String) = {
    this.name = newName
  }

  /*
  def addElementGroup(group: ElementGroup): Unit = {
    group.elements.foreach(this.remove)
    this.add(group)
  }*/

  def addToGroup(element: Element, group: ElementGroup): Unit = {
    if (this.contains(element) && this.contains(group)) {
      this.remove(element)
      this.remove(group)
      this.add(group.add(element))
    } else {
      throw new Exception("group or element does not belong to this layer")
    }
  }
/*
  def removeElementGroup(group: ElementGroup): Element = {
    val index = this.elements.indexOf(group)
    this.addAtIndex(group.elements.reverse, index)
    val newGroup = group.copy(deleted = true, previousVersion = Some(group))
    this.updateElement(newGroup)
  }*/

  /*
  def removeFromGroup(element: Element, group: ElementGroup): Unit = {
    if (this.contains(element) && this.contains(group)) {
      this.elements += element
      this.elements -= group
      this.elements += group.removeElement(element)
    } else {
      throw new Exception("group or element does not belong in this layer")
    }
  }
   */

  def update(element: Element): Element = {
    val index = element.previousVersion
                  .map( e => this.elements.indexOf(e) )
                  .map( i => math.max(0, i) )
                  .getOrElse(this.elements.length - 1)
    element.previousVersion.foreach(this.remove)
    this.addAtIndex(element, index)
    element
  }

  def update(oldElement: Element, newElement: Element): Element = {
    val index = this.elements.indexOf(oldElement)
    this.addAtIndex(newElement, index)
    this.remove(oldElement)
    newElement
  }

  def update(elements: Seq[Element]): Seq[Element] = {
    elements.map(this.update)
  }

  def restore(element: Element): Unit = {
    val index = this.elements.indexOf(element)
    this.remove(element)
    element.previousVersion.foreach( this.addAtIndex(_, index) )
    element match {
      case group: ElementGroup if group.previousVersion.isEmpty => {
        group.elements.reverse.foreach( this.addAtIndex(_, index) )
      }
      case _ =>
    }
  }

  def restore(elements: Seq[Element]): Unit = {
    elements.foreach(this.restore)
  }

  def delete(element: Element): Element = {
    val deleted = element match {
      case e: Shape => e.copy(deleted = true, previousVersion = Some(e))
      case e: Stroke => e.copy(deleted = true, previousVersion = Some(e))
      case e: TextBox => e.copy(deleted = true, previousVersion = Some(e))
      case e: ElementGroup => e.copy(deleted = true, previousVersion = Some(e))
      case e: Element => e
    }
    this.update(deleted)
  }

  def delete(elements: Seq[Element]): Seq[Element] = {
    elements.map(this.delete)
  }

  /*
  def removeElementFromGroup(group: ElementGroup, name: String): ElementGroup = {
    val element = group.findByName(name)
    element match {
      case Some(e: Element) => {
        val newGroup = group.removeElement(e)
        this.updateElement(newGroup)
        this.add(e)
        ActionHistory.add(newGroup)
        ActionHistory.add(e)
        newGroup
      }
      case None => group
    }
  }*/


  def removeFromGroup(group: ElementGroup, elements: Seq[Element]): (ElementGroup, Seq[Element]) = {
    val index = this.elements.indexOf(group)
    val groupWithoutTarget = group.remove(elements)
    val newGroup = if (groupWithoutTarget.elements.isEmpty) groupWithoutTarget.copy(deleted = true, previousVersion = Some(group)) else groupWithoutTarget
    this.update(newGroup)
    val newElements = elements.map{
      case e: Shape => e.copy(previousVersion = None)
      case e: Stroke => e.copy(previousVersion = None)
      case e: TextBox => e.copy(previousVersion = None)
      case e: ElementGroup => e.copy(previousVersion = None)
      case e: Element => e
    }
    this.addAtIndex(newElements.reverse, index + 1)
    (newGroup, newElements)
  }

  def removeFromGroupByName(group: ElementGroup, names: Seq[String]): (ElementGroup, Seq[Element]) = {
    val elements = group.find(names)
    removeFromGroup(group, elements)
  }

  def find(name: String): Option[Element] = {
    this.elements.find(_.name == name)
  }

  def find(names: Seq[String]): Seq[Element] = {
    names.flatMap(find)
  }

  def rename(element: Element, newName: String): Element = {
    val newElement = element match {
      case e: Shape => e.copy(name = newName, previousVersion = Some(e))
      case e: Stroke => e.copy(name = newName, previousVersion = Some(e))
      case e: TextBox => e.copy(name = newName, previousVersion = Some(e))
      case e: ElementGroup => e.copy(name = newName, previousVersion = Some(e))
      case e: Element => e
    }
    this.update(newElement)
  }

  def select(point: Point2D): Option[Element] =
    this.elements.reverse.to(LazyList)
      .filter(!_.deleted)
      .find(_.collidesWith(point))
}
