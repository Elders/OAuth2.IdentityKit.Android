package com.eldersoss.identitykit

import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Created by IvanVatov on 11/6/2017.
 */

@RunWith(RobolectricTestRunner::class)
class SerialExecutorTests {

    private val serialExecutor = SerialTaskExecutor()

    private var result = intArrayOf()
    private val expected = intArrayOf(8, 5, 22, 3, 10)

    private val mainLock = Object()

    @Test
    fun testSerialExecutor() {

        val workerThread = Thread {
            execute(8, 5)
            execute(5, 7)
            execute(22, 1)
            execute(3, 2)
            execute(10, 4)

            synchronized(mainLock) {
                mainLock.wait(2200)
            }
        }
        workerThread.start()
        workerThread.join()

        assertTrue(result.contentEquals(expected))
    }

    private fun execute(op: Int, time: Int) {

        val runnable = Runnable {
            val lock = Object()

            performNetworkRequest(op, time) {
                result = result.plus(op)
                synchronized(lock) {
                    lock.notify()
                }
            }
            synchronized(lock) {
                lock.wait()
            }
        }
        serialExecutor.execute(runnable)
    }

    private fun performNetworkRequest(op: Int, time: Int, callback: (op: Int) -> Unit) {
        val t = Thread {
            Thread.sleep(time * 100L)
            callback(op)
        }
        t.start()
    }
}