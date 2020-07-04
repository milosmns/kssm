package me.angrybyte.kssm.impl.machine

import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

internal object SingleThreadDispatcherFactory {

  fun create(prefix: String): ExecutorCoroutineDispatcher =
    Executors.newSingleThreadExecutor(newNamedFactory(prefix)).asCoroutineDispatcher()

  private fun newNamedFactory(prefix: String) = ThreadFactory { runnable ->
    Executors.defaultThreadFactory()
      .newThread(runnable)
      .apply {
        name = "$prefix-$name"
        isDaemon = true
      }
  }

}