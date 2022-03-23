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
      throw new Exception("group or element does not belong in this layer")
    }
  }

  def removeElementGroup(group: ElementGroup): Unit = {
    if (this.contains(group)) {
      group.elements.foreach( this.elements += _ )
      this.elements -= group
    } else {
      throw new Exception("group does not belong in this layer")
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

  def select(point: Point2D): Option[Element] = this.elements.to(LazyList).filter(!_.hidden).find(_.collidesWith(point))
}
