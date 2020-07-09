package me.angrybyte.kssm.impl.action

import me.angrybyte.kssm.api.Action

/**
 * A default [Action] implementation that requires both a name and a payload.
 * It's meant to be used within the state machine DSL. For example:
 *
 * ```kotlin
 * val a = payloadAction(name = "ActionA", payload = 45)
 * ```
 */
internal data class PayloadAction<T>(
  override val name: String,
  override val payload: T?
) : Action<T> {

  override fun toString() = "PayloadAction ($name/$payload)"

}