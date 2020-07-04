package me.angrybyte.kssm.impl.machine

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.angrybyte.kssm.api.Action
import me.angrybyte.kssm.api.ErrorHandler
import me.angrybyte.kssm.api.State
import me.angrybyte.kssm.api.StateMachine
import me.angrybyte.kssm.api.TransitionHandler
import me.angrybyte.kssm.api.data.IncubationException
import me.angrybyte.kssm.api.data.IncubationPassedException
import me.angrybyte.kssm.api.data.NoMappingException
import me.angrybyte.kssm.api.data.StateMapping
import me.angrybyte.kssm.api.data.Transition
import me.angrybyte.kssm.impl.action.CoreAction.Birth
import me.angrybyte.kssm.impl.action.CoreAction.Death
import me.angrybyte.kssm.impl.state.CoreState.Dead
import me.angrybyte.kssm.impl.state.CoreState.Incubating
import me.angrybyte.kssm.impl.state.CoreState.None

@ExperimentalCoroutinesApi
internal class StateMachineImpl : StateMachine, CoroutineScope {

  // Coroutine scope setup

  private var defaultDispatcher: CoroutineDispatcher = SingleThreadDispatcherFactory.create("StateMachineQueue")
  private var transitionsDispatcher: CoroutineDispatcher = defaultDispatcher
  private var errorsDispatcher: CoroutineDispatcher = defaultDispatcher

  private val parentJob = SupervisorJob()
  override val coroutineContext = parentJob + CoroutineExceptionHandler { _, error ->
    errorFlow.value = error
    launch(errorsDispatcher) {
      errorHandlers.forEach { it(error) }
    }
  }

  // State and flow setup

  private val stateMappings = mutableSetOf<StateMapping<*, *, *>>()
  private var useStrictMapping = true

  private val errorHandlers = mutableSetOf<ErrorHandler>() // static observing
  private val errorFlow = MutableStateFlow<Throwable>( // reactive observing
    IncubationException
  )
  override val errors: Flow<Throwable> = errorFlow // public flow access

  private val transitionHandlers = mutableSetOf<TransitionHandler>() // static observing
  private val transitionFlow: MutableStateFlow<Transition<*, *, *>> = MutableStateFlow( // reactive observing
    Transition(
      oldState = None,
      action = Birth,
      newState = Incubating
    )
  )
  override val transitions: Flow<Transition<*, *, *>> = transitionFlow // public flow access

  override val currentState: State<*>
    get() = transitionFlow.value.newState

  override val lastAction: Action<*>
    get() = transitionFlow.value.action

  // Configuration

  @Synchronized
  @Throws(IncubationPassedException::class, IllegalStateException::class)
  override fun <S, A, D> addStateMapping(mapping: StateMapping<S, A, D>) {
    requireIncubationFor("Adding mappings")

    // find conflicting mapping (same source and action, different destination)
    stateMappings
      .firstOrNull { existing ->
        areEqual(existing.source, mapping.source) &&
          areEqual(existing.action, mapping.action) &&
          areEqual(existing.destination, mapping.destination)
      }
      ?.let { conflict ->
        error("Conflict detected!\nExisting: $conflict\nYours: $mapping")
      }

    stateMappings += mapping
  }

  @Synchronized
  @Throws(IncubationPassedException::class)
  override fun <T> setInitialState(state: State<T>) {
    requireIncubationFor("Adding handlers")

    transitionFlow.value = Transition(
      oldState = None,
      action = Birth,
      newState = state
    )
  }

  @Synchronized
  @Throws(IncubationPassedException::class)
  override fun setStrictStateMatching(strict: Boolean) {
    requireIncubationFor("Changing strict mapping")
    useStrictMapping = strict
  }

  @Synchronized
  override fun addTransitionHandler(handler: TransitionHandler) {
    transitionHandlers += handler
  }

  @Synchronized
  override fun removeTransitionHandler(handler: TransitionHandler) {
    transitionHandlers -= handler
  }

  @Synchronized
  override fun addErrorHandler(handler: ErrorHandler) {
    errorHandlers += handler
  }

  @Synchronized
  override fun removeErrorHandler(handler: ErrorHandler) {
    errorHandlers -= handler
  }

