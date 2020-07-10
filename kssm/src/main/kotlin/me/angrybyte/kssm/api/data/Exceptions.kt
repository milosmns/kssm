package me.angrybyte.kssm.api.data

import me.angrybyte.kssm.api.Action
import me.angrybyte.kssm.api.State
import me.angrybyte.kssm.api.StateMachine

/**
 * Signals the catcher that [StateMachine] is in incubation state.
 * Used to replace `null` in the observable flow of errors.
 */
object IncubationException : IllegalArgumentException("You should skip this value")

/**
 * Signals the catcher that [StateMachine] has passed the incubation state.
 * No more configuration changes should be made on the state machine instance.
 */
data class IncubationPassedException(val operation: String) :
  IllegalStateException("State machine is alive! $operation is not allowed anymore")

/**
 * Signals the catcher that [StateMachine] doesn't have the mapping registered for the given state and action.
 * The easiest fix is to check your mappings and add the missing ones.
 */
data class NoMappingException(val state: State<*>, val action: Action<*>) :
  IllegalStateException("No mapping found for state (${state.name}) using action [${action.name}]")