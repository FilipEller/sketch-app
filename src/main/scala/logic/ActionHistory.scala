package logic

import scala.collection.mutable.Stack

object ActionHistory {

  val actions: Stack[Element] = Stack()

  def add(element: Element): Unit = {
    this.actions.push(element)
    println("doing... history: " + this.actions)
  }

  def undo(): Option[Element] = {
    println("undoing... history: " + this.actions)
    if (this.actions.nonEmpty) {
      Some(this.actions.pop())
    } else {
      None
    }
  }

}
