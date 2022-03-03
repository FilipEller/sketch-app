package logic

import scalafx.scene.paint.Color

abstract class Shape(width: Int, height: Int, borderWidth: Int, borderColor: Color, id: String, origin: Point, rotation: Int, hidden: Boolean, deleted: Boolean, group: Option[Long], color: Color, previousVersion: Option[Element]) extends Element(id, origin, rotation, hidden, deleted, group, color, previousVersion) {

}
