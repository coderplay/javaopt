/**
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package javaopt.queue;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class QueuePerfTest {
  public static final int QUEUE_CAPACITY = 1 << 15;
  public static final int ITERATION = 50000000;

  public static class Producer implements Runnable {
    private final Queue<Integer> queue;

    public Producer(final Queue<Integer> queue) {
      this.queue = queue;
    }

    public void run() {
      int i = ITERATION;
      do {
        Integer msg = new Integer(i);
        while (!queue.offer(msg)) {
          Thread.yield();
        }
      } while (0 != --i);
    }
  }

  public static void main(final String[] args) throws Exception {
    System.out.println("capacity:" + QUEUE_CAPACITY + " iteration:" + ITERATION);
    int op = Integer.parseInt(args[0]);
    final Queue<Integer> queue = createQueue(op);

    for (int i = 0; i < 20; i++) {
      System.gc();
      performanceRun(i,queue, op);
    }
  }

  private static Queue<Integer> createQueue(final int option) {
    switch (option) {
    case 0:
      return new ArrayBlockingQueue<Integer>(QUEUE_CAPACITY);
    case 1:
      return new P1C1QueueStep1<Integer>(QUEUE_CAPACITY);
    case 2:
      return new P1C1QueueStep2<Integer>(QUEUE_CAPACITY);
    case 3:
      return new P1C1QueueStep3<Integer>(QUEUE_CAPACITY);
    case 4:
      return new P1C1QueueStep4<Integer>(QUEUE_CAPACITY);
    case 5:
      return new P1C1QueueStep5(QUEUE_CAPACITY);
    case 6:
      return new P1C1QueueStep6(QUEUE_CAPACITY);
    default:
      throw new IllegalArgumentException("Invalid option: " + option);
    }
  }

  private static void performanceRun(final int runNumber,
      final Queue<Integer> queue, int op) throws Exception {
    final long start = System.nanoTime();

    final Thread thread = new Thread(new Producer(queue));
    thread.start();

    
    Integer e;
    long result = 0;
    int i = ITERATION;
    do {
      while (null == (e = queue.poll())) {
        Thread.yield();
      }
      result += e.intValue();
    } while (0 != --i);

    thread.join();

    final long duration = System.nanoTime() - start;
    final long ops = (ITERATION * 1000L * 1000L * 1000L) / duration;
    System.out.format("optimize step %d : %d - ops/sec=%,d result=%d\n", op,
        Integer.valueOf(runNumber), Long.valueOf(ops), result);
  }

}
