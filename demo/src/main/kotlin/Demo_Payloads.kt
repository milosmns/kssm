@file:Suppress("DuplicatedCode")

import WaterState.Empty
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import me.angrybyte.kssm.api.Action
import me.angrybyte.kssm.api.data.requirePayload
import me.angrybyte.kssm.dsl.moves
import me.angrybyte.kssm.dsl.payloadAction
import me.angrybyte.kssm.dsl.payloadState
import me.angrybyte.kssm.dsl.stateMachine
import me.angrybyte.kssm.dsl.to
import java.lang.System.err
import java.util.concurrent.atomic.AtomicInteger

@Suppress("UNCHECKED_CAST")
fun main() = runBlocking {

  // NOTE: we can manipulate the payload as long as it's internally mutable

  // define dynamic actions
  val heat = payloadAction("heat", 10)
  val chill = payloadAction("chill", 10)
  val drink = payloadAction("drink", 30)
  val fill = payloadAction("fill", 20)

  // define states
  val waterAmount = AtomicInteger(0)
  val ice = payloadState("ice", waterAmount)
  val steam = payloadState("steam", waterAmount)
  val liquid = payloadState("liquid", waterAmount)

  // NOTE: infix function 'to' comes from the DSL package

  val sm = stateMachine {

    mappings(
      heat moves ice to liquid,
      heat moves steam to steam,
      heat moves liquid to steam,

      chill moves ice to ice,
      chill moves steam to liquid,
      chill moves liquid to ice,

      drink moves liquid to liquid,
      fill moves liquid to liquid,
      fill moves Empty to liquid
    )

    useStrictMatching = false // matches state names even when payload differs
    initialState = Empty

  }

  // NOTE: configure special handling for dynamic water amount

  sm.addTransitionHandler {
    when {
      // steam evaporates
      it.action == heat && it.newState == steam -> waterAmount.getAndAdd(-heat.requirePayload())
      // ice expands
      it.action == chill && it.newState == ice -> waterAmount.getAndAdd(chill.requirePayload())
      // simple fill into empty container
      it.action == fill && it.oldState == Empty -> waterAmount.set(fill.requirePayload())
      // add more water to the container
      it.action == fill && it.oldState == liquid -> waterAmount.getAndAdd(fill.requirePayload())
      // remove water from the container (yes, it can go under 0)
      it.action == drink -> waterAmount.getAndAdd(-drink.requirePayload())
    }
    println("Handled transition: $it\n")
  }

  sm.addErrorHandler { err.println("Invalid transition: $it\n") }

  // NOTE: we move through states one after the other, but normally you would not do this;
  // instead, you would react to the actual outside events and send only one input/action at a time

  val transitionTasks = mutableListOf<Job>()
  fun moveTo(a: Action<*>) = transitionTasks.add(sm.transition(a))

  moveTo(fill)  // becomes 'liquid' with 20 water
  moveTo(chill) // becomes 'ice' with 30 ice
  moveTo(drink) // fails
  moveTo(heat)  // becomes 'liquid' with 30 water
  moveTo(heat)  // becomes 'steam' with 20 water
  moveTo(drink) // fails
  moveTo(chill) // becomes 'liquid' with 20 water
  moveTo(fill)  // stays 'liquid' with 40 water
  moveTo(drink) // stays 'liquid' with 10 water

  // NOTE: we wait for completion here

  transitionTasks.joinAll()
  sm.finish() // kill the state machine

  println("Final water amount = ${waterAmount.get()}")

}