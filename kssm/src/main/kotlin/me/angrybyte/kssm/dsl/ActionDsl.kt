package me.angrybyte.kssm.dsl

import me.angrybyte.kssm.api.Action
import me.angrybyte.kssm.impl.action.NamedAction
import me.angrybyte.kssm.impl.action.PayloadAction

fun namedAction(name: String): Action<Nothing> = NamedAction(name)

fun <T> payloadAction(name: String, payload: T?): Action<T> = PayloadAction(name, payload)