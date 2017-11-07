package com.eldersoss.identitykit

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Created by IvanVatov on 11/6/2017.
 */

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class TestAsyncCode {

    @Test
    fun testAcync() {

        //println("1")
        this.testAsyncCode(2000) { completion ->

            //println("3")
            this.performNetworkRequest(1, 5) {

                //println("4")
                completion()
            }
        }

        //println("2")
        this.testAsyncCode(2000) { completion ->

            //println("5")
            this.performNetworkRequest(1, 1) {

                //println("6")
                completion()
            }
        }
    }

    private fun testAsyncCode(waitTime: Long, closure: (completion: () -> Unit) -> Unit){

        val lock = java.lang.Object()

        val thread = Thread {

            closure {

                synchronized(lock) {

                    lock.notify()
                }
            }
        }

        thread.start()

        synchronized(lock) {

            lock.wait(waitTime)
        }
    }

    private fun performNetworkRequest(op: Int, time: Int, callback: (op: Int) -> Unit) {
        val t = Thread({
            Thread.sleep(time * 100L)
            callback(op)
        })
        t.start()
    }
}