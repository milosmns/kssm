package me.angrybyte.kssm.impl.action

import me.angrybyte.kssm.api.Action

/**
 * An empty [Action] implementation that requires only a name, but carries no payload.
 * It's meant to be used within the state machine DSL. For example:
 *
 * ```kotlin
 * val a = namedAction("ActionA")
 * ```
 */
internal data class NamedAction(
  override val name: String
) : Action<Nothing> {

  override val payload = null

  override fun toString() = "NamedAction [$name]"

}