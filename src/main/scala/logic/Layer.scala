package logic

import scalafx.geometry.Point2D
import scalafx.scene.canvas.Canvas

import scala.collection.mutable.Buffer

case class Layer(var name: String) {

  val elements = Buffer[Element]()
  var hidden = false
  var currentImage: Option[Canvas] = None

  def addElement(element: Element): Unit = {
    this.elements += element
  }

  def addElements(elements: Seq[Element]): Unit = {
    elements.foreach(this.addElement)
  }

  def addElementAtIndex(element: Element, index: Int): Unit = {
    val indexToUse = math.max(0, index)
    this.elements.insert(indexToUse, element)
  }

  def addElementsAtIndex(elements: Seq[Element], index: Int): Unit = {
    elements.foreach(this.elements.insert(index, _))
  }

  def removeElement(element: Element): Unit = {
    this.elements -= element
  }

  def removeElements(elements: Seq[Element]): Unit = {
    elements.foreach(this.removeElement)
  }

  def contains(element: Element) = this.elements.contains(element)

  def paint(width: Int, height: Int): Canvas = {
    def repaint(width: Int, height: Int) = {
      val canvas = new Canvas(width, height)
      this.elements.foreach(_.paint(canvas))
      currentImage = Some(canvas) // lets not try and do any optimizations for now. just this does not work anyway
      canvas
    }
    repaint(width, height) // currentImage.getOrElse(repaint(width, height))
  }

  def rename(newName: String) = {
    this.name = newName
  }

  def addElementGroup(group: ElementGroup): Unit = {
    group.elements.foreach( this.elements -= _ )
    this.elements += group
  }

  def addToGroup(element: Element, group: ElementGroup): Unit = {
    if (this.contains(element) && this.contains(group)) {
      this.elements -= element
      this.elements -= group
      this.elements += group.addElement(element)
    } else {
      throw new Exception("group or element does not belong to this layer")
    }
  }

  def removeElementGroup(group: ElementGroup): Unit = {
    if (this.contains(group)) {
      val index = this.elements.indexOf(group)
      this.addElementsAtIndex(group.elements.reverse, index)
      this.removeElement(group)
    } else {
      throw new Exception("group does not belong to this layer")
    }
  }

  def removeFromGroup(element: Element, group: ElementGroup): Unit = {
    if (this.contains(element) && this.contains(group)) {
      this.elements += element
      this.elements -= group
      this.elements += group.removeElement(element)
    } else {
      throw new Exception("group or element does not belong in this layer")
    }
  }

  def updateElement(element: Element): Element = {
    val index = element.previousVersion
                  .map( e => this.elements.indexOf(e) )
                  .map( i => math.max(0, i) )
                  .getOrElse(this.elements.length - 1)
    element.previousVersion.foreach(removeElement)
    this.addElementAtIndex(element, index)
    element
  }

  def updateElement(oldElement: Element, newElement: Element): Element = {
    this.removeElement(oldElement)
    this.addElement(newElement)
    newElement
  }

  def updateElements(elements: Seq[Element]): Seq[Element] = {
    elements.map( this.updateElement(_) )
  }

  def restoreElement(element: Element): Unit = {
    val index = this.elements.indexOf(element)
    this.removeElement(element)
    element.previousVersion.foreach( this.addElementAtIndex(_, index) )
  }

  def deleteElement(element: Element): Element = {
    val deleted = element match {
      case e: Shape => e.copy(deleted = true, previousVersion = Some(e))
      case e: Stroke => e.copy(deleted = true, previousVersion = Some(e))
      case e: TextBox => e.copy(deleted = true, previousVersion = Some(e))
      case e: ElementGroup => e.copy(deleted = true, previousVersion = Some(e))
      case e: Element => e
    }
    ActionHistory.add(deleted)
    this.updateElement(deleted)
  }

  def deleteElements(elements: Seq[Element]): Seq[Element] = {
    println("deleting elements")
    elements.map(deleteElement)
  }

  def removeElementFromGroup(group: ElementGroup, name: String): ElementGroup = {
    val element = group.findByName(name)
    element match {
      case Some(e: Element) => {
        val newGroup = group.removeElement(e)
        this.updateElement(newGroup)
        this.addElement(e)
        ActionHistory.add(newGroup)
        ActionHistory.add(e)
        newGroup
      }
      case None => group
    }
  }

  def removeElementsFromGroup(group: ElementGroup, names: Seq[String]): ElementGroup = {
    val elements = names.flatMap(group.findByName(_))
    val newGroup = group.removeElements(elements)
    this.updateElement(newGroup)
    this.addElements(elements)
    ActionHistory.add(newGroup)
    elements.foreach(ActionHistory.add)
    newGroup
  }

  def findElementByName(name: String): Option[Element] = {
    this.elements.find(_.name == name)
  }

  def findElementsByName(names: Seq[String]): Seq[Element] = {
    names.flatMap(findElementByName)
  }

  def select(point: Point2D): Option[Element] = this.elements.reverse.to(LazyList)
                                                  .filter(!_.deleted)
                                                  .find(_.collidesWith(point))
}
