package logic

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

  def paint(width: Int, height: Int): Canvas = {
    def repaint(width: Int, height: Int) = {
      val canvas = new Canvas(width, height)
      this.elements.foreach(_.paint(canvas))
      // currentImage = Some(canvas) // lets not try and do any optimizations for now. this probably does not work
      canvas
    }
    currentImage.getOrElse(repaint(width, height))
  }

  def rename(newName: String) = {
    this.name = newName
  }

}
