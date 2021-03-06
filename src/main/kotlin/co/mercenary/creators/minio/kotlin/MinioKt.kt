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

@file:kotlin.jvm.JvmName("MinioKt")

package co.mercenary.creators.minio.kotlin

import co.mercenary.creators.minio.MinioOperations
import co.mercenary.creators.minio.MinioTemplate
import co.mercenary.creators.minio.data.MinioBucket
import co.mercenary.creators.minio.data.MinioItem
import co.mercenary.creators.minio.data.MinioObjectStatus
import co.mercenary.creators.minio.data.MinioUserMetaData
import co.mercenary.creators.minio.json.JSON
import co.mercenary.creators.minio.json.JSONUtils
import com.fasterxml.jackson.annotation.JsonIgnoreType
import org.springframework.core.io.Resource
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.net.URL
import java.nio.file.Path
import java.util.Optional
import java.util.function.Predicate
import java.util.regex.Pattern
import java.util.stream.Stream

fun <T> Optional<T>.exists(): Boolean = isPresent()

fun <T> optionalOf(): Optional<T> = Optional.empty()

fun <T> optionalOf(v: T?): Optional<T> = Optional.ofNullable(v)

fun <T> optionalOf(v: () -> T?): Optional<T> = Optional.ofNullable(v())

fun <T> Array<T>.sequence(): Sequence<T> = Sequence { iterator() }

fun <T> Stream<T>.sequence(): Sequence<T> = Sequence { iterator() }

fun <T> Collection<T>.sequence(): Sequence<T> = Sequence { iterator() }

internal inline fun <reified T> Stream<T>.expand(): Array<T> = sequence().expand()

internal inline fun <reified T> Sequence<T>.expand(): Array<T> = toList().expand()

internal inline fun <reified T> Collection<T>.expand(): Array<T> = toTypedArray()

@JsonIgnoreType
class MetaDataOf(private val getFunction: () -> MinioUserMetaData, private val setFunction: (MinioUserMetaData?) -> Unit, private val addFunction: (MinioUserMetaData?) -> Unit) {
	var data: MinioUserMetaData
		get() = getFunction()
		set(meta) = setFunction(meta)

	override fun toString(): String = getFunction().toString()

	operator fun invoke(): MinioUserMetaData {
		return getFunction()
	}

	operator fun invoke(meta: MinioUserMetaData): MetaDataOf = apply {
		setFunction(meta)
	}

	operator fun plus(invoke: MinioUserMetaData): MetaDataOf = apply {
		addFunction(invoke)
	}
}

fun metaDataOf(): MinioUserMetaData = MinioUserMetaData()

fun metaDataOf(meta: MetaDataOf): MinioUserMetaData = MinioUserMetaData().add(meta())

fun metaDataOf(k: String, v: String): MinioUserMetaData = MinioUserMetaData(k, v)

fun metaDataOf(meta: Map<String, String>?): MinioUserMetaData = MinioUserMetaData().add(meta)

fun metaDataOf(pair: Pair<String, String>): MinioUserMetaData = MinioUserMetaData(pair.first, pair.second)

fun metaDataOf(vararg list: Pair<String, String>): MinioUserMetaData = MinioUserMetaData().add(hashMapOf(*list))

fun metaDataOf(list: Collection<Pair<String, String>>): MinioUserMetaData = MinioUserMetaData().add(hashMapOf(*(list.expand())))

fun metaDataOf(list: Sequence<Pair<String, String>>): MinioUserMetaData = MinioUserMetaData().add(hashMapOf(*(list.expand())))

fun metaDataOf(list: Stream<Pair<String, String>>): MinioUserMetaData = MinioUserMetaData().add(hashMapOf(*(list.expand())))

operator fun MinioUserMetaData.plus(meta: MetaDataOf): MinioUserMetaData = add(meta())

operator fun MinioUserMetaData.plus(meta: Map<String, String>?): MinioUserMetaData = add(meta)

