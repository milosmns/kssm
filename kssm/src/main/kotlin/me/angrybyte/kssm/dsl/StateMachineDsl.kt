package me.angrybyte.kssm.dsl

import kotlinx.coroutines.CoroutineDispatcher
import me.angrybyte.kssm.api.ErrorHandler
import me.angrybyte.kssm.api.State
import me.angrybyte.kssm.api.StateMachine
import me.angrybyte.kssm.api.TransitionHandler
import me.angrybyte.kssm.api.data.StateMapping
import me.angrybyte.kssm.impl.machine.StateMachineImpl
import kotlin.DeprecationLevel.HIDDEN

/**
 * The DSL (domain-specific language) wrapper. Allows easy state machine configuration and instantiation.
 *
 * This wrapper respects the bounds set by the [StateMachine] API, meaning that you might get configuration exceptions
 * if you try to reconfigure things after state machine has started working.
 *
 * For example, [initialState] should be your last configuration call.
 * Another example - handlers can be added at any time, as they are not state machine's configuration elements.
 *
 * Here's how to use it:
 *
 * ```kotlin
 * object AtoB : SingletonAction()
 * object AtoC : SingletonAction()
 * object A : SingletonState()
 *
 * val sm = stateMachine {
 *   mappings(
 *     AtoB moves A to namedState("B"),
 *     AtoC moves A to payloadState("C", 12.41),
 *     namedAction("CtoA") moves payloadState("C", 12.41) to A
 *   )
 *   useStrictMatching = false
 *   transitionsDispatcher = Dispatchers.Main // optional
 *   transitionHandler { /* inline handler */ }
 *   initialState = A
 * }
 *
 * // and then on that instance:
 * sm.transition(AtoB)
 * ```
 *
 * @see [StateMachine]
 * @see [stateMachine]
 */
class StateMachineDsl(
  internal val instance: StateMachine
) {

  /**
   * Takes a list of [StateMapping]s and adds them into the state machine.
   *
   * @see [StateMachine.addStateMapping]
   */
  fun mappings(vararg mappings: StateMapping<*, *, *>) = apply {
    mappings.forEach { instance.addStateMapping(it) }
  }

  /**
   * Sets the initial state. Make this call your last configuration call as it creates a transition internally.
   *
   * @see [StateMachine.setInitialState]
   */
  var initialState: State<*>
    @Deprecated("Write-only.", level = HIDDEN)
    get() = throw UnsupportedOperationException()
    set(value) = instance.setInitialState(value)

  /**
   * Sets the strict mapping flag.
   *
   * @see [StateMachine.setStrictStateMatching]
   */
  var useStrictMatching: Boolean
    @Deprecated("Write-only.", level = HIDDEN)
    get() = throw UnsupportedOperationException()
    set(value) = instance.setStrictStateMatching(value)

  /**
   * Sets the internal processing dispatcher.
   *
   * @see [StateMachine.setDefaultDispatcher]
   */
  var defaultDispatcher: CoroutineDispatcher
    @Deprecated("Write-only.", level = HIDDEN)
    get() = throw UnsupportedOperationException()
    set(value) = instance.setDefaultDispatcher(value)

  /**
   * Sets the transitions dispatcher.
   *
   * @see [StateMachine.setTransitionHandlerDispatcher]
   */
  var transitionsDispatcher: CoroutineDispatcher
    @Deprecated("Write-only.", level = HIDDEN)
    get() = throw UnsupportedOperationException()
    set(value) = instance.setTransitionHandlerDispatcher(value)

  /**
   * Sets the errors dispatcher.
   *
   * @see [StateMachine.setErrorHandlerDispatcher]
   */
  var errorsDispatcher: CoroutineDispatcher
    @Deprecated("Write-only.", level = HIDDEN)
    get() = throw UnsupportedOperationException()
    set(value) = instance.setErrorHandlerDispatcher(value)

  /**
   * Adds a new inline transition handler.
   *
   * @see [StateMachine.addTransitionHandler]
   */
  fun transitionHandler(handler: TransitionHandler) = apply { instance.addTransitionHandler(handler) }

  /**
   * Adds a new inline error handler.
   *
   * @see [StateMachine.addErrorHandler]
   */
  fun errorHandler(handler: ErrorHandler) = apply { instance.addErrorHandler(handler) }

}

/**
 * Starts the DSL ([StateMachineDsl]) with a configuration block on a new state machine instance.
 */
fun stateMachine(initialize: StateMachineDsl.() -> Unit): StateMachine =
  StateMachineDsl(StateMachineImpl()).apply(initialize).instance

/**
 * Starts the DSL ([StateMachineDsl]) without a configuration block on a new state machine instance.
 */
fun stateMachine(): StateMachine = StateMachineImpl()