package logic

import logic.Path.bresenhamLine
import scalafx.geometry.Point2D

import scala.math.abs

class Path(points: Seq[Point2D]) extends Seq[Point2D] {

  def :+ (point: Point2D): Path = {
    val end = this.points.last
    val newPoints = this.points.dropRight(1) ++ Path.bresenhamLine(end.x.round.toInt, end.y.round.toInt, point.x.round.toInt, point.y.round.toInt) :+ point
    new Path(newPoints)
  }

  def apply(index: Int) = this.points(index)
  def length = this.points.length
  def iterator = this.points.iterator

  def move(xDiff: Double, yDiff: Double): Path = {
    new Path(this.points.map( point => new Point2D(point.x + xDiff, point.y + yDiff) ))
  }

}

object Path {

  private def bresenhamLine(x0: Int, y0: Int, x1: Int, y1: Int): Seq[Point2D] = {

    var x = x0
    var y = y0

    val Dx = abs(x1 - x0)
    val sx = if(x0 < x1) 1 else -1
    val Dy = -abs(y1 - y0)
    val sy = if(y0 < y1) 1 else -1
    var error = Dx + Dy
    var finished = (x == x1 && y == y1)
    var path = Seq[Point2D]()

    while (!finished) {
      path = path :+ new Point2D(x, y)
      val e2 = 2 * error
      if (e2 >= Dy) {
        finished = (x == x1 || finished)
        error = error + Dy
        x = x + sx
      }
      if (e2 <= Dx) {
        finished = (y == y1 || finished)
        error = error + Dx
        y = y + sy
      }
      finished = ((x == x1 && y == y1) || finished)
    }
    path
  }

  def apply(point: Point2D): Path = {
    new Path(Seq(point))
  }

  def apply(point1: Point2D, point2: Point2D) = {
    val points = bresenhamLine(point1.x.round.toInt, point1.y.round.toInt, point2.x.round.toInt, point2.y.round.toInt) :+ point2
    new Path(points)
  }

}


