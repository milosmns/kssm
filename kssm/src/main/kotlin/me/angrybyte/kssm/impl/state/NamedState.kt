package me.angrybyte.kssm.impl.state

import me.angrybyte.kssm.api.State

data class NamedState(
  override val name: String
) : State<Nothing> {

  override val payload = null

  override fun toString() = "NamedState [$name]"

}