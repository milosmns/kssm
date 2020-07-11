package me.angrybyte.kssm.dsl

import me.angrybyte.kssm.api.Action
import me.angrybyte.kssm.impl.action.NamedAction
import me.angrybyte.kssm.impl.action.PayloadAction

/**
 * Simplifies creation of a new [NamedAction] instance.
 */
fun namedAction(name: String): Action<Nothing> = NamedAction(name)

/**
 * Simplifies creation of a new [PayloadAction] instance.
 */
fun <T> payloadAction(name: String, payload: T?): Action<T> = PayloadAction(name, payload)