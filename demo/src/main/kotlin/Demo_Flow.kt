@file:Suppress("DuplicatedCode")

import WaterAction.Chill
import WaterAction.Drink
import WaterAction.Fill
import WaterAction.Heat
import WaterState.Empty
import WaterState.Ice
import WaterState.Liquid
import WaterState.Steam
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.angrybyte.kssm.api.Action
import me.angrybyte.kssm.dsl.moves
import me.angrybyte.kssm.dsl.stateMachine
import me.angrybyte.kssm.dsl.to
import java.util.concurrent.Executors

@ExperimentalCoroutinesApi
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

  }

  // NOTE: we will suspend to different threads to be able to continue;
  // alternatively, you can set dispatch threads to the state machine directly via the API

  val customDispatcher = Executors.newFixedThreadPool(2).asCoroutineDispatcher() // 2 - transitions + errors
  val transitionsJob = launch(customDispatcher) {
    // remember, Flow is conflated
    sm.transitions
      .drop(1)
      .collect { println("Detected change: $it\n") }
  }
  val errorsJob = launch(customDispatcher) {
    // remember, Flow is conflated
    sm.errors
      .drop(1)
      .collect { System.err.println("Invalid request: $it\n") }
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

  // NOTE: we wait for completion here and cancel the listeners

  transitionTasks.joinAll()
  transitionsJob.cancel()
  errorsJob.cancel()
  customDispatcher.close()
  sm.finish() // kill the state machine

}