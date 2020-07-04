package me.angrybyte.kssm.impl.state

import me.angrybyte.kssm.api.State

data class PayloadState<T>(
  override val name: String,
  override val payload: T?
) : State<T> {

  override fun toString() = "PayloadState ($name/$payload)"

}