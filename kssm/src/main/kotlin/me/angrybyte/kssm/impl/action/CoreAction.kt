package me.angrybyte.kssm.impl.action

import me.angrybyte.kssm.impl.state.CoreState

/**
 * A predefined list of core actions. Used only internally, **do not attempt to use**.
 */
internal sealed class CoreAction : SingletonAction() {

  /**
   * The first action that brings the state machine into existence. Replacement for `null`.
   */
  object Birth : CoreAction()

  /**
   * The last action a state machine can take. Transitions it into [CoreState.Dead].
   */
  object Death : CoreAction()

}