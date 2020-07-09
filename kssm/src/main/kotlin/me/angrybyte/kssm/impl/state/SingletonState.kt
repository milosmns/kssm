package me.angrybyte.kssm.impl.state

import me.angrybyte.kssm.api.State

/**
 * A simple [State] implementation that uses the inheritor's class name as state name.
 * It doesn't carry any payload and it's meant to be used with `object`s. For example:
 *
 * ```kotlin
 * object StateA : SingletonState()
 * ```
 */
open class SingletonState : State<Nothing> {

  override val name: String
    get() = this::class.java.simpleName

  override val payload = null

  override fun toString() = "SingletonState ($name)"

}