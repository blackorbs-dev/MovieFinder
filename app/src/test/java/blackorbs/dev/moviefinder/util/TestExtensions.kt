/*
 * Copyright 2024 BlackOrbs (blackorbs@icloud.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package blackorbs.dev.moviefinder.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.buffer
import okio.source
import java.nio.charset.StandardCharsets
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

object TestExtensions {

    fun <T> LiveData<T>.getOrAwaitValue(
        time: Long = 2,
        timeUnit: TimeUnit = TimeUnit.SECONDS,
        afterObserve: () -> Unit = {}
    ): T {
        var data: T? = null
        val latch = CountDownLatch(1)
        val observer = object : Observer<T> {
            override fun onChanged(value: T) {
                data = value
                latch.countDown()
                this@getOrAwaitValue.removeObserver(this)
            }
        }
        this.observeForever(observer)

        afterObserve.invoke()

        // Don't wait indefinitely if the LiveData is not set.
        if (!latch.await(time, timeUnit)) {
            this.removeObserver(observer)
            throw TimeoutException("LiveData value was never set.")
        }

        @Suppress("UNCHECKED_CAST")
        return data as T
    }

    suspend fun <T> LiveData<T>.observeForTesting(block: suspend  () -> Unit) {
        val observer = Observer<T> { }
        try {
            observeForever(observer)
            block()
        } finally {
            removeObserver(observer)
        }
    }

    inline fun <T> LiveData<T>.captureValues(block: LiveDataValueCapture<T>.() -> Unit) {
        val capture = LiveDataValueCapture<T>()
        val observer = Observer<T> {
            capture.addValue(it)
        }
        observeForever(observer)
        try {
            capture.block()
        } finally {
            removeObserver(observer)
        }
    }

    internal fun MockWebServer.enqueueResponse(fileName: String, code: Int) {
        val inputStream = javaClass.classLoader?.getResourceAsStream(fileName)

        val source = inputStream?.let { inputStream.source().buffer() }
        source?.let {
            enqueue(
                MockResponse()
                    .setResponseCode(code)
                    .setBody(source.readString(StandardCharsets.UTF_8))
            )
        }
    }
}

class LiveDataValueCapture<T> {

    private val lock = Any()

    private val _values = mutableListOf<T?>()
    val values: List<T?>
        get() = synchronized(lock) {
            _values.toList() // copy to avoid returning reference to mutable list
        }

    fun addValue(value: T?) = synchronized(lock) {
        _values += value
    }
}