  @Synchronized
  @Throws(IncubationPassedException::class)
  override fun setTransitionHandlerDispatcher(dispatcher: CoroutineDispatcher) {
    requireIncubationFor("Setting transition dispatcher")
    transitionsDispatcher = dispatcher
  }

  @Synchronized
  @Throws(IncubationPassedException::class)
  override fun setErrorHandlerDispatcher(dispatcher: CoroutineDispatcher) {
    requireIncubationFor("Setting error dispatcher")
    errorsDispatcher = dispatcher
  }

  @Synchronized
  @Throws(IncubationPassedException::class)
  override fun setDefaultDispatcher(dispatcher: CoroutineDispatcher) {
    requireIncubationFor("Setting default dispatcher")
    defaultDispatcher = dispatcher
  }

  // State machine operations

  @Synchronized
  override fun <T> transition(action: Action<T>): Job = launch(defaultDispatcher) launch@{
    if (areEqual(currentState, Dead)) {
      val error = IllegalStateException("State machine is dead, create a new instance")
      errorFlow.value = error
      withContext(errorsDispatcher) {
        errorHandlers.forEach { it(error) }
      }
      return@launch
    }

    if (areEqual(action, Birth)) {
      val error = IllegalStateException("State machine can't be reborn, create a new instance")
      errorFlow.value = error
      withContext(errorsDispatcher) {
        errorHandlers.forEach { it(error) }
      }
      return@launch
    }

    // treat death exceptionally
    if (areEqual(action, Death)) {
      val transition = Transition(oldState = currentState, action = Death, newState = Dead)
      transitionFlow.value = transition
      withContext(transitionsDispatcher) {
        transitionHandlers.forEach { it(transition) }
      }
      return@launch
    }

    // try to find a mapping
    stateMappings
      .firstOrNull {
        areEqual(it.source, currentState) && areEqual(it.action, action)
      }
      ?.let { mapping ->
        // make the transition
        val transition = Transition(oldState = currentState, action = action, newState = mapping.destination)
        transitionFlow.value = transition
        withContext(transitionsDispatcher) {
          transitionHandlers.forEach { it(transition) }
        }
        return@launch
      }

    // no mapping found
    val error = NoMappingException(currentState, action)
    errorFlow.value = error
    withContext(errorsDispatcher) {
      errorHandlers.forEach { it(error) }
    }
  }

  @Synchronized
  override fun observeErrors(observer: (Throwable) -> Unit): Job = launch(errorsDispatcher) {
    val flow = if (errorFlow.value == IncubationException) errors.drop(1) else errors
    flow.collect { observer(it) }
  }

  @Synchronized
  override fun observeTransitions(observer: (Transition<*, *, *>) -> Unit): Job = launch(transitionsDispatcher) {
    val flow = if (areEqual(transitionFlow.value.action, Birth)) transitions.drop(1) else transitions
    flow.collect { observer(it) }
  }

  @Synchronized
  @ExperimentalStdlibApi
  override fun finish() {
    if (areEqual(currentState, Dead)) return
    val deathTransition = transition(Death)

    launch(defaultDispatcher) {
      deathTransition.join()
      errorHandlers.clear()
      transitionHandlers.clear()
      (coroutineContext[CoroutineDispatcher.Key] as? ExecutorCoroutineDispatcher)?.close()
      parentJob.cancel()
    }
  }

  // Diagnostics

  @Synchronized
  override fun getAllStates(): List<State<*>> = stateMappings
    .fold(mutableListOf<State<*>>()) { container, item ->
      container.apply {
        add(item.source)
        add(item.destination)
      }
    }
    .distinctBy { it.name }
    .sortedBy { it.name }

  @Synchronized
  override fun getAllActions(): List<Action<*>> = stateMappings
    .fold(mutableListOf<Action<*>>()) { container, item ->
      container.apply {
        add(item.action)
      }
    }
    .distinctBy { it.name }
    .sortedBy { it.name }

  // Helpers

  @Throws(IncubationPassedException::class)
  private fun requireIncubationFor(operation: String) {
    if (currentState != Incubating) throw IncubationPassedException(operation)
  }

  private fun areEqual(a: State<*>, b: State<*>) = if (useStrictMapping) a == b else a.name == b.name
  private fun areEqual(a: Action<*>, b: Action<*>) = if (useStrictMapping) a == b else a.name == b.name

}