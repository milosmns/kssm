package me.angrybyte.kssm.api

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import me.angrybyte.kssm.api.data.IncubationPassedException
import me.angrybyte.kssm.api.data.StateMapping
import me.angrybyte.kssm.api.data.Transition
import me.angrybyte.kssm.dsl.StateMachineDsl
import me.angrybyte.kssm.impl.action.CoreAction
import me.angrybyte.kssm.impl.state.CoreState

/**
 * The public-facing API for configuring, using and observing changes within a finite state machine.
 *
 * ## About state machines
 *
 * Fundamentally, a finite state machine (FSM) is a mathematical model of computation. It is an abstract machine that
 * can be in exactly one of a finite number of states at any given time. The FSM can change from one state to another
 * in response to some inputs (called `action`s here), and the change from one state to another is called a transition.
 * An FSM is defined by a **list of its states**, its **initial state**, and the **inputs/actions** that trigger each
 * of the transitions. In the context of this API, these definitions are called "state mappings".
 *
 * A state is a description of the status of a system that is waiting to execute a transition. A transition can be one,
 * or a set of actions, to be executed when a condition is fulfilled or when an event is received. For example, when
 * using an audio system to listen to the radio (the system is in the "playing radio" state), receiving a "next"
 * action results in moving to the next station. When the system is in the "playing CD" state, the "next" stimulus
 * results in moving to the next track. Identical stimuli trigger different actions depending on the current state.
 *
 * See [this image](https://i.imgur.com/rhDX2ux.png) for a quick reference.
 *
 * _Source: Wikipedia_
 *
 * ## Containment
 *
 * This particular API assumes that the state machine is self-contained. This means that all processing and state
 * manipulation happens inside of the implementing class: outside observers and interactors can only use a reduced set
 * of state machine touch-points that this API exposes.
 *
 * All possible states and actions should be configured before any transition is executed.
 *
 * ### Threading
 *
 * By default, a single-threaded, queue-like event loop will be created for processing events and reporting results and
 * errors. This event loop belongs to a [CoroutineDispatcher] that gets created before the state machine starts running.
 * In case you want to change threads, make sure that your dispatcher is single-threaded - otherwise you'll get the
 * reported events out of order. The API exposes functions for changing the dispatchers in 3 layers:
 *
 *   - Default dispatcher: used for internal processing of actions and transitioning to new states
 *   - Transitions dispatcher: used for reporting state changes
 *   - Error dispatcher: used for reporting any issues that occur while processing events
 *
 * The state machine manages its internal running jobs and transitions in a thread-safe way, while exposing control over
 * long-running tasks to the user when needed by returning a [Job] object for monitoring.
 *
 * ### Monitoring
 *
 * The state machine can be monitored in a couple of ways:
 *
 *   - [Flow]: **(preferred)** allows data manipulation and in-place concurrency using the familiar native interface
 *   - Handlers: [TransitionHandler] and [ErrorHandler] allow suspending reactions to state machine events
 *   - Observers: caller supplies a callback lambda, and gets a [Job] in response to cancel the processing manually
 *
 * Note that for handlers and suspending jobs, state machine runs processing actions on configured dispatchers.
 * The recommended approach with [Flow] allows you to control your own processing mechanism, e.g. in a nested coroutine.
 *
 * ## Instantiating
 *
 * Trying to instantiate these state machines manually using constructors is an error. To be sure that things are
 * correctly configured and prepared for executing event transitions, use the [StateMachineDsl].
 *
 * Here's an example how that would look like:
 *
 * ```kotlin
 * object AtoB : SingletonAction()
 * object BtoC : SingletonAction()
 * object CtoC : SingletonAction()
 * object A : SingletonState()
 * object B : SingletonState()
 * object C : SingletonState()
 *
 * val sm = stateMachine {
 *   mappings(
 *     AtoB moves A to B,
 *     BtoC moves B to C,
 *     CtoC moves C to C
 *   )
 *
 *   defaultDispatcher = findMyProcessingDispatcher() // optional
 *   transitionsDispatcher = findMyTransitionsDispatcher() // optional
 *
 *   transitionHandler { /* inline handler */ }
 *
 *   initialState = A
 * }
 * ```
 *
 * The returned object is an instance of this interface and you can call API functions on it. The returned instance is
 * thread-safe, so you can multi-thread on it without any issues.
 *
 * @see Action
 * @see State
 * @see StateMachineDsl
 * @see StateMapping
 * @see Transition
 */
interface StateMachine {

  // State operations

  /**
   * The [State] that this state machine is currently in. Initially, that's [CoreState.Incubating]
   */
  val currentState: State<*>

  /**
   * The last [Action] executed successfully by this state machine. Initially, that's [CoreAction.Birth]
   */
  val lastAction: Action<*>

  /**
   * Diagnostics: Check all possible states this state machine can transition to
   */
  fun getAllStates(): List<State<*>>

  /**
   * Diagnostics: Check all possible actions this state machine can process
   */
  fun getAllActions(): List<Action<*>>

  /**
   * Manually add new [StateMapping]s to this state machine. It is better to use the mappings DSL, if possible.
   *
   * @param mapping The state-action-state mapping. Best constructed using the mapping DSL
   *
   * @throws IncubationPassedException if state machine has already started processing events
   * @throws IllegalArgumentException if provided mapping has a conflict with another mapping
   */
  @Throws(IncubationPassedException::class, IllegalArgumentException::class)
  fun <S, A, D> addStateMapping(mapping: StateMapping<S, A, D>)

  /**
   * Sets the initial state for this state machine. If not set, the initial state will be [CoreState.Incubating].
   *
   * Make this call your last configuration call as it creates a transition internally.
   *
   * @throws IncubationPassedException if state machine has already started processing events
   */
  @Throws(IncubationPassedException::class)
  fun <T> setInitialState(state: State<T>)

  /**
   * Allows the caller to switch between **strict** and **loose** state matching modes. When transitioning using an
   * action, the state machine will try to find the resulting state by looking for the current state and the given
   * action.
   *
   * Default value is `true` (strict mode).
   *
   * ### Strict matching
   *
   * With **strict** matching, current state and the new action will be compared using [Any.equals], which means that
   * state/action type, name and payload all must match their stored mappings. An example:
   *
   * ```kotlin
   * object AB : SingletonAction()
   * object A : SingletonState()
   * object B : SingletonState()
   *
   * val sm = stateMachine {
   *   mappings(AB moves A to B)
   *   initialState = A
   * }
   *
   * // fails and reports an error (mismatch: payload and state type)
   * sm.transition(namedState("AB"))
   * ```
   *
   * ### Loose matching
   *
   * In contrast to strict state matching, **loose** matching will allow you to process actions during the transition
   * phase even though the payload or action/state type are different from the ones in the mapping list. The only thing
   * compared will be the `name` property of the action/state. An example:
   *
   * ```kotlin
   * object AB : SingletonAction()
   * object A : SingletonState()
   * object B : SingletonState()
   *
   * val sm = stateMachine {
   *   mappings(AB moves A to B)
   *   initialState = A
   * }
   *
   * sm.transition(namedState("AB"))
   * println(sm.currentState) // B, because only 'AB' name is being matched
   * ```
   *
   * ### Use-cases
   *
   * Loose matching is extremely useful if you want to send a different payload with the same action name, for example.
   * Another use-case would be using an internally-mutable payload in a state - that would not match the initial
   * payload provided in the state mapping block.
   *
   * @throws IncubationPassedException if state machine has already started processing events
   */
  @Throws(IncubationPassedException::class)
  fun setStrictStateMatching(strict: Boolean)

  // Transition observable

  /**
   * Allows observing transition changes using the native [Flow] interface
   */
  val transitions: Flow<Transition<*, *, *>>

  /**
   * Allows observing transition changes using a plain callback lambda.
   * Delivers events on the transitions dispatcher.
   *
   * @return a [Job] instance to manually cancel observing
   */
  fun observeTransitions(observer: (Transition<*, *, *>) -> Unit): Job

  /**
   * Adds a suspending transition handler that gets invoked every time a transition change needs to be reported.
   * Delivers events on the transitions dispatcher.
   */
  fun addTransitionHandler(handler: TransitionHandler)

  /**
   * Inverse to [addTransitionHandler].
   */
  fun removeTransitionHandler(handler: TransitionHandler)

  /**
   * Changes the dispatcher that reports transition changes.
   * Make sure that the given dispatcher is single-threaded, queue-like - otherwise events might come out-of-order.
   *
   * @throws IncubationPassedException if state machine has already started processing events
   */
  @Throws(IncubationPassedException::class)
  fun setTransitionHandlerDispatcher(dispatcher: CoroutineDispatcher)

  /**
   * Changes the dispatcher that internally processes state machine changes.
   * It's generally safe to use a multi-threaded dispatcher here.
   *
   * @throws IncubationPassedException if state machine has already started processing events
   */
  @Throws(IncubationPassedException::class)
  fun setDefaultDispatcher(dispatcher: CoroutineDispatcher)

  // Error observable

  /**
   * Allows observing error changes using the native [Flow] interface
   */
  val errors: Flow<Throwable>

  /**
   * Allows observing error changes using a plain callback lambda.
   * Delivers events on the errors dispatcher.
   *
   * @return a [Job] instance to manually cancel observing
   */
  fun observeErrors(observer: (Throwable) -> Unit): Job

  /**
   * Adds a suspending error handler that gets invoked every time an error needs to be reported.
   * Delivers events on the errors dispatcher.
   */
  fun addErrorHandler(handler: ErrorHandler)

  /**
   * Inverse to [addErrorHandler].
   */
  fun removeErrorHandler(handler: ErrorHandler)

  /**
   * Changes the dispatcher that reports errors.
   * Make sure that the given dispatcher is single-threaded, queue-like - otherwise events might come out-of-order.
   *
   * @throws IncubationPassedException if state machine has already started processing events
   */
  @Throws(IncubationPassedException::class)
  fun setErrorHandlerDispatcher(dispatcher: CoroutineDispatcher)

  // Operations

  /**
   * Attempts to transition the state machine into a new [State] using the given [Action]. In case of a successful
   * transition, your transition observers and [transitions] Flow will be notified appropriately. In case of an error,
   * your error observers and [errors] Flow will be notified appropriately.
   *
   * Note that this is an asynchronous operation in order to guarantee the order of calls and executions.
   *
   * @return A [Job] to manually cancel the transition
   */
  fun <T> transition(action: Action<T>): Job

  /**
   * Stops all internal processing and removes all transition and error handlers and observables. Also invalidates all
   * internal threading mechanisms, so nothing else can be executed on this state machine.
   *
   * After cleaning up, the state machine's state will be [CoreState.Dead] and the last action will be [CoreAction.Death].
   */
  fun finish()

}

/**
 * A suspending shorthand for reacting to [Transition]s coming from a [StateMachine].
 */
typealias TransitionHandler = suspend (transition: Transition<*, *, *>) -> Unit

/**
 * A suspending shorthand for reacting to [Throwable]s coming from a [StateMachine].
 */
typealias ErrorHandler = suspend (error: Throwable) -> Unit