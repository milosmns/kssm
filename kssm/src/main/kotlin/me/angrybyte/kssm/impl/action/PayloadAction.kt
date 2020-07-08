package me.angrybyte.kssm.impl.action

import me.angrybyte.kssm.api.Action

internal data class PayloadAction<T>(
  override val name: String,
  override val payload: T?
) : Action<T> {

  override fun toString() = "PayloadAction ($name/$payload)"

}