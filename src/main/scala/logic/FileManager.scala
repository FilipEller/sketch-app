package logic

import scalafx.geometry.Point2D
import scalafx.scene.paint.Color
import ujson._

object FileManager {

  def encodeShapeType(stype: ShapeType): String = {
    stype match {
      case Rectangle => "Rectangle"
      case Square => "Square"
      case Ellipse => "Ellipse"
      case Circle => "Circle"
    }
  }

  def encodeColor(color: Color) = {
    ujson.Arr(color.red, color.blue, color.green, color.opacity)
  }

  def encodePoint(point: Point2D) = {
    ujson.Arr(point.x, point.y)
  }

  def encodeBrush(brush: Brush) = {
    ujson.Obj(
      "size" -> brush.size,
      "hardness" -> brush.hardness
    )
  }

  def encodeElement(element: Element): Obj = {
    element match {
      case e: Shape => ujson.Obj(
        "type" -> encodeShapeType(e.stype),
        "width" -> e.width,
        "height" -> e.height,
        "borderWidth" -> e.borderWidth,
        "color" -> encodeColor(e.color),
        "fillColor" -> encodeColor(e.fillColor),
        "useBorder" -> e.useBorder,
        "useFill" -> e.useFill,
        "origin" -> encodePoint(e.origin),
        "name" -> e.name
      )
      case e: Stroke => ujson.Obj(
        "color" -> encodeColor(e.color),
        "origin" -> encodePoint(e.origin),
        "path" -> e.path.map(encodePoint),
        "brush" -> encodeBrush(e.brush),
        "name" -> e.name
      )
      case e: TextBox => ujson.Obj(
        "text" -> e.text,
        "width" -> e.width,
        "height" -> e.height,
        "color" -> encodeColor(e.color),
        "origin" -> encodePoint(e.origin),
        "name" -> e.name
      )
      case e: ElementGroup => ujson.Obj(
        "elements" -> e.elements.map(encodeElement),
        "color" -> encodeColor(e.color),
        "name" -> e.name
      )
    }
  }

  def encodeLayer(layer: Layer) = {
    ujson.Obj(
      "name" -> layer.name,
      "hidden" -> layer.hidden,
      "elements" -> layer.elements.filter(!_.deleted).map(encodeElement)
    )
  }

  def encodeDrawing(drawing: Drawing) = {
    ujson.Obj(
      "width" -> drawing.width,
      "height" -> drawing.height,
      "layers" -> drawing.layers.map(encodeLayer)
    )
  }

  def save(drawing: Drawing, name: String): Unit = {
    val output = encodeDrawing(drawing)
    os.write(os.pwd/"tmp"/"test-drawing.json", output)
    println("saving drawing to file 🤔")
    println(output)
  }

  def load(file: String): Unit = {
    println("loading drawing from file 🧐")
  }

}
