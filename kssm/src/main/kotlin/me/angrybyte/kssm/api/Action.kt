package me.angrybyte.kssm.api

interface Action<out T> {

  val name: String

  val payload: T?

}