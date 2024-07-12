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

import blackorbs.dev.moviefinder.util.MovieUtil.getResourceData
import blackorbs.dev.moviefinder.util.TestHelper.IO_EXCEPTION
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.mockwebserver.SocketPolicy
import okio.Buffer

class MockWebServerDispatcher {
    internal inner class RequestDispatcher(private val query: String) : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            return when {
                query == IO_EXCEPTION -> MockResponse().setBody(Buffer().write(ByteArray(4096))).setSocketPolicy(SocketPolicy.DISCONNECT_DURING_RESPONSE_BODY)
                request.path.equals("/?i=$query") -> successResponse("movie-200.json", this)
                query == "empty" -> successResponse("movies-all-empty.json",this)
                request.path?.startsWith("/?s=$query&page=") == true -> successResponse("movies-all-200.json",this)
                else -> MockResponse().setResponseCode(400)
            }
        }
    }

    internal inner class ErrorDispatcher : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            return MockResponse().setResponseCode(400).setBody("Some error occurred!")
        }
    }

    private fun successResponse(filename: String, dispatcher: Dispatcher): MockResponse {
        return MockResponse().setResponseCode(200).setBody(dispatcher.getResourceData(filename))//.throttleBody(1024,1,TimeUnit.SECONDS)
    }
}