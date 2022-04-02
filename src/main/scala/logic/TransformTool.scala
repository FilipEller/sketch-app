package logic
import javafx.scene.input.MouseEvent
import scalafx.geometry.Point2D

object TransformTool extends Tool {

  var isActive = false
  var lastPoint = new Point2D(0, 0)
  var originalElements = Seq[Element]()

  def move(drawing: Drawing, eventPoint: Point2D): Seq[Element] = {
    val xDiff = eventPoint.x - lastPoint.x
    val yDiff = eventPoint.y - lastPoint.y
    drawing.moveSelected(xDiff, yDiff)
  }

  def use(drawing: Drawing, event: MouseEvent, eventPoint: Point2D): Unit = {
    event.getEventType match {
      case MouseEvent.MOUSE_PRESSED => {
        this.isActive = drawing.config.selectedElements.exists( _.collidesWith(eventPoint) )
        if (this.isActive) {
          this.lastPoint = eventPoint
          this.originalElements = drawing.config.selectedElements
        }
        println("MOUSE_PRESSED")
      }
      case MouseEvent.MOUSE_DRAGGED => {
        println("MOUSE_DRAGGED")
        if (this.isActive) {
          this.move(drawing, eventPoint)
          this.lastPoint = eventPoint
        }
      }
      case MouseEvent.MOUSE_RELEASED => {
        println("MOUSE_RELEASED")
        if (this.isActive) {
          val movedElements = this.move(drawing, eventPoint)
          val elementsToUse = movedElements.zip(originalElements).map( x => x._1 match {
            case e: Shape => e.copy(previousVersion = Some(x._2))
            case e: Stroke => e.copy(previousVersion = Some(x._2))
            case e: TextBox => e.copy(previousVersion = Some(x._2))
            case e: ElementGroup => e.copy(previousVersion = Some(x._2))
            case e: Element => e
          })
          movedElements.zip(elementsToUse).foreach( x => drawing.config.activeLayer.updateElement(x._1, x._2) )
          elementsToUse.foreach(ActionHistory.add)
          drawing.config = drawing.config.copy(selectedElements = elementsToUse)
        }
        this.isActive = false
      }
      case _ => {
        println("unrecognized mouseEvent type: " + event.getEventType)
      }
    }
  }
}
