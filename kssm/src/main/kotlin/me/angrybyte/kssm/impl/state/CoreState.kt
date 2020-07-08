package me.angrybyte.kssm.impl.state

internal sealed class CoreState : SingletonState() {
  object None : CoreState()
  object Incubating : CoreState()
  object Dead : CoreState()
}