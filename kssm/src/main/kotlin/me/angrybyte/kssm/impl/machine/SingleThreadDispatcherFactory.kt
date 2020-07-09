package me.angrybyte.kssm.impl.machine

import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.Executors.defaultThreadFactory
import java.util.concurrent.ThreadFactory

/**
 * A specialized thread factory that produces new coroutine dispatchers.
 * This implementation only produces single-threaded, queue-like dispatchers for serialized task scheduling.
 *
 * Attempting to use this component directly is an error.
 */
internal object SingleThreadDispatcherFactory {

  /**
   * Creates a new coroutine dispatcher bound to a single-threaded [Executor], to be used in a queue-like fashion.
   * The underlying thread will be set to operate in `daemon` mode, and it comes from the '[defaultThreadFactory]'.
   *
   * The thread's name will be slightly altered by adding the given `prefix` argument to the original thread name.
   *
   * @param prefix A name to append to the start of the underlying thread. No separators are needed within the prefix
   */
  fun create(prefix: String): ExecutorCoroutineDispatcher =
    Executors.newSingleThreadExecutor(newNamedFactory(prefix)).asCoroutineDispatcher()

  private fun newNamedFactory(prefix: String) = ThreadFactory { runnable ->
    defaultThreadFactory()
      .newThread(runnable)
      .apply {
        name = "$prefix-$name"
        isDaemon = true
      }
  }

}