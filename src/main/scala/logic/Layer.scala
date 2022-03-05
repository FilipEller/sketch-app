package logic

import scalafx.scene.canvas.Canvas

import scala.collection.mutable.Buffer

case class Layer(name: String, elements: Buffer[Element], hidden: Boolean, var currentImage: Option[Canvas]) {

  def addElement(element: Element) = {
    this.elements += element
  }

  def removeElement(element: Element) = {
    this.elements -= element
  }

  def paint(width: Int, height: Int): Canvas = {
    if (currentImage.nonEmpty) {
      return currentImage.get
    }
    val canvas = new Canvas(width, height)
    this.elements.foreach(_.paint(canvas))
    currentImage = Some(canvas)
    canvas
  }

}
