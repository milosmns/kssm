package me.angrybyte.kssm.api.data

import me.angrybyte.kssm.api.Action
import me.angrybyte.kssm.api.State
import me.angrybyte.kssm.api.StateMachine

/**
 * In most ways a structurally similar data model to [StateMapping]. In contrast to [StateMapping], a `Transition` is
 * mostly used to report already processed state changes to the outside world.
 *
 * Normally you should not need to construct one yourself.
 *
 * @param oldState the state from which the [StateMachine] transitioned
 * @param action the action that triggered the state transition
 * @param newState the state to which the [StateMachine] transitioned
 * @param S the old state's payload type
 * @param A the action's payload type
 * @param D the new state's payload type
 *
 * @see StateMapping
 */
data class Transition<S, A, D>(
  val oldState: State<S>,
  val action: Action<A>,
  val newState: State<D>
) {

  override fun toString() = "Transition (${oldState.name}) ---[${action.name}]--> (${newState.name})"

}