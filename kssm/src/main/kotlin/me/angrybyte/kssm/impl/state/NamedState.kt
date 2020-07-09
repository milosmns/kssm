package me.angrybyte.kssm.impl.state

import me.angrybyte.kssm.api.State

/**
 * An empty [State] implementation that requires only a name, but carries no payload.
 * It's meant to be used within the state machine DSL. For example:
 *
 * ```kotlin
 * val a = namedState("StateA")
 * ```
 */
internal data class NamedState(
  override val name: String
) : State<Nothing> {

  override val payload = null

  override fun toString() = "NamedState [$name]"

}