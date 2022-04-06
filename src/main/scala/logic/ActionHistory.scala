package logic

import scala.collection.mutable.Stack

object ActionHistory {

  val actions: Stack[Element] = Stack()

  def add(element: Element): Unit = {
    this.actions.push(element)
  }

  def undo(): Option[Element] = {
    if (this.actions.nonEmpty) {
      Some(this.actions.pop())
    } else {
      None
    }
  }

}
