package me.angrybyte.kssm.api.data

import me.angrybyte.kssm.api.Action
import me.angrybyte.kssm.api.State

object IncubationException : IllegalArgumentException("You should skip this value")

class IncubationPassedException(operation: String) :
  IllegalStateException("State machine is alive! $operation is not allowed anymore")

class NoMappingException(state: State<*>, action: Action<*>) :
  IllegalStateException("No mapping found for state (${state.name}) using action [${action.name}]")