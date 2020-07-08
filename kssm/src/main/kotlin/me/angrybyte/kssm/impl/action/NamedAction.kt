package me.angrybyte.kssm.impl.action

import me.angrybyte.kssm.api.Action

internal data class NamedAction(
  override val name: String
) : Action<Nothing> {

  override val payload = null

  override fun toString() = "NamedAction [$name]"

}