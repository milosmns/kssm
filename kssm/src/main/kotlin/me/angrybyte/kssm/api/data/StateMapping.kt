package me.angrybyte.kssm.api.data

import me.angrybyte.kssm.api.Action
import me.angrybyte.kssm.api.State

/**
 * Keeps information about a particular state-to-state transition mapping.
 * You should provide an action at creation time, and both **start** and **end** states surrounding the action.
 *
 * The easiest way to create one is by using the DSL:
 *
 * ```kotlin
 * val mapping = AtoB moves A to B
 * ```
 * where `AtoB` is the action, and `A` and `B` are states.
 *
 * @param source the origin state (where the transition would start from)
 * @param action the trigger/action (what makes the transition start)
 * @param destination the goal state (where the transition would end at)
 * @param [S] source state's payload type
 * @param [A] action's payload type
 * @param [D] destination state's payload type
 *
 * @see Transition
 */
data class StateMapping<S, A, D>(
  val source: State<S>,
  val action: Action<A>,
  val destination: State<D>
) {

  override fun toString() = "Mapping (${source.name}) ---[${action.name}]--> (${destination.name})"

}