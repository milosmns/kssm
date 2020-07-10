package me.angrybyte.kssm.api

import me.angrybyte.kssm.impl.action.CoreAction
import me.angrybyte.kssm.impl.action.NamedAction
import me.angrybyte.kssm.impl.action.PayloadAction
import me.angrybyte.kssm.impl.action.SingletonAction

/**
 * A fundamental building block of a [StateMachine].
 * In [this example](https://i.imgur.com/rhDX2ux.png) that would be `A-to-B`, `B-to-C`, `C to A` and `C : Self`.
 *
 * Formally, we consider actions to be `inputs` for the state machine.
 * Actions can either move the state machine into a different state, keep it in the same state, or simply fail if
 * state change is not possible from the current state using the given input/action.
 *
 * Using the current API, each action must have a unique name. Apart from having a name, each action can carry a
 * single object (immutable payload) with it. The payload is kept in-memory as long as the action is in-memory.
 *
 * The easiest way to create new action instances is using the DSL:
 *
 * ```kotlin
 * val named = namedAction("AtoB")
 * val heavy = payloadAction("AddWaterBucket", 20.0)
 * ```
 *
 * If you have a single set of static actions, you could use [SingletonAction]s, like so:
 *
 * ```kotlin
 * sealed class VendingMachineAction : SingletonAction() {
 *   object SelectProduct : VendingMachineAction()
 *   object AddMoney : VendingMachineAction()
 *   object PayBackChange : VendingMachineAction()
 *   object ShowWaitMode : VendingMachineAction()
 * }
 * ```
 *
 * @param T the action's payload type
 *
 * @see [NamedAction]
 * @see [PayloadAction]
 * @see [SingletonAction]
 * @see [CoreAction]
 * @see [State]
 * @see [StateMachine]
 */
interface Action<out T> {

  /**
   * A unique name associated with this action
   */
  val name: String

  /**
   * Optional payload that this action will carry in-memory
   */
  val payload: T?

}