package logic

import scalafx.geometry.Point2D
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.rgb
import ujson.Value.InvalidData
import ujson._

import java.io.File

object FileManager {
  // Check src/test/resources for example files

  private def encodeShapeType(stype: ShapeType): String = {
    stype match {
      case Rectangle => "rectangle"
      case Square => "square"
      case Ellipse => "ellipse"
      case Circle => "circle"
    }
  }

  private def decodeShapeType(data: String): ShapeType = {
    data.toLowerCase match {
      case "rectangle" => Rectangle
      case "square" => Square
      case "ellipse" => Ellipse
      case "circle" => Circle
    }
  }

  private def encodeColor(color: Color) = {
    ujson.Arr(
      (color.red * 255).round.toInt,
      (color.green * 255).round.toInt,
      (color.blue * 255).round.toInt,
      color.opacity,
    )
  }

  private def decodeColor(data: Value): Color = {
    val color = data.arr.map(_.num)
    rgb(color(0).toInt, color(1).toInt, color(2).toInt, color(3))
  }

  private def encodePoint(point: Point2D) = {
    ujson.Arr(point.x, point.y)
  }

  private def decodePoint(data: Value): Point2D = {
    val coords = data.arr.map(_.num)
    new Point2D(coords(0), coords(1))
  }

  private def encodeBrush(brush: Brush) = {
    ujson.Obj(
      "size" -> brush.size,
      "hardness" -> brush.hardness
    )
  }

  private def decodeBrush(data: Value): Brush = {
    val size = data("size").num.toInt
    val hardness = data("hardness").num.toInt
    new Brush(size, hardness)
  }

  private def encodePath(path: Path) = {
    path.map(encodePoint)
  }

  private def decodePath(data: Value): Path = {
    val points = data.arr.map(decodePoint).toSeq
    new Path(points)
  }

  private def encodeElement(element: Element): Obj = {
    element match {
      case e: Shape => ujson.Obj(
        "type"        -> "shape",
        "shapeType"   -> encodeShapeType(e.shapeType),
        "width"       -> e.width,
        "height"      -> e.height,
        "borderWidth" -> e.borderWidth,
        "color"       -> encodeColor(e.color),
        "fillColor"   -> encodeColor(e.fillColor),
        "useBorder"   -> e.useBorder,
        "useFill"     -> e.useFill,
        "origin"      -> encodePoint(e.origin),
        "name"        -> e.name
      )
      case e: Stroke => ujson.Obj(
        "type"      -> "stroke",
        "color"     -> encodeColor(e.color),
        "origin"    -> encodePoint(e.origin),
        "path"      -> encodePath(e.path),
        "brush"     -> encodeBrush(e.brush),
        "name"      -> e.name
      )
      case e: TextBox => ujson.Obj(
        "type"      -> "textbox",
        "text"      -> e.text,
        "width"     -> e.width,
        "height"    -> e.height,
        "fontSize"  -> e.fontSize,
        "color"     -> encodeColor(e.color),
        "origin"    -> encodePoint(e.origin),
        "name"      -> e.name
      )
      case e: ElementGroup => ujson.Obj(
        "type" -> "group",
        "elements" -> e.elements.map(encodeElement),
        "color" -> encodeColor(e.color),
        "name" -> e.name
      )
    }
  }

  private def decodeElement(data: Value): Element = {
    data("type").str.toLowerCase match {
      case "shape" => {
        new Shape(
          decodeShapeType(data("shapeType").str),
          data("width").num.toInt,
          data("height").num.toInt,
          data("borderWidth").num.toInt,
          decodeColor(data("color")),
          decodeColor(data("fillColor")),
          data("useBorder").bool,
          data("useFill").bool,
          decodePoint(data("origin")),
          data("name").str
        )
      }
      case "stroke" => {
        new Stroke(
          decodeColor(data("color")),
          decodePoint(data("origin")),
          decodePath(data("path")),
          decodeBrush(data("brush")),
          data("name").str
        )
      }
      case "textbox" => {
        new TextBox(
          data("text").str,
          data("width").num.toInt,
          data("height").num.toInt,
          data("fontSize").num.toInt,
          decodeColor(data("color")),
          decodePoint(data("origin")),
          data("name").str
        )
      }
      case "group" => {
        new ElementGroup(
          data("elements").arr.map(decodeElement).toSeq,
          decodeColor(data("color")),
          data("name").str
        )
      }
    }
  }

  private def encodeLayer(layer: Layer) = {
    ujson.Obj(
      "name" -> layer.name,
      "hidden" -> layer.isHidden,
      "elements" -> layer.elements.filter(!_.isDeleted).map(encodeElement)
    )
  }

  private def decodeLayer(data: Value): Layer = {
    val layer = new Layer(data("name").str)
    val elements = data("elements").arr.map(decodeElement).toSeq
    layer.add(elements)
    layer.isHidden = data("hidden").bool
    layer
  }

  private def encodeDrawing(drawing: Drawing) = {
    ujson.Obj(
      "width" -> drawing.width,
      "height" -> drawing.height,
      "layers" -> drawing.layers.reverse.map(encodeLayer)
    )
  }

  private def decodeDrawing(data: Value): Drawing = {
    val layers = data("layers").arr.map(decodeLayer).reverse.toBuffer
    val width = data("width").num.toInt
    val height = data("height").num.toInt
    if (width < 1) {
      throw new InvalidData(width, "Width was nonpositive")
    }
    if (height < 1) {
      throw new InvalidData(height, "Height was nonpositive")
    }
    if (layers.length < 1) {
      throw new InvalidData(layers.mkString(", "), "No layers found")
    }
    new Drawing(width, height, layers)
  }

  // Saves the given Drawing to a JSON file with the path of the given File.
  // Overwrites that file if it already exists
  def save(drawing: Drawing, file: File): Unit = {
    val encoded = encodeDrawing(drawing)
    val jsonString = ujson.transform(encoded, StringRenderer(indent = 2)).toString
    val path = file.getPath
    val osPath = os.Path(if (path.endsWith(".json")) path else path + ".json")
    os.write.over(osPath, jsonString)
  }

  // Loads a Drawing from the given File.
  // Possible thrown exceptions include:
  // ujson.ParseException,
  // ujson.Value.InvalidData,
  // NoSuchElementException, and
  // IllegalArgumentException
  def load(file: File): Drawing = {
    val path = os.Path(file.getPath)
    val jsonString = os.read(path)
    val input = ujson.read(jsonString)
    decodeDrawing(input)
  }

}
