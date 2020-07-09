package me.angrybyte.kssm.impl.state

import me.angrybyte.kssm.api.State

/**
 * A default [State] implementation that requires both a name and a payload.
 * It's meant to be used within the state machine DSL. For example:
 *
 * ```kotlin
 * val a = payloadState(name = "StateA", payload = 45)
 * ```
 */
internal data class PayloadState<T>(
  override val name: String,
  override val payload: T?
) : State<T> {

  override fun toString() = "PayloadState ($name/$payload)"

}