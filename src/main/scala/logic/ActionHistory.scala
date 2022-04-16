package logic

import scala.collection.mutable.Stack

object ActionHistory {

  val actions: Stack[Seq[Element]] = Stack()

  def add(element: Element): Unit = {
    println("ADDING HISTORY:" + element + ", " + element.previousVersion)
    this.actions.push(Seq(element))
  }

  def add(elements: Seq[Element]): Unit = {
    println("ADDING HISTORY:" + elements + ", " + elements.map(_.previousVersion))
    this.actions.push(elements)
  }

  def undo(): Seq[Element] = {
    if (this.actions.nonEmpty) {
      val result = this.actions.pop()
      println("undoing: " + result + ", " + result.map(_.previousVersion))
      result
    } else {
      Seq()
    }
  }

}
