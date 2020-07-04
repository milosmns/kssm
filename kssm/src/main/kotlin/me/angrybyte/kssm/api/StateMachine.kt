package me.angrybyte.kssm.api

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import me.angrybyte.kssm.api.data.IncubationPassedException
import me.angrybyte.kssm.api.data.StateMapping
import me.angrybyte.kssm.api.data.Transition

typealias TransitionHandler = suspend (transition: Transition<*, *, *>) -> Unit
typealias ErrorHandler = suspend (error: Throwable) -> Unit

interface StateMachine {

  // State operations

  val currentState: State<*>

  val lastAction: Action<*>

  fun getAllStates(): List<State<*>>

  fun getAllActions(): List<Action<*>>

  @Throws(IncubationPassedException::class, IllegalStateException::class)
  fun <S, A, D> addStateMapping(mapping: StateMapping<S, A, D>)

  @Throws(IncubationPassedException::class)
  fun <T> setInitialState(state: State<T>)

  @Throws(IncubationPassedException::class)
  fun setStrictStateMatching(strict: Boolean)

  // Transition observable

  val transitions: Flow<Transition<*, *, *>>

  fun observeTransitions(observer: (Transition<*, *, *>) -> Unit): Job

  fun addTransitionHandler(handler: TransitionHandler)

  fun removeTransitionHandler(handler: TransitionHandler)

  @Throws(IncubationPassedException::class)
  fun setTransitionHandlerDispatcher(dispatcher: CoroutineDispatcher)

  @Throws(IncubationPassedException::class)
  fun setDefaultDispatcher(dispatcher: CoroutineDispatcher)

  // Error observable

  val errors: Flow<Throwable>

  fun observeErrors(observer: (Throwable) -> Unit): Job

  fun addErrorHandler(handler: ErrorHandler)

  fun removeErrorHandler(handler: ErrorHandler)

  @Throws(IncubationPassedException::class)
  fun setErrorHandlerDispatcher(dispatcher: CoroutineDispatcher)

  // Operations

  fun <T> transition(action: Action<T>): Job

  fun finish()

}