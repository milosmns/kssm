package me.angrybyte.kssm.api

interface State<out T> {

  val name: String

  val payload: T?

}