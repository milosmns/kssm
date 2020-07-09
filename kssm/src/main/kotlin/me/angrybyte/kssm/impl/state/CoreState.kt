package me.angrybyte.kssm.impl.state

/**
 * A predefined list of core states. Used only internally, **do not attempt to use**.
 */
internal sealed class CoreState : SingletonState() {

  /**
   * State does not exist. Replacement for `null`.
   */
  object None : CoreState()

  /**
   * Configuring and preparing the state machine. Don't react to this.
   */
  object Incubating : CoreState()

  /**
   * The final state. Nothing can happen after this state.
   */
  object Dead : CoreState()

}