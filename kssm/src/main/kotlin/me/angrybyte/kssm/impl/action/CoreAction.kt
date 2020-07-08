package me.angrybyte.kssm.impl.action

internal sealed class CoreAction : SingletonAction() {
  object Birth : CoreAction()
  object Death : CoreAction()
}