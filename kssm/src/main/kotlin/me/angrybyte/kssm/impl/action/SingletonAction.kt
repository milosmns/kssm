package me.angrybyte.kssm.impl.action

import me.angrybyte.kssm.api.Action

/**
 * A simple [Action] implementation that uses the inheritor's class name as action name.
 * It doesn't carry any payload and it's meant to be used with `object`s. For example:
 *
 * ```kotlin
 * object ActionA : SingletonAction()
 * ```
 */
open class SingletonAction : Action<Nothing> {

  override val name: String
    get() = this::class.java.simpleName

  override val payload = null

  override fun toString() = "SingletonAction ($name)"

}