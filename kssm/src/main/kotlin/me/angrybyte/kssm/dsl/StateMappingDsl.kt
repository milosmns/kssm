package me.angrybyte.kssm.dsl

import me.angrybyte.kssm.api.Action
import me.angrybyte.kssm.api.State
import me.angrybyte.kssm.api.data.StateMapping

/**
 * Binds the source [State] and an [Action] from a [StateVector] with a destination state into a [StateMapping].
 */
infix fun <A, S, D> StateVector<S, A>.to(destination: State<D>): StateMapping<S, A, D> = StateMapping(
  source = source,
  action = action,
  destination = destination
)

/**
 * Binds an [Action] and a source [State] into a [StateVector].
 */
infix fun <A, S> Action<A>.moves(source: State<S>): StateVector<S, A> = StateVector(source, this)

/**
 * Holds a [State] and an [Action], without knowing anything about the resulting state.
 */
data class StateVector<S, A>(val source: State<S>, val action: Action<A>)