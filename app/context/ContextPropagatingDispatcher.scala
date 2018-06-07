package context

import java.util.concurrent.TimeUnit

import akka.dispatch._
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{Duration, FiniteDuration}

class ContextPropagatingDispatcher(
                                    _configurator: MessageDispatcherConfigurator,
                                    id: String,
                                    throughput: Int,
                                    throughputDeadlineTime: Duration,
                                    executorServiceFactoryProvider: ExecutorServiceFactoryProvider,
                                    shutdownTimeout: FiniteDuration)
  extends Dispatcher(
    _configurator,
    id,
    throughput,
    throughputDeadlineTime,
    executorServiceFactoryProvider,
    shutdownTimeout) { self =>

  println("Creating ContextPropagatingDispatcher")

  override def prepare(): ExecutionContext = new ExecutionContext {

    private val ref: ContextRef = Context.getFromThread

    override def reportFailure(cause: Throwable): Unit = self.reportFailure(cause)

    override def execute(runnable: Runnable): Unit = self.execute(new Runnable() {
      override def run(): Unit = {
        val oldRef = Context.getFromThread
        Context.setInThread(ref)
        try runnable.run() finally Context.setInThread(oldRef)
      }
    })
  }
}

class ContextPropagatingDispatcherConfigurator(_config: Config, override val prerequisites: DispatcherPrerequisites) extends MessageDispatcherConfigurator(_config, prerequisites) {

  private def getDuration(config: Config, path: String, unit: TimeUnit): FiniteDuration =
    Duration(config.getDuration(path, unit), unit)

  private val instance = {
    new ContextPropagatingDispatcher(
      this,
      config.getString("id"),
      config.getInt("throughput"),
      getDuration(config, "throughput-deadline-time", TimeUnit.NANOSECONDS),
      configureExecutor(),
      getDuration(config, "shutdown-timeout", TimeUnit.MILLISECONDS))
  }

  /**
   * Returns the same dispatcher instance for each invocation
   */
  override def dispatcher(): MessageDispatcher = instance
}