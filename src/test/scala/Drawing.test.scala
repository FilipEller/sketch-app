import logic._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DrawingTest extends AnyFlatSpec with Matchers {

  "New Drawing" should "have one layer by default" in {

    val drawing = new Drawing
    assert(drawing.layers.length === 1)
    assert(drawing.layers.head.name === "Layer 1")

  }

  "Drawing.addLayer" should "add a new empty Layer as the last value of Layers" in {

    val drawing = new Drawing
    assume(drawing.layers.length === 1)

    drawing.addLayer()
    assert(drawing.layers.length === 2)
    assert(drawing.layers.last.elements.isEmpty)

    drawing.addLayer()
    assert(drawing.layers.length === 3)
    assert(drawing.layers.last.elements.isEmpty)

  }

  "Drawing.addLayer" should "add a given Layer as the last value of Layers" in {

    val drawing = new Drawing
    assume(drawing.layers.length === 1)

    val layer1 = new Layer("My Layer 1")
    drawing.addLayer(layer1)
    assert(drawing.layers.length === 2)
    assert(drawing.layers.last === layer1)

  }

  "Drawing.selectLayer" should "change the Drawing's active Layer" in {

    val drawing = new Drawing
    assume(drawing.activeLayer === drawing.layers.head)

    val layer1 = new Layer("My Layer 1")
    val layer2 = new Layer("My Layer 2")
    drawing.addLayer(layer1)
    drawing.addLayer(layer2)

    drawing.selectLayer(layer1)
    assert(drawing.activeLayer === layer1)

    drawing.selectLayer(layer2)
    assert(drawing.activeLayer === layer2)

  }

  "Drawing.selectLayer" should "do nothing if Drawing does not contain given Layer" in {

    val drawing = new Drawing

    val layer1 = new Layer("My Layer 1")
    val layer2 = new Layer("My Layer 2")
    drawing.addLayer(layer1)
    drawing.addLayer(layer2)

    val layer3 = new Layer("My Layer 3")

    drawing.selectLayer(layer1)
    assert(drawing.activeLayer === layer1)

    drawing.selectLayer(layer3)
    assert(drawing.activeLayer === layer1)
    assert(!drawing.layers.contains(layer3))

  }

  "Drawing.findLayer" should "find a Layer by its name if Drawing contains that Layer" in {

    val drawing = new Drawing

    val layer1 = new Layer("My Layer 1")
    val layer2 = new Layer("My Layer 2")
    val layer3 = new Layer("My Layer 3")
    drawing.addLayer(layer1)
    drawing.addLayer(layer2)
    drawing.addLayer(layer3)

    assert(drawing.findLayer(layer1.name) === Some(layer1))
    assert(drawing.findLayer(layer2.name) === Some(layer2))
    assert(drawing.findLayer(layer3.name) === Some(layer3))

  }

  "Drawing.findLayer" should "return None if Drawing does not contain layer with given name" in {

    val drawing = new Drawing

    val layer1 = new Layer("My Layer 1")
    val layer2 = new Layer("My Layer 2")
    val layer3 = new Layer("My Layer 3")
    drawing.addLayer(layer1)
    drawing.addLayer(layer2)
    drawing.addLayer(layer3)

    assert(drawing.findLayer("Nonexistent") === None)

  }

  "Drawing.removeLayer" should "remove Layer by its name" in {

    val drawing = new Drawing
    assume(drawing.layers.length === 1)

    val layer1 = new Layer("My Layer 1")
    val layer2 = new Layer("My Layer 2")
    val layer3 = new Layer("My Layer 3")
    drawing.addLayer(layer1)
    drawing.addLayer(layer2)
    drawing.addLayer(layer3)
    assert(drawing.layers.length === 4)

    drawing.removeLayer(layer1.name)
    assert(drawing.layers.length === 3)
    assert(!drawing.layers.contains(layer1))

  }

  "Drawing.removeLayer" should "do nothing if there is only one Layer" in {

    val drawing = new Drawing
    assume(drawing.layers.length === 1)

    val target = drawing.layers.head

    drawing.removeLayer(target.name)
    assert(drawing.layers.length === 1)
    assert(drawing.layers.contains(target))

  }

  "Drawing.renameLayer" should "change a Layer's name" in {

    val drawing = new Drawing
    assume(drawing.layers.length === 1)

    val layer1 = new Layer("My Layer 1")
    val layer2 = new Layer("My Layer 2")
    val layer3 = new Layer("My Layer 3")
    drawing.addLayer(layer1)
    drawing.addLayer(layer2)
    drawing.addLayer(layer3)

    drawing.renameLayer(layer1, "My new Layer 1")
    assert(layer1.name === "My new Layer 1")
    assert(drawing.layers(1) === layer1)

  }

  "Drawing.renameLayer" should "do nothing if Drawing does not contain given Layer" in {

    val drawing = new Drawing
    assume(drawing.layers.length === 1)

    val layer1 = new Layer("My Layer 1")
    val layer2 = new Layer("My Layer 2")
    drawing.addLayer(layer1)
    drawing.addLayer(layer2)

    assert(drawing.layers.length === 3)

    val layer3 = new Layer("My Layer 3")
    drawing.renameLayer(layer1, "My new Layer 3")
    assert(layer3.name === "My Layer 3")
    assert(!drawing.layers.contains(layer3))
    assert(drawing.layers.length === 3)

  }

  "Drawing.renameLayer" should "add an index if Drawing already contains given name" in {

    val drawing = new Drawing
    assume(drawing.layers.length === 1)

    val layer1 = new Layer("My Layer")
    val layer2 = new Layer("Boring name")
    val layer3 = new Layer("Another boring name")
    drawing.addLayer(layer1)
    drawing.addLayer(layer2)
    drawing.addLayer(layer3)

    assert(drawing.layers.length === 4)

    drawing.renameLayer(layer2, "My Layer")
    assert(layer2.name === "My Layer 2")
    assert(drawing.layers.contains(layer2))
    assert(drawing.layers.length === 4)

    drawing.renameLayer(layer3, "My Layer")
    assert(layer3.name === "My Layer 3")
    assert(drawing.layers.contains(layer3))
    assert(drawing.layers.length === 4)

  }

}
