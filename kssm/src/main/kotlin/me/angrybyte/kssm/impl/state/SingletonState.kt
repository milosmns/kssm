package me.angrybyte.kssm.impl.state

import me.angrybyte.kssm.api.State

open class SingletonState : State<Nothing> {

  override val name: String
    get() = this::class.java.simpleName

  override val payload = null

  override fun toString() = "SingletonState ($name)"

}