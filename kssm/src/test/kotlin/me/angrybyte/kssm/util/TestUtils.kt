package me.angrybyte.kssm.util

import org.junit.jupiter.api.Assertions
import kotlin.reflect.KClass

internal fun <T : Throwable> shouldThrow(
  clazz: KClass<T>,
  block: () -> Unit
): T = Assertions.assertThrows(clazz.java, block)

internal infix fun Any?.shouldBe(other: Any?) = Assertions.assertEquals(other, this)

internal infix fun Any?.shouldBeA(other: KClass<*>) {
  Assertions.assertNotNull(this)
  Assertions.assertTrue(this!!::class == other)
}