package me.angrybyte.kssm.api.data

import me.angrybyte.kssm.api.Action

@Throws(ClassCastException::class, NullPointerException::class, KotlinNullPointerException::class)
inline fun <reified T> Action<T>.requirePayload(): T = payload as T