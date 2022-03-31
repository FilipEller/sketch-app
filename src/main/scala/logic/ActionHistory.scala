package logic

import scala.collection.mutable.Stack

object ActionHistory {

  val actions: Stack[Element] = Stack()

  def add(element: Element)  = this.actions.push(element)

  def undo() = this.actions.pop()

}