operator fun MinioUserMetaData.plus(pair: Pair<String, String>): MinioUserMetaData = add(pair.first, pair.second)

operator fun MinioUserMetaData.plus(list: Array<Pair<String, String>>): MinioUserMetaData = add(hashMapOf(*list))

operator fun MinioUserMetaData.plus(list: Collection<Pair<String, String>>): MinioUserMetaData = add(hashMapOf(*(list.expand())))

operator fun MinioUserMetaData.plus(list: Sequence<Pair<String, String>>): MinioUserMetaData = add(hashMapOf(*(list.expand())))

operator fun MinioUserMetaData.plus(list: Stream<Pair<String, String>>): MinioUserMetaData = add(hashMapOf(*(list.expand())))

fun json(): JSON = JSON()

fun json(data: URL): JSON = JSONUtils.toJSON(data)

fun json(data: File): JSON = JSONUtils.toJSON(data)

fun json(data: Path): JSON = JSONUtils.toJSON(data)

fun json(data: Resource): JSON = JSONUtils.toJSON(data)

fun json(data: ByteArray): JSON = JSONUtils.toJSON(data)

fun json(data: Reader, done: Boolean = false): JSON = JSONUtils.toJSON(data, done)

fun json(data: InputStream, done: Boolean = false): JSON = JSONUtils.toJSON(data, done)

fun json(data: MinioItem): JSON = json(data.resource())

fun json(meta: MetaDataOf): JSON = JSON(meta())

fun json(k: String, v: Any?): JSON = JSON(k, v)

fun json(data: Map<String, Any?>?): JSON = JSON(data)

fun json(pair: Pair<String, Any?>): JSON = JSON(pair.first, pair.second)

fun json(vararg list: Pair<String, Any?>): JSON = JSON(hashMapOf(*list))

fun json(list: Collection<Pair<String, Any?>>): JSON = JSON(hashMapOf(*(list.expand())))

fun json(list: Sequence<Pair<String, Any?>>): JSON = JSON(hashMapOf(*(list.expand())))

fun json(list: Stream<Pair<String, Any?>>): JSON = JSON(hashMapOf(*(list.expand())))

operator fun JSON.plus(meta: MetaDataOf): JSON = add(meta())

operator fun JSON.plus(data: Map<String, Any?>?): JSON = add(data)

operator fun JSON.plus(pair: Pair<String, Any?>): JSON = add(pair.first, pair.second)

operator fun JSON.plus(list: Array<Pair<String, Any?>>): JSON = add(hashMapOf(*list))

operator fun JSON.plus(list: Collection<Pair<String, Any?>>): JSON = add(hashMapOf(*(list.expand())))

operator fun JSON.plus(list: Sequence<Pair<String, Any?>>): JSON = add(hashMapOf(*(list.expand())))

operator fun JSON.plus(list: Stream<Pair<String, Any?>>): JSON = add(hashMapOf(*(list.expand())))

fun MinioItem.remove(): Boolean = withOperations().deleteObject()

fun MinioItem.exists(): Boolean = isFile || withOperations().isObject

fun MinioItem.resource(): Resource = withOperations().resource

fun MinioItem.stream(): InputStream = withOperations().objectInputStream

fun MinioItem.status(): MinioObjectStatus = withOperations().objectStatus

fun MinioItem.bucket(): Optional<MinioBucket> = withOperations().findBucket()

fun MinioItem.metaDataOf(): MetaDataOf = with(withOperations()) { MetaDataOf(::getUserMetaData, ::setUserMetaData, ::addUserMetaData) }

fun MinioBucket.remove(): Boolean = withOperations().deleteBucket()

fun MinioBucket.exists(name: String): Boolean = withOperations().isObject(name)

fun MinioBucket.remove(name: String): Boolean = withOperations().deleteObject(name)

fun MinioBucket.stream(name: String): InputStream = withOperations().getObjectInputStream(name)

fun MinioBucket.status(name: String): MinioObjectStatus = withOperations().getObjectStatus(name)

