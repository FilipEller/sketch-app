import logic._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scalafx.geometry.Point2D
import scalafx.scene.paint.Color.rgb

class LayerTest extends AnyFlatSpec with Matchers {

  val rectangle1 = Shape(Rectangle, 10, 10, 10, rgb(0, 0, 0), rgb(0, 0, 0), true, true, new Point2D(0, 0))
  val rectangle2 = Shape(Rectangle, 10, 10, 10, rgb(100, 100, 100), rgb(100, 100, 100), true, true, new Point2D(100, 100))
  val ellipse = Shape(Ellipse, 10, 10, 10, rgb(0, 0, 0), rgb(0, 0, 0), true, true, new Point2D(0, 0))
  val square = Shape(Square, 10, 10, 10, rgb(0, 0, 0), rgb(0, 0, 0), true, true, new Point2D(0, 0))
  val circle = Shape(Circle, 10, 10, 10, rgb(0, 0, 0), rgb(0, 0, 0), true, true, new Point2D(0, 0))

  "Layer.add" should "store the every new Element as the layer's elements collection's last value" in {
    val layer = new Layer("test")
    assume(layer.elements.isEmpty)

    layer.add(rectangle1)
    assert(rectangle1 === layer.elements.last)

    layer.add(ellipse)
    assert(ellipse === layer.elements.last)

    layer.add(Seq(square, circle))
    assert(circle === layer.elements.last)

  }

  "Layer.remove" should "remove an Element from the Layer" in {
    val layer = new Layer("test")
    assume(layer.elements.isEmpty)

    layer.add(rectangle1)
    layer.add(rectangle2)
    layer.add(ellipse)
    layer.add(circle)
    assert(layer.elements.contains(ellipse))
    assert(layer.elements.length == 4)

    layer.remove(circle)
    assert(!layer.elements.contains(circle))
    assert(layer.elements.length == 3)

    layer.remove(Seq(rectangle1, rectangle2))
    assert(!layer.elements.contains(rectangle1))
    assert(!layer.elements.contains(rectangle2))
    assert(layer.elements.length == 1)
    assert(layer.elements.contains(ellipse))

  }

  "Layer.remove" should "do nothing if the Layer does not contain the target Element" in {
    val layer = new Layer("test")
    assume(layer.elements.isEmpty)

    layer.add(rectangle1)
    layer.add(rectangle2)
    layer.add(ellipse)
    assert(layer.elements.length == 3)

    layer.remove(circle)
    assert(layer.elements.contains(rectangle1))
    assert(layer.elements.contains(rectangle2))
    assert(layer.elements.contains(ellipse))
    assert(layer.elements.length == 3)

  }

  "Layer.addAtIndex" should "add the Element at the correct index" in {

    val layer = new Layer("test")
    assume(layer.elements.isEmpty)

    layer.add(circle)
    layer.add(ellipse)

    layer.addAtIndex(square, 1)
    assert(square === layer.elements(1))

    layer.addAtIndex(Seq(rectangle1, rectangle2), 2)
    assert(rectangle1 === layer.elements(2))
    assert(rectangle2 === layer.elements(3))

  }

  "Layer.addAtIndex" should "add the Element at the end of elements if a too high index is given" in {

    val layer = new Layer("test")
    assume(layer.elements.isEmpty)

    layer.add(circle)
    layer.add(ellipse)

    layer.addAtIndex(square, 5)
    assert(square === layer.elements.last)

    layer.addAtIndex(Seq(rectangle1, rectangle2), 8)
    assert(rectangle1 === layer.elements(3))
    assert(rectangle2 === layer.elements.last)

  }

  "Layer.addAtIndex" should "add the Element at the start of elements if a too low index is given" in {

    val layer = new Layer("test")
    assume(layer.elements.isEmpty)

    layer.add(circle)
    layer.add(ellipse)

    layer.addAtIndex(square, -1)
    assert(square === layer.elements.head)

    layer.addAtIndex(Seq(rectangle1, rectangle2), -8)
    assert(rectangle1 === layer.elements.head)
    assert(rectangle2 === layer.elements(1))

  }

  "Layer.find" should "return Some(Element) if it contains parameter Element" in {

    val layer = new Layer("test")
    assume(layer.elements.isEmpty)

    layer.add(circle)
    layer.add(ellipse)
    layer.add(square)

    assert(layer.find(circle.name) === Some(circle))
    assert(layer.find(ellipse.name) === Some(ellipse))
    assert(layer.find(square.name) === Some(square))

  }

  "Layer.find" should "return None if does not contain parameter Element" in {

    val layer = new Layer("test")
    assume(layer.elements.isEmpty)

    layer.add(circle)
    layer.add(square)
    assert(layer.find(ellipse.name) === None)

  }

  "Layer.find" should "return a Seq of all Elements it contains" in {

    val layer = new Layer("test")
    assume(layer.elements.isEmpty)

    layer.add(circle)
    layer.add(rectangle1)
    layer.add(ellipse)
    layer.add(rectangle2)
    assert(layer.find(Seq(circle.name, rectangle2.name)) === Seq(circle, rectangle2))
    assert(layer.find(Seq(rectangle1.name, rectangle2.name, square.name)) === Seq(rectangle1, rectangle2))

  }

  "Layer.rename" should "change the layer's name" in {

    val layer = new Layer("test")
    layer.rename("My Layer")
    assert(layer.name == "My Layer")

  }


  /*
    assertResult(ellipse) {
        layer.elements.last
    }
   */
}