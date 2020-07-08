package me.angrybyte.kssm.api.data

import me.angrybyte.kssm.api.Action
import me.angrybyte.kssm.api.State

object IncubationException : IllegalArgumentException("You should skip this value")

data class IncubationPassedException(val operation: String) :
  IllegalStateException("State machine is alive! $operation is not allowed anymore")

data class NoMappingException(val state: State<*>, val action: Action<*>) :
  IllegalStateException("No mapping found for state (${state.name}) using action [${action.name}]")