package me.angrybyte.kssm.impl.action

import me.angrybyte.kssm.api.Action

open class SingletonAction : Action<Nothing> {

  override val name: String
    get() = this::class.java.simpleName

  override val payload = null

  override fun toString() = "SingletonAction ($name)"

}