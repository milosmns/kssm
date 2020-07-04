package me.angrybyte.kssm.api.data

import me.angrybyte.kssm.api.State

@Throws(ClassCastException::class, NullPointerException::class, KotlinNullPointerException::class)
inline fun <reified T> State<T>.requirePayload(): T = payload as T