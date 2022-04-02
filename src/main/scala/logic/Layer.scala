package logic

import scalafx.geometry.Point2D
import scalafx.scene.canvas.Canvas

import scala.collection.mutable.Buffer

case class Layer(var name: String) {

  val elements = Buffer[Element]()
  var hidden = false
  var currentImage: Option[Canvas] = None

  def addElement(element: Element) = {
    println("adding " + element + " to layer " + this.name)
    this.elements += element
  }

  def addElementAtIndex(element: Element, index: Int) = {
    println("adding " + element + " to layer " + this.name)
    this.elements.insert(index, element)
  }

  def removeElement(element: Element) = {
    println("removing " + element)
    this.elements -= element
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
      group.elements.foreach( this.elements += _ )
      this.elements -= group
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
    element.previousVersion.foreach( this.removeElement(_) )
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
    val index = math.max(0, this.elements.indexOf(element))
    this.removeElement(element)
    element.previousVersion.foreach( this.addElementAtIndex(_, index) )
  }

  def select(point: Point2D): Option[Element] = this.elements.reverse.to(LazyList)
                                                  .filter(!_.hidden)
                                                  .find(_.collidesWith(point))
}
