package clearcontrol.core.device.queue;

import java.util.concurrent.Future;

/**
 * Interface implemented by all devices that support queues.
 *
 * @param <Q> queue type
 * @author royer
 */
public interface QueueDeviceInterface<Q extends QueueInterface>

{

  /**
   * Creates a queue
   *
   * @return queue
   */
  Q requestQueue();

  /**
   * Plays back a state queue.
   *
   * @param pQueue
   * @return future that represents the execution of the queue.
   */
  Future<Boolean> playQueue(Q pQueue);

}
