package me.angrybyte.kssm.api.data

import me.angrybyte.kssm.api.Action
import me.angrybyte.kssm.api.State

data class StateMapping<S, A, D>(
  val source: State<S>,
  val action: Action<A>,
  val destination: State<D>
) {

  override fun toString() = "Mapping (${source.name}) ---[${action.name}]--> (${destination.name})"

}