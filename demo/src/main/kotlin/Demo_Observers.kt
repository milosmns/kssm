@file:Suppress("DuplicatedCode")

import WaterAction.Chill
import WaterAction.Drink
import WaterAction.Fill
import WaterAction.Heat
import WaterState.Empty
import WaterState.Ice
import WaterState.Liquid
import WaterState.Steam
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import me.angrybyte.kssm.api.Action
import me.angrybyte.kssm.dsl.moves
import me.angrybyte.kssm.dsl.stateMachine
import me.angrybyte.kssm.dsl.to
import java.lang.System.err

fun main() = runBlocking {

  // NOTE: infix function 'to' comes from the DSL package

  val sm = stateMachine {

    mappings(
      Heat moves Ice to Liquid,
      Heat moves Steam to Steam,
      Heat moves Liquid to Steam,

      Chill moves Ice to Ice,
      Chill moves Steam to Liquid,
      Chill moves Liquid to Ice,

      Drink moves Liquid to Empty,
      Fill moves Empty to Liquid
    )

    initialState = Empty

    transitionHandler { println("Detected change: $it\n") }
    errorHandler { err.println("Invalid request: $it\n") }

  }

  // NOTE: we move through states one after the other, but normally you would not do this;
  // instead, you would react to the actual outside events and send only one input/action at a time

  val transitionTasks = mutableListOf<Job>()
  fun moveTo(a: Action<*>) = transitionTasks.add(sm.transition(a))

  moveTo(Fill)  // becomes 'Liquid'
  moveTo(Chill) // becomes 'Ice'
  moveTo(Drink) // fails
  moveTo(Heat)  // becomes 'Liquid'
  moveTo(Heat)  // becomes 'Steam'
  moveTo(Drink) // fails
  moveTo(Chill) // becomes 'Liquid'
  moveTo(Drink) // becomes 'Empty'
  moveTo(Drink) // fails

  // NOTE: we wait for completion here

  transitionTasks.joinAll()
  sm.finish() // kill the state machine

}