package me.angrybyte.kssm.dsl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import me.angrybyte.kssm.api.ErrorHandler
import me.angrybyte.kssm.api.State
import me.angrybyte.kssm.api.StateMachine
import me.angrybyte.kssm.api.TransitionHandler
import me.angrybyte.kssm.api.data.StateMapping
import me.angrybyte.kssm.impl.machine.StateMachineImpl
import kotlin.DeprecationLevel.HIDDEN

class StateMachineDsl(
  internal val instance: StateMachine
) {

  fun mappings(vararg mappings: StateMapping<*, *, *>) = apply {
    mappings.forEach { instance.addStateMapping(it) }
  }

  var initialState: State<*>
    @Deprecated("Write-only.", level = HIDDEN)
    get() = throw UnsupportedOperationException()
    set(value) = instance.setInitialState(value)

  var useStrictMatching: Boolean
    @Deprecated("Write-only.", level = HIDDEN)
    get() = throw UnsupportedOperationException()
    set(value) = instance.setStrictStateMatching(value)

  var defaultDispatcher: CoroutineDispatcher
    @Deprecated("Write-only.", level = HIDDEN)
    get() = throw UnsupportedOperationException()
    set(value) = instance.setDefaultDispatcher(value)

  var transitionsDispatcher: CoroutineDispatcher
    @Deprecated("Write-only.", level = HIDDEN)
    get() = throw UnsupportedOperationException()
    set(value) = instance.setTransitionHandlerDispatcher(value)

  var errorsDispatcher: CoroutineDispatcher
    @Deprecated("Write-only.", level = HIDDEN)
    get() = throw UnsupportedOperationException()
    set(value) = instance.setErrorHandlerDispatcher(value)

  fun transitionHandler(handler: TransitionHandler) = apply { instance.addTransitionHandler(handler) }

  fun errorHandler(handler: ErrorHandler) = apply { instance.addErrorHandler(handler) }

}

@ExperimentalCoroutinesApi
fun stateMachine(initialize: StateMachineDsl.() -> Unit): StateMachine =
  StateMachineDsl(StateMachineImpl()).apply(initialize).instance

@ExperimentalCoroutinesApi
fun stateMachine(): StateMachine = StateMachineImpl()