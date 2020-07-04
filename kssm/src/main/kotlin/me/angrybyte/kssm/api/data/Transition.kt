package me.angrybyte.kssm.api.data

import me.angrybyte.kssm.api.Action
import me.angrybyte.kssm.api.State

data class Transition<S, A, D>(
  val oldState: State<S>,
  val action: Action<A>,
  val newState: State<D>
) {

  override fun toString() = "Transition (${oldState.name}) ---[${action.name}]--> (${newState.name})"

}