package logic

import scalafx.geometry.Point2D
import scalafx.scene.paint.Color
import scalafx.stage.FileChooser
import ujson._

import java.io.File

object FileManager {

  private def encodeShapeType(stype: ShapeType): String = {
    stype match {
      case Rectangle => "Rectangle"
      case Square => "Square"
      case Ellipse => "Ellipse"
      case Circle => "Circle"
    }
  }

  private def encodeColor(color: Color) = {
    ujson.Arr(color.red, color.blue, color.green, color.opacity)
  }

  private def encodePoint(point: Point2D) = {
    ujson.Arr(point.x, point.y)
  }

  private def encodeBrush(brush: Brush) = {
    ujson.Obj(
      "size" -> brush.size,
      "hardness" -> brush.hardness
    )
  }

  private def encodeElement(element: Element): Obj = {
    element match {
      case e: Shape => ujson.Obj(
        "type" -> "shape",
        "shapeType" -> encodeShapeType(e.stype),
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
        "type" -> "stroke",
        "color" -> encodeColor(e.color),
        "origin" -> encodePoint(e.origin),
        "path" -> e.path.map(encodePoint),
        "brush" -> encodeBrush(e.brush),
        "name" -> e.name
      )
      case e: TextBox => ujson.Obj(
        "type" -> "textbox",
        "text" -> e.text,
        "width" -> e.width,
        "height" -> e.height,
        "color" -> encodeColor(e.color),
        "origin" -> encodePoint(e.origin),
        "name" -> e.name
      )
      case e: ElementGroup => ujson.Obj(
        "type" -> "elementgroup",
        "elements" -> e.elements.map(encodeElement),
        "color" -> encodeColor(e.color),
        "name" -> e.name
      )
    }
  }

  private def encodeLayer(layer: Layer) = {
    ujson.Obj(
      "name" -> layer.name,
      "hidden" -> layer.hidden,
      "elements" -> layer.elements.filter(!_.deleted).map(encodeElement)
    )
  }

  private def encodeDrawing(drawing: Drawing) = {
    ujson.Obj(
      "width" -> drawing.width,
      "height" -> drawing.height,
      "layers" -> drawing.layers.map(encodeLayer)
    )
  }

  private def decodeElement(data: Value): Element = {
    ???
  }

  private def decodeLayer(data: Value): Layer = {
    val layer = new Layer(data("name").str)
    val elements = data("elements").arr.map(decodeElement).toSeq
    layer.addElements(elements)
    layer.hidden = data("hidden").bool
    layer
  }

  private def decodeDrawing(data: Value): Drawing = {
    val drawing = new Drawing(data("width").num.toInt, data("width").num.toInt)
    val layers = data("layers").arr.map(decodeLayer).toSeq
    drawing.addLayers(layers)
    drawing
  }

  def save(drawing: Drawing, file: File): Unit = {
    val encoded = encodeDrawing(drawing)
    val jsonString = ujson.transform(encoded, StringRenderer(indent = 2)).toString
    val path = file.getPath
    val osPath = os.Path(if (path.endsWith(".json")) path else path + ".json")
    os.write.over(osPath, jsonString)
  }

  def load(file: File): Drawing = {
    println("loading drawing from file 🧐")
    println(file)
    // this could use for...yield
    val path = os.Path(file.getPath)
    val jsonString = os.read(path)
    val input = ujson.read(jsonString)
    decodeDrawing(input)
  }

}
