package logic

import scala.collection.mutable.Stack

object ElementHistory {

  // Stores the most recently changed Elements in a Stack
  private val actions: Stack[Seq[Element]] = Stack()

  // Adds a new Element on the top of the Stack
  def add(element: Element): Unit = {
    this.actions.push(Seq(element))
  }

  // Adds a sequence of Elements on the top of the Stack
  def add(elements: Seq[Element]): Unit = {
    this.actions.push(elements)
  }

  // Pops the top of the Stack, if it is not empty
  def undo(): Seq[Element] = {
    if (this.actions.nonEmpty) {
      this.actions.pop()
    } else {
      Seq()
    }
  }

  // Empties the Stack. This is done when a new Drawing is loaded.
  def clear(): Unit = {
    this.actions.clear()
  }

}
