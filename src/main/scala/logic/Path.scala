package logic

import scalafx.geometry.Point2D

import scala.math.abs

class Path(points: Seq[Point2D]) extends Seq[Point2D] {

  def apply(index: Int) = this.points(index)
  def length = this.points.length
  def iterator = this.points.iterator

}

object Path {

  def bresenhamLine(x0: Double, y0: Double, x1: Double, y1: Double): Vector[Point2D] = {

    var path = Vector[Point2D]()

    var x = x0
    var y = y0

    val Dx = abs(x1 - x0)
    val sx = if(x0 < x1) 1 else -1
    val Dy = -abs(y1 - y0)
    val sy = if(y0 < y1) 1 else -1
    var error = Dx + Dy

    var finished = (x == x1 && y == y1)

    while (!finished) {
      println(s"(x0, y0): ($x0, $y0), (x1, y1): ($x1, $y1), (x, y): ($x, $y),")
      path = path :+ new Point2D(x, y)
      val e2 = 2 * error
      if (e2 >= Dy) {
        finished = (math.ceil(x) == math.ceil(x1) || finished)
        error = error + Dy
        x = x + sx
      }
      if (e2 <= Dx) {
        finished = (math.ceil(y) == math.ceil(y1) || finished)
        error = error + Dx
        y = y + sy
      }
      finished = ((math.ceil(x) == math.ceil(x1) && math.ceil(y) == math.ceil(y1)) || finished)
    }

    path // note that endpoint (x1, y1) is not included
  }


  def apply[C[Point2D] <: collection.Seq[Point2D]](points: C[Point2D]): Path = {
    var fullPath = Vector[Point2D]()
    for (i <- 0 until points.length - 1) {
      fullPath = fullPath ++ bresenhamLine(points(i).x, points(i).y, points(i + 1).x, points(i + 1).y)
    }
    println(fullPath)
    fullPath = fullPath :+ points.last

    new Path(fullPath)
  }

  def apply(point: Point2D): Path = {
    new Path(Seq(point))
  }

}


