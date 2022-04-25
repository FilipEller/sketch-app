package logic
import javafx.scene.input.MouseEvent
import scalafx.geometry.Point2D

object MoveTool extends Tool {

  private var isActive = false

  // The Point of the Canvas where the Tool was last used
  private var lastPoint = new Point2D(0, 0)
  // The Elements that are currently being moved
  // but at their original places
  private var originalElements = Seq[Element]()

  private def move(drawing: Drawing, eventPoint: Point2D): Seq[Element] = {
    // Move the selected Elements of the Drawing
    // by the offset between eveenPoint and lastPoint
    val xDiff = eventPoint.x - lastPoint.x
    val yDiff = eventPoint.y - lastPoint.y
    drawing.moveSelected(xDiff, yDiff)
  }

  def use(drawing: Drawing, event: MouseEvent, eventPoint: Point2D): Unit = {
    event.getEventType match {
      case MouseEvent.MOUSE_PRESSED => {
        // The tool becomes active if the mouse is pressed on top of any of the Selected Elements
        this.isActive = drawing.config.selectedElements.exists( _.collidesWith(eventPoint) )
        if (this.isActive) {
          this.lastPoint = eventPoint
          this.originalElements = drawing.config.selectedElements
        }
      }
      case MouseEvent.MOUSE_DRAGGED => {
        // If the tool is active, move the selected Elements of the Drawing
        if (this.isActive) {
          this.move(drawing, eventPoint)
          this.lastPoint = eventPoint
        }
      }
      case MouseEvent.MOUSE_RELEASED => {
        // Finalize moving the selected Elements.
        if (this.isActive) {
          val movedElements = this.move(drawing, eventPoint)
          // Add a previousVersion thee Elements at their original places
          // as the previousVersions of the moved Elements
          val elementsWithHistory =
            movedElements.zip(originalElements)
              .map( x => x._1 match {
                case e: Shape => e.copy(previousVersion = Some(x._2))
                case e: Stroke => e.copy(previousVersion = Some(x._2))
                case e: TextBox => e.copy(previousVersion = Some(x._2))
                case e: ElementGroup => e.copy(previousVersion = Some(x._2))
                case e: Element => e
              })
          movedElements.zip(elementsWithHistory)
            .foreach( x => drawing.config.activeLayer.update(x._1, x._2) )
          ElementHistory.add(elementsWithHistory)
          drawing.select(elementsWithHistory)
        }
        this.isActive = false
      }
      case _ => {
        println("unrecognized mouseEvent type: " + event.getEventType)
      }
    }
  }
}
