package me.angrybyte.kssm.api

import me.angrybyte.kssm.impl.state.CoreState
import me.angrybyte.kssm.impl.state.NamedState
import me.angrybyte.kssm.impl.state.PayloadState
import me.angrybyte.kssm.impl.state.SingletonState

/**
 * A fundamental building block of a [StateMachine].
 * In [this example](https://i.imgur.com/rhDX2ux.png) that would be `State A`, `State B` and `State C`.
 *
 * Formally, we consider states to be static checkpoints for the state machine to go through.
 * A state machine can have only one 'current' state, and it can transition from it using an action (also called input).
 *
 * Using the current API, each state must have a unique name. Apart from having a name, each state can carry a
 * single object (immutable payload) with it. The payload is kept in-memory as long as the state is in-memory.
 *
 * The easiest way to create new state instances is using the DSL:
 *
 * ```kotlin
 * val named = namedState("StateA")
 * val heavy = payloadState("WaitingFor20Seconds", 20)
 * ```
 *
 * If you have a single set of static states, you could use [SingletonState]s, like so:
 *
 * ```kotlin
 * sealed class VendingMachineState : SingletonState() {
 *   object WaitingForSelection : VendingMachineState()
 *   object WaitingForMoney : VendingMachineState()
 *   object MovingDrink : VendingMachineState()
 *   object PayingBackChange : VendingMachineState()
 * }
 * ```
 *
 * @param T the state's payload type
 *
 * @see [NamedState]
 * @see [PayloadState]
 * @see [SingletonState]
 * @see [CoreState]
 * @see [Action]
 * @see [StateMachine]
 */
interface State<out T> {

  /**
   * A unique name associated with this state
   */
  val name: String

  /**
   * Optional payload that this state will carry in-memory
   */
  val payload: T?

}