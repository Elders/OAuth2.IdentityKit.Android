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

    private var sThreadFactory: ThreadFactory = object : ThreadFactory {
        private val mCount = AtomicInteger(1)

        override fun newThread(r: Runnable): Thread {
            return Thread(r, "IdentityKit #" + mCount.getAndIncrement())
        }
    }

    private var sPoolWorkQueue: BlockingQueue<Runnable> = LinkedBlockingQueue(10)

    private var threadPoolExecutor: ThreadPoolExecutor = ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory)

    private val mTasks = ArrayDeque<Runnable>()

    private var mActive: Runnable? = null

    @Synchronized
    override fun execute(r: Runnable) {
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

    @Synchronized
    private fun scheduleNext() {
        mActive = mTasks.poll()
        if (mActive != null) {
            threadPoolExecutor.execute(mActive!!)
        }
    }

    companion object {
        private const val CORE_POOL_SIZE = 1
        private const val MAXIMUM_POOL_SIZE = 5
        private const val KEEP_ALIVE = 1L
    }
}
