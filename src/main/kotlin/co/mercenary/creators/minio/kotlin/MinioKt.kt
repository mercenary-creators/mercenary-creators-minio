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

package co.mercenary.creators.minio.kotlin

import co.mercenary.creators.minio.MinioOperations
import co.mercenary.creators.minio.data.MinioBucket
import co.mercenary.creators.minio.data.MinioItem
import co.mercenary.creators.minio.data.MinioObjectStatus
import co.mercenary.creators.minio.data.MinioUserMetaData
import co.mercenary.creators.minio.util.MinioUtils
import java.io.InputStream
import java.util.Optional
import java.util.function.Predicate
import java.util.regex.Pattern
import java.util.stream.Stream

fun <T> Stream<T>.sequence(): Sequence<T> = Sequence { iterator() }

data class Meta(val getFunction: () -> MinioUserMetaData, val setFunction: (MinioUserMetaData?) -> Unit, val addFunction: (MinioUserMetaData?) -> Unit) {
	var data: MinioUserMetaData
		get() = getFunction()
		set(meta) = setFunction(meta)

	override fun toString(): String = getFunction().toString()

	operator fun invoke(): MinioUserMetaData {
		return getFunction()
	}

	operator fun invoke(meta: MinioUserMetaData): Meta = apply {
		setFunction(meta)
	}

	operator fun plus(invoke: MinioUserMetaData): Meta = apply {
		addFunction(invoke)
	}
}

fun metaDataOf(): MinioUserMetaData = MinioUserMetaData()

fun metaDataOf(meta: Meta): MinioUserMetaData = MinioUserMetaData().add(meta())

fun metaDataOf(k: String, v: String): MinioUserMetaData = MinioUserMetaData(k, v)

fun metaDataOf(meta: Map<String, String>?): MinioUserMetaData = MinioUserMetaData().add(meta)

fun metaDataOf(pair: Pair<String, String>): MinioUserMetaData = MinioUserMetaData(pair.first, pair.second)

fun metaDataOf(vararg list: Pair<String, String>): MinioUserMetaData = MinioUserMetaData().add(hashMapOf(*list))

fun metaDataOf(list: Collection<Pair<String, String>>): MinioUserMetaData = MinioUserMetaData().add(hashMapOf(*(list.toTypedArray())))

operator fun MinioUserMetaData.plus(meta: Meta): MinioUserMetaData = MinioUserMetaData().add(meta())

operator fun MinioUserMetaData.plus(meta: Map<String, String>?): MinioUserMetaData = MinioUserMetaData().add(meta)

operator fun MinioUserMetaData.plus(pair: Pair<String, String>): MinioUserMetaData = MinioUserMetaData(pair.first, pair.second)

operator fun MinioUserMetaData.plus(list: Array<Pair<String, String>>): MinioUserMetaData = MinioUserMetaData().add(hashMapOf(*list))

operator fun MinioUserMetaData.plus(list: Collection<Pair<String, String>>): MinioUserMetaData = MinioUserMetaData().add(hashMapOf(*(list.toTypedArray())))

fun MinioItem.remove(): Boolean = withOperations().deleteObject()

fun MinioItem.exists(): Boolean = isFile || withOperations().isObject

fun MinioItem.stream(): InputStream = withOperations().objectInputStream

fun MinioItem.status(): MinioObjectStatus = withOperations().objectStatus

fun MinioItem.bucket(): Optional<MinioBucket> = withOperations().findBucket()

fun MinioItem.meta(): Meta = with(withOperations()) { Meta(::getUserMetaData, ::setUserMetaData, ::addUserMetaData) }

fun MinioBucket.remove(): Boolean = withOperations().deleteBucket()

fun MinioBucket.exists(name: String): Boolean = withOperations().isObject(name)

fun MinioBucket.remove(name: String): Boolean = withOperations().deleteObject(name)

fun MinioBucket.stream(name: String): InputStream = withOperations().getObjectInputStream(name)

fun MinioBucket.status(name: String): MinioObjectStatus = withOperations().getObjectStatus(name)

fun MinioBucket.items(prefix: String? = MinioUtils.NULL(), recursive: Boolean = true): Sequence<MinioItem> = with(withOperations()) { findItems(prefix, recursive).sequence() }

fun MinioBucket.item(name: String): Optional<MinioItem> = withOperations().findItem(name)

fun MinioBucket.meta(name: String): Meta = with(withOperations()) { Meta({ getUserMetaData(name) }, { meta -> setUserMetaData(name, meta) }, { meta -> addUserMetaData(name, meta) }) }

fun MinioOperations.buckets(): Sequence<MinioBucket> = findBuckets().sequence()

fun MinioOperations.buckets(filter: String): Sequence<MinioBucket> = findBuckets(filter).sequence()

fun MinioOperations.buckets(filter: Pattern): Sequence<MinioBucket> = findBuckets(filter).sequence()

fun MinioOperations.buckets(filter: Predicate<String>): Sequence<MinioBucket> = findBuckets(filter).sequence()

fun MinioOperations.buckets(filter: Collection<String>): Sequence<MinioBucket> = findBuckets(filter).sequence()

fun MinioOperations.buckets(filter: Sequence<String>): Sequence<MinioBucket> = findBuckets(filter.toSet()).sequence()

fun MinioOperations.bucket(bucket: String): Optional<MinioBucket> = findBucket(bucket)

fun MinioOperations.exists(bucket: String): Boolean = isBucket(bucket)

fun MinioOperations.remove(bucket: String): Boolean = deleteBucket(bucket)

fun MinioOperations.ensure(bucket: String): Boolean = ensureBucket(bucket)

fun MinioOperations.policy(bucket: String): String = getBucketPolicy(bucket)

fun MinioOperations.items(bucket: String, recursive: Boolean = true): Sequence<MinioItem> = findItems(bucket, MinioUtils.NULL(), recursive).sequence()

fun MinioOperations.items(bucket: String, prefix: String? = MinioUtils.NULL(), recursive: Boolean = true): Sequence<MinioItem> = findItems(bucket, prefix, recursive).sequence()

fun MinioOperations.item(bucket: String, name: String): Optional<MinioItem> = findItem(bucket, name)

fun MinioOperations.exists(bucket: String, name: String): Boolean = isObject(bucket, name)

fun MinioOperations.remove(bucket: String, name: String): Boolean = deleteObject(bucket, name)

fun MinioOperations.status(bucket: String, name: String): MinioObjectStatus = getObjectStatus(bucket, name)

fun MinioOperations.stream(bucket: String, name: String): InputStream = getObjectInputStream(bucket, name)

fun MinioOperations.meta(bucket: String, name: String): Meta = Meta({ getUserMetaData(bucket, name) }, { meta -> setUserMetaData(bucket, name, meta) }, { meta -> addUserMetaData(bucket, name, meta) })