fun MinioBucket.items(prefix: String? = null, recursive: Boolean = true): Sequence<MinioItem> = with(withOperations()) { findItems(prefix, recursive).sequence() }

fun MinioBucket.item(name: String): Optional<MinioItem> = withOperations().findItem(name)

fun MinioBucket.metaDataOf(name: String): MetaDataOf = with(withOperations()) { MetaDataOf({ getUserMetaData(name) }, { meta -> setUserMetaData(name, meta) }, { meta -> addUserMetaData(name, meta) }) }

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

fun MinioOperations.items(bucket: String, recursive: Boolean = true): Sequence<MinioItem> = findItems(bucket, null, recursive).sequence()

fun MinioOperations.items(bucket: String, prefix: String? = null, recursive: Boolean = true): Sequence<MinioItem> = findItems(bucket, prefix, recursive).sequence()

fun MinioOperations.item(bucket: String, name: String): Optional<MinioItem> = findItem(bucket, name)

fun MinioOperations.exists(bucket: String, name: String): Boolean = isObject(bucket, name)

fun MinioOperations.remove(bucket: String, name: String): Boolean = deleteObject(bucket, name)

fun MinioOperations.status(bucket: String, name: String): MinioObjectStatus = getObjectStatus(bucket, name)

fun MinioOperations.stream(bucket: String, name: String): InputStream = getObjectInputStream(bucket, name)

fun MinioOperations.metaDataOf(bucket: String, name: String): MetaDataOf = MetaDataOf({ getUserMetaData(bucket, name) }, { meta -> setUserMetaData(bucket, name, meta) }, { meta -> addUserMetaData(bucket, name, meta) })

fun MinioTemplate.buckets(): Sequence<MinioBucket> = findBuckets().sequence()

fun MinioTemplate.buckets(filter: String): Sequence<MinioBucket> = findBuckets(filter).sequence()

fun MinioTemplate.buckets(filter: Pattern): Sequence<MinioBucket> = findBuckets(filter).sequence()

fun MinioTemplate.buckets(filter: Predicate<String>): Sequence<MinioBucket> = findBuckets(filter).sequence()

fun MinioTemplate.buckets(filter: Collection<String>): Sequence<MinioBucket> = findBuckets(filter).sequence()

fun MinioTemplate.buckets(filter: Sequence<String>): Sequence<MinioBucket> = findBuckets(filter.toSet()).sequence()

fun MinioTemplate.bucket(bucket: String): Optional<MinioBucket> = findBucket(bucket)

fun MinioTemplate.exists(bucket: String): Boolean = isBucket(bucket)

fun MinioTemplate.remove(bucket: String): Boolean = deleteBucket(bucket)

fun MinioTemplate.ensure(bucket: String): Boolean = ensureBucket(bucket)

fun MinioTemplate.policy(bucket: String): String = getBucketPolicy(bucket)

fun MinioTemplate.items(bucket: String, recursive: Boolean = true): Sequence<MinioItem> = findItems(bucket, null, recursive).sequence()

fun MinioTemplate.items(bucket: String, prefix: String? = null, recursive: Boolean = true): Sequence<MinioItem> = findItems(bucket, prefix, recursive).sequence()

fun MinioTemplate.item(bucket: String, name: String): Optional<MinioItem> = findItem(bucket, name)

fun MinioTemplate.exists(bucket: String, name: String): Boolean = isObject(bucket, name)

fun MinioTemplate.remove(bucket: String, name: String): Boolean = deleteObject(bucket, name)

fun MinioTemplate.status(bucket: String, name: String): MinioObjectStatus = getObjectStatus(bucket, name)

fun MinioTemplate.stream(bucket: String, name: String): InputStream = getObjectInputStream(bucket, name)

fun MinioTemplate.metaDataOf(bucket: String, name: String): MetaDataOf = MetaDataOf({ getUserMetaData(bucket, name) }, { meta -> setUserMetaData(bucket, name, meta) }, { meta -> addUserMetaData(bucket, name, meta) })
