/*
 * Copyright (c) 2017. Elders LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.eldersoss.identitykit

import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by IvanVatov on 9/18/2017.
 */
class SerialTaskExecutor : Executor {

    private val CORE_POOL_SIZE = 5
    private val MAXIMUM_POOL_SIZE = 128
    private val KEEP_ALIVE = 1

    private var sThreadFactory: ThreadFactory? = null
    private var sPoolWorkQueue: BlockingQueue<Runnable>? = null


    private var THREAD_POOL_EXECUTOR: ThreadPoolExecutor? = null

    init {
        sPoolWorkQueue = LinkedBlockingQueue(10)
        sThreadFactory = object : ThreadFactory {
            private val mCount = AtomicInteger(1)

            override fun newThread(r: Runnable): Thread {
                return Thread(r, "IdentityKit #" + mCount.getAndIncrement())
            }
        }
        THREAD_POOL_EXECUTOR = ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE.toLong(), TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory)
    }

    internal val mTasks = ArrayDeque<Runnable>()
    internal var mActive: Runnable? = null

    @Synchronized override fun execute(r: Runnable) {
        mTasks.offer(Runnable {
            try {
                r.run()
            } finally {
                scheduleNext()
            }
        })
        if (mActive == null) {
            scheduleNext()
        }
    }

    @Synchronized protected fun scheduleNext() {
        mActive = mTasks.poll()
        if (mActive != null) {
            THREAD_POOL_EXECUTOR!!.execute(mActive!!)
        }
    }
}
