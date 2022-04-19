package logic

import scala.collection.mutable.Stack

object ElementHistory {

  val actions: Stack[Seq[Element]] = Stack()

  def add(element: Element): Unit = {
    this.actions.push(Seq(element))
  }

  def add(elements: Seq[Element]): Unit = {
    this.actions.push(elements)
  }

  def undo(): Seq[Element] = {
    if (this.actions.nonEmpty) {
      this.actions.pop()
    } else {
      Seq()
    }
  }

}
