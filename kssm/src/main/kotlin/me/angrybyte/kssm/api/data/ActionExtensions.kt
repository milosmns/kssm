package me.angrybyte.kssm.api.data

import me.angrybyte.kssm.api.Action

/**
 * Force casts the payload object into a non-`null` value of type [T] and returns it.
 * If there was no payload set, this call will throw. If you use the wrong type [T], this will throw.
 *
 * @param [T] the type of payload this action carries
 *
 * @throws [ClassCastException] when payload is of a different type from [T]
 * @throws [KotlinNullPointerException] when there's no payload at all (`null` case)
 */
@Throws(ClassCastException::class, KotlinNullPointerException::class)
inline fun <reified T> Action<T>.requirePayload(): T = payload as T