package me.angrybyte.kssm.impl.machine

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import me.angrybyte.kssm.api.ErrorHandler
import me.angrybyte.kssm.api.StateMachine
import me.angrybyte.kssm.api.TransitionHandler
import me.angrybyte.kssm.api.data.IncubationPassedException
import me.angrybyte.kssm.api.data.NoMappingException
import me.angrybyte.kssm.api.data.StateMapping
import me.angrybyte.kssm.api.data.Transition
import me.angrybyte.kssm.dsl.StateMachineDsl
import me.angrybyte.kssm.dsl.moves
import me.angrybyte.kssm.dsl.namedAction
import me.angrybyte.kssm.dsl.namedState
import me.angrybyte.kssm.dsl.payloadAction
import me.angrybyte.kssm.dsl.payloadState
import me.angrybyte.kssm.dsl.stateMachine
import me.angrybyte.kssm.dsl.to
import me.angrybyte.kssm.impl.action.CoreAction
import me.angrybyte.kssm.impl.action.SingletonAction
import me.angrybyte.kssm.impl.state.CoreState
import me.angrybyte.kssm.impl.state.SingletonState
import me.angrybyte.kssm.util.shouldBe
import me.angrybyte.kssm.util.shouldBeA
import me.angrybyte.kssm.util.shouldThrow
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class StateMachineImplTest {

  companion object {
    private val A = namedState("A")
    private val B = payloadState("B", 10)
    private val AB = namedAction("AB")
    private val BC = payloadAction("BC", 20)
  }

  private object C : SingletonState()
  private object CC : SingletonAction()

  private lateinit var sm: StateMachineImpl
  private val testDispatcher = TestCoroutineDispatcher()

  @AfterEach fun tearDown() = sm.finish()

  @Test fun `empty init - last action is birth`() = runBlockingTest {
    sm = stateMachine().dispatchToTest().test()

    sm.lastAction shouldBe CoreAction.Birth
  }

  @Test fun `empty init - current state is incubating`() = runBlockingTest {
    sm = stateMachine().dispatchToTest().test()

    sm.currentState shouldBe CoreState.Incubating
  }

  @Test fun `listing all states`() = runBlockingTest {
    sm = stateMachine {
      mappings(
        AB moves A to B,
        BC moves B to C,
        CC moves C to C
      )
      dispatchToTest()
    }.test()

    sm.getAllStates() shouldBe listOf(A, B, C)
  }

  @Test fun `listing all actions`() = runBlockingTest {
    sm = stateMachine {
      mappings(
        AB moves A to B,
        BC moves B to C,
        CC moves C to C
      )
      dispatchToTest()
    }.test()

    sm.getAllActions() shouldBe listOf(AB, BC, CC)
  }

  @Test fun `initial state set - throws if incubation passed`() = runBlockingTest {
    sm = stateMachine {
      mappings(
        namedAction("MoveToX") moves CoreState.Incubating to namedState("X")
      )
      dispatchToTest()
    }.test()
    sm.transition(namedAction("MoveToX"))
    testDispatcher.advanceUntilIdle()

    shouldThrow(IncubationPassedException::class) {
      sm.setInitialState(B)
    }
  }

  @Test fun `initial state set - current state is set`() = runBlockingTest {
    sm = stateMachine {
      mappings(
        AB moves A to B
      )
      dispatchToTest()
      initialState = A
    }.test()

    sm.currentState shouldBe A
  }

  @Test fun `initial state set - last action is set`() = runBlockingTest {
    sm = stateMachine {
      mappings(
        AB moves A to B
      )
      dispatchToTest()
      initialState = A
    }.test()

    sm.lastAction shouldBe CoreAction.Birth
  }

  @Test fun `adding and removing transition handler`() = runBlockingTest {
    val handler: TransitionHandler = { /* empty */ }
    sm = stateMachine {
      transitionHandler(handler)
      dispatchToTest()
    }.test()

    (sm as? StateMachineImpl)?.transitionHandlers shouldBe setOf(handler)

    sm.removeTransitionHandler(handler)

    (sm as? StateMachineImpl)?.transitionHandlers shouldBe emptySet<TransitionHandler>()
  }

  @Test fun `adding and removing error handler`() = runBlockingTest {
    val handler: ErrorHandler = { /* empty */ }
    sm = stateMachine {
      errorHandler(handler)
      dispatchToTest()
    }.test()

    (sm as? StateMachineImpl)?.errorHandlers shouldBe setOf(handler)

    sm.removeErrorHandler(handler)

    (sm as? StateMachineImpl)?.errorHandlers shouldBe emptySet<ErrorHandler>()
  }

  @Test fun `mapping - conflict throws`() = runBlockingTest {
    sm = stateMachine {
      mappings(
        AB moves A to B
      )
      dispatchToTest()
    }.test()

    shouldThrow(IllegalArgumentException::class) {
      sm.addStateMapping(AB moves A to C)
    }
  }

  @Test fun `mapping - duplicate mappings are ignored`() = runBlockingTest {
    sm = stateMachine {
      mappings(
        AB moves A to B,
        AB moves A to B
      )
      dispatchToTest()
      initialState = A
    }.test()

    (sm as? StateMachineImpl)?.stateMappings shouldBe setOf(StateMapping(A, AB, B))
  }

  @Test fun `mapping - can use states and actions`() = runBlockingTest {
    sm = stateMachine {
      mappings(
        AB moves A to B,
        BC moves B to C,
        CC moves C to C
      )
      dispatchToTest()
    }.test()

    (sm as? StateMachineImpl)?.stateMappings shouldBe setOf(
      StateMapping(A, AB, B),
      StateMapping(B, BC, C),
      StateMapping(C, CC, C)
    )
  }

  @Test fun `observing transitions - using flow`() = runBlockingTest {
    sm = stateMachine {
      mappings(
        AB moves A to B
      )
      dispatchToTest()
      initialState = A
    }.test()

    var transition: Transition<*, *, *>? = null
    val job = launch(testDispatcher) {
      sm.transitions.collect { transition = it }
    }
    sm.transition(AB)
    testDispatcher.advanceUntilIdle()
    job.cancel() // wait only for one

    transition shouldBe Transition(A, AB, B)
  }

  @Test fun `observing errors - using flow`() = runBlockingTest {
    sm = stateMachine().dispatchToTest().test()

    var error: Throwable? = null
    val job = launch(testDispatcher) {
      sm.errors.collect { error = it }
    }
    sm.transition(CC)
    testDispatcher.advanceUntilIdle()
    job.cancel() // wait only for one

    error shouldBe NoMappingException(CoreState.Incubating, CC)
  }

  @Test fun `observing transitions - using observer`() = runBlockingTest {
    sm = stateMachine {
      mappings(
        AB moves A to B
      )
      dispatchToTest()
      initialState = A
    }.test()

    var transition: Transition<*, *, *>? = null
    val job = sm.observeTransitions { transition = it }
    sm.transition(AB)
    testDispatcher.advanceUntilIdle()
    job.cancel() // wait only for one

    transition shouldBe Transition(A, AB, B)
  }

  @Test fun `observing errors - using observer`() = runBlockingTest {
    sm = stateMachine().dispatchToTest().test()

    var error: Throwable? = null
    val job = sm.observeErrors { error = it }
    sm.transition(CC)
    testDispatcher.advanceUntilIdle()
    job.cancel() // wait only for one

    error shouldBe NoMappingException(CoreState.Incubating, CC)
  }

  @Test fun `observing transitions - using handler`() = runBlockingTest {
    var transition: Transition<*, *, *>? = null
    sm = stateMachine {
      mappings(
        AB moves A to B
      )
      dispatchToTest()
      initialState = A

      transitionHandler { transition = it }
    }.test()

    sm.transition(AB)
    testDispatcher.advanceUntilIdle()

    transition shouldBe Transition(A, AB, B)
  }

  @Test fun `observing errors - using handler`() = runBlockingTest {
    var error: Throwable? = null
    sm = stateMachine {
      dispatchToTest()

      errorHandler { error = it }
    }.test()

    sm.transition(CC)
    testDispatcher.advanceUntilIdle()

    error shouldBe NoMappingException(CoreState.Incubating, CC)
  }

  @Test fun `transition - strict matching prevents transition`() = runBlockingTest {
    sm = stateMachine {
      mappings(
        AB moves A to B
      )
      dispatchToTest()
      useStrictMatching = true
      initialState = A
    }.test()

    val abSimilar = payloadAction("AB", 0) // same name, not equal
    sm.transition(abSimilar)
    testDispatcher.advanceUntilIdle()

    sm.currentState shouldBe A
    sm.lastAction shouldBe CoreAction.Birth
  }

  @Test fun `transition - loose matching allows transition`() = runBlockingTest {
    sm = stateMachine {
      mappings(
        AB moves A to B
      )
      dispatchToTest()
      useStrictMatching = false
      initialState = A
    }.test()

    val abSimilar = payloadAction("AB", 0) // same name, not equal
    sm.transition(abSimilar)
    testDispatcher.advanceUntilIdle()

    sm.currentState shouldBe B
    sm.lastAction shouldBe abSimilar
  }

  @Test fun `transition - can't transition after death`() = runBlockingTest {
    var transition: Transition<*, *, *>? = null
    var error: Throwable? = null
    sm = stateMachine {
      mappings(
        AB moves A to B
      )
      dispatchToTest()
      initialState = CoreState.Dead
      transitionHandler { transition = it }
      errorHandler { error = it }
    }.test()

    sm.transition(AB)
    testDispatcher.advanceUntilIdle()

    sm.currentState shouldBe CoreState.Dead
    sm.lastAction shouldBe CoreAction.Birth
    transition shouldBe null
    error shouldBeA IllegalStateException::class
  }

  @Test fun `transition - can't transition into birth again`() = runBlockingTest {
    var transition: Transition<*, *, *>? = null
    var error: Throwable? = null
    sm = stateMachine {
      mappings(
        AB moves A to B
      )
      dispatchToTest()
      initialState = A
      transitionHandler { transition = it }
      errorHandler { error = it }
    }.test()

    sm.transition(CoreAction.Birth)
    testDispatcher.advanceUntilIdle()

    sm.currentState shouldBe A
    sm.lastAction shouldBe CoreAction.Birth
    transition shouldBe null
    error shouldBeA IllegalStateException::class
  }

  @Test fun `transition - transition into death`() = runBlockingTest {
    var transition: Transition<*, *, *>? = null
    var error: Throwable? = null
    sm = stateMachine {
      mappings(
        AB moves A to B
      )
      dispatchToTest()
      initialState = A
      transitionHandler { transition = it }
      errorHandler { error = it }
    }.test()

    sm.transition(CoreAction.Death)
    testDispatcher.advanceUntilIdle()

    sm.currentState shouldBe CoreState.Dead
    sm.lastAction shouldBe CoreAction.Death
    transition shouldBe Transition(A, CoreAction.Death, CoreState.Dead)
    error shouldBe null
  }

  @Test fun `transition - mapping not found`() = runBlockingTest {
    var transition: Transition<*, *, *>? = null
    var error: Throwable? = null
    sm = stateMachine {
      mappings(
        AB moves A to B
      )
      dispatchToTest()
      initialState = A
      transitionHandler { transition = it }
      errorHandler { error = it }
    }.test()

    sm.transition(CC)
    testDispatcher.advanceUntilIdle()

    sm.currentState shouldBe A
    sm.lastAction shouldBe CoreAction.Birth
    transition shouldBe null
    error shouldBe NoMappingException(A, CC)
  }

  @Test fun `transition - mixed success and failure`() = runBlockingTest {
    val transitions = mutableListOf<Transition<*, *, *>>()
    val errors = mutableListOf<Throwable>()
    sm = stateMachine {
      mappings(
        AB moves A to B,
        BC moves B to C,
        CC moves C to C
      )
      dispatchToTest()
      initialState = A
      transitionHandler { transitions.add(it) }
      errorHandler { errors += it }
    }.test()

    val invalidAction = namedAction("invalid")
    sm.transition(AB) // ok
    sm.transition(BC) // ok
    sm.transition(invalidAction) // fails
    sm.transition(CC) // ok
    sm.transition(AB) // fails (not in A state)
    testDispatcher.advanceUntilIdle()

    sm.currentState shouldBe C
    sm.lastAction shouldBe CC
    transitions shouldBe listOf(
      Transition(A, AB, B),
      Transition(B, BC, C),
      Transition(C, CC, C)
    )
    errors shouldBe listOf(
      NoMappingException(C, invalidAction),
      NoMappingException(C, AB)
    )
  }

  @Test fun `finishing cleans everything`() {
    sm = stateMachine {
      mappings(
        AB moves A to B,
        BC moves B to C,
        CC moves C to C
      )
      dispatchToTest()
      initialState = A
      transitionHandler { /* empty */ }
      errorHandler { /* empty */ }
    }.test()

    sm.finish()
    testDispatcher.advanceUntilIdle()

    sm.currentState shouldBe CoreState.Dead
    sm.lastAction shouldBe CoreAction.Death
    sm.transitionHandlers shouldBe emptySet<TransitionHandler>()
    sm.errorHandlers shouldBe emptySet<ErrorHandler>()
    sm.isActive shouldBe false
  }

  // Helpers

  private fun StateMachineDsl.dispatchToTest() = apply {
    defaultDispatcher = testDispatcher
    transitionsDispatcher = testDispatcher
    errorsDispatcher = testDispatcher
  }

  private fun StateMachine.dispatchToTest() = apply {
    setDefaultDispatcher(testDispatcher)
    setTransitionHandlerDispatcher(testDispatcher)
    setErrorHandlerDispatcher(testDispatcher)
  }

  private fun StateMachine.test() = this as StateMachineImpl

}