package me.angrybyte.kssm.impl.action

sealed class CoreAction : SingletonAction() {
  object Birth : CoreAction()
  object Death : CoreAction()
}