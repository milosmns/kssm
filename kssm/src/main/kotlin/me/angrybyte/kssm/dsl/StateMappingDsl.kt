package me.angrybyte.kssm.dsl

import me.angrybyte.kssm.api.Action
import me.angrybyte.kssm.api.State
import me.angrybyte.kssm.api.data.StateMapping

infix fun <A, S, D> StateVector<S, A>.to(destination: State<D>): StateMapping<S, A, D> = StateMapping(
  source = source,
  action = action,
  destination = destination
)

infix fun <A, S> Action<A>.moves(source: State<S>): StateVector<S, A> = StateVector(source, this)

data class StateVector<S, A>(val source: State<S>, val action: Action<A>)