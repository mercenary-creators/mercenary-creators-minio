/*
 * Copyright (c) 2018, Mercenary Creators Company. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.mercenary.creators.minio.test.util

import co.mercenary.creators.minio.MinioOperations
import co.mercenary.creators.minio.util.AbstractMinioTests
import org.junit.jupiter.api.Assertions
import java.util.stream.Stream

open class KMinioTests :  AbstractMinioTests()
{
     fun minio() : MinioOperations = minioOperations

     fun <T> sequence(source: Stream<T>): Sequence<T> = sequence(source.iterator())

     fun <T> sequence(source: Iterable<T>): Sequence<T> = sequence(source.iterator())

     fun <T> sequence(source: Iterator<T>): Sequence<T> = Sequence { source }

    fun fail() {
        Assertions.fail<Void>()
    }

    fun fail(message: String) {
        Assertions.fail<Void>(message)
    }

    fun fail(message: String, cause: Throwable) {
        Assertions.fail<Void>(message, cause)
    }
}