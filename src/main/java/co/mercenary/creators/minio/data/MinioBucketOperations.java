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

package co.mercenary.creators.minio.data;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import co.mercenary.creators.minio.errors.MinioOperationException;
import co.mercenary.creators.minio.resource.MinioInputStreamResource;
import co.mercenary.creators.minio.util.MinioUtils;
import co.mercenary.creators.minio.util.WithSelf;
import co.mercenary.creators.minio.util.WithServerData;
import io.minio.ServerSideEncryption;
import io.minio.http.Method;

public interface MinioBucketOperations extends WithSelf<MinioBucket>, WithServerData
{
    boolean deleteBucket() throws MinioOperationException;

    boolean isObject(@NonNull CharSequence name) throws MinioOperationException;

    boolean deleteObject(@NonNull CharSequence name) throws MinioOperationException;

    void setBucketPolicy(@NonNull Object policy) throws MinioOperationException;

    @NonNull
    String getBucketPolicy() throws MinioOperationException;

    @NonNull
    <T> T getBucketPolicy(@NonNull Class<T> type) throws MinioOperationException;

    @NonNull
    MinioObjectStatus getObjectStatus(@NonNull CharSequence name) throws MinioOperationException;

    @NonNull
    MinioObjectStatus getObjectStatus(@NonNull CharSequence name, @NonNull ServerSideEncryption keys) throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(@NonNull CharSequence name) throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(@NonNull CharSequence name, long skip) throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(@NonNull CharSequence name, long skip, long leng) throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(@NonNull CharSequence name, @NonNull ServerSideEncryption keys) throws MinioOperationException;

    @NonNull
    default MinioInputStreamResource getObjectInputStreamResource(@NonNull final CharSequence name) throws MinioOperationException
    {
        return new MinioInputStreamResource(getObjectInputStream(name));
    }

    @NonNull
    default MinioInputStreamResource getObjectInputStreamResource(@NonNull final CharSequence name, final long skip) throws MinioOperationException
    {
        return new MinioInputStreamResource(getObjectInputStream(name, skip));
    }

    @NonNull
    default MinioInputStreamResource getObjectInputStreamResource(@NonNull final CharSequence name, final long skip, final long leng) throws MinioOperationException
    {
        return new MinioInputStreamResource(getObjectInputStream(name, skip, leng));
    }

    @NonNull
    default MinioInputStreamResource getObjectInputStreamResource(@NonNull final CharSequence name, @NonNull final ServerSideEncryption keys) throws MinioOperationException
    {
        return new MinioInputStreamResource(getObjectInputStream(name, keys));
    }

    void putObject(@NonNull CharSequence name, @NonNull File input, @Nullable CharSequence type) throws MinioOperationException;

    void putObject(@NonNull CharSequence name, @NonNull File input, long size, @Nullable CharSequence type) throws MinioOperationException;

    void putObject(@NonNull CharSequence name, @NonNull Path input, @Nullable CharSequence type) throws MinioOperationException;

    void putObject(@NonNull CharSequence name, @NonNull Path input, long size, @Nullable CharSequence type) throws MinioOperationException;

    void putObject(@NonNull CharSequence name, @NonNull byte[] input, @Nullable CharSequence type) throws MinioOperationException;

    void putObject(@NonNull CharSequence name, @NonNull Resource input, @Nullable CharSequence type) throws MinioOperationException;

    void putObject(@NonNull CharSequence name, @NonNull InputStream input, @Nullable CharSequence type) throws MinioOperationException;

    void putObject(@NonNull CharSequence name, @NonNull InputStream input, long size, @Nullable CharSequence type) throws MinioOperationException;

    default void putObject(@NonNull final CharSequence name, @NonNull final InputStream input) throws MinioOperationException
    {
        putObject(name, input, MinioUtils.NULL(CharSequence.class));
    }

    default void putObject(@NonNull final CharSequence name, @NonNull final InputStream input, final long size) throws MinioOperationException
    {
        putObject(name, input, size, MinioUtils.NULL(CharSequence.class));
    }

    default void putObject(@NonNull final CharSequence name, @NonNull final byte[] input) throws MinioOperationException
    {
        putObject(name, input, MinioUtils.NULL(CharSequence.class));
    }

    default void putObject(@NonNull final CharSequence name, @NonNull final Resource input) throws MinioOperationException
    {
        putObject(name, input, MinioUtils.NULL(CharSequence.class));
    }

    default void putObject(@NonNull final CharSequence name, @NonNull final File input) throws MinioOperationException
    {
        putObject(name, input, MinioUtils.NULL(CharSequence.class));
    }

    default void putObject(@NonNull final CharSequence name, @NonNull final File input, final long size) throws MinioOperationException
    {
        putObject(name, input, size, MinioUtils.NULL(CharSequence.class));
    }

    default void putObject(@NonNull final CharSequence name, @NonNull final Path input) throws MinioOperationException
    {
        putObject(name, input, MinioUtils.NULL(CharSequence.class));
    }

    default void putObject(@NonNull final CharSequence name, @NonNull final Path input, final long size) throws MinioOperationException
    {
        putObject(name, input, size, MinioUtils.NULL(CharSequence.class));
    }

    @NonNull
    Stream<MinioItem> getItems(@Nullable CharSequence prefix, boolean recursive) throws MinioOperationException;

    @NonNull
    default Stream<MinioItem> getItems(final boolean recursive) throws MinioOperationException
    {
        return getItems(MinioUtils.NULL(CharSequence.class), recursive);
    }

    @NonNull
    default Stream<MinioItem> getItems() throws MinioOperationException
    {
        return getItems(false);
    }

    @NonNull
    default Optional<MinioItem> getItem(@NonNull final CharSequence name) throws MinioOperationException
    {
        return getItems(MinioUtils.requireToString(name), false).findFirst();
    }

    @NonNull
    default String getSignedObjectUrl(@NonNull final CharSequence name) throws MinioOperationException
    {
        return getSignedObjectUrl(Method.GET, name);
    }

    @NonNull
    default String getSignedObjectUrl(@NonNull final CharSequence name, @NonNull final Long seconds) throws MinioOperationException
    {
        return getSignedObjectUrl(Method.GET, name, seconds);
    }

    @NonNull
    default String getSignedObjectUrl(@NonNull final CharSequence name, @NonNull final Duration seconds) throws MinioOperationException
    {
        return getSignedObjectUrl(Method.GET, name, seconds);
    }

    @NonNull
    default String getSignedObjectUrl(@NonNull final CharSequence name, @NonNull final Long time, @NonNull final TimeUnit unit) throws MinioOperationException
    {
        return getSignedObjectUrl(Method.GET, name, time, unit);
    }

    @NonNull
    String getSignedObjectUrl(@NonNull Method method, @NonNull CharSequence name) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull Method method, @NonNull CharSequence name, @NonNull Long seconds) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull Method method, @NonNull CharSequence name, @NonNull Duration seconds) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull Method method, @NonNull CharSequence name, @NonNull Long time, @NonNull TimeUnit unit) throws MinioOperationException;

    default boolean copyObject(@NonNull final CharSequence name, @NonNull final CharSequence target) throws MinioOperationException
    {
        return copyObject(name, target, MinioUtils.NULL(CharSequence.class), MinioUtils.NULL(MinioCopyConditions.class));
    }

    default boolean copyObject(@NonNull final CharSequence name, @NonNull final CharSequence target, @Nullable final CharSequence object) throws MinioOperationException
    {
        return copyObject(name, target, object, MinioUtils.NULL(MinioCopyConditions.class));
    }

    boolean copyObject(@NonNull CharSequence name, @NonNull CharSequence target, @Nullable MinioCopyConditions conditions) throws MinioOperationException;

    boolean copyObject(@NonNull CharSequence name, @NonNull CharSequence target, @Nullable CharSequence object, @Nullable MinioCopyConditions conditions) throws MinioOperationException;

    @NonNull
    default Stream<MinioUpload> getIncompleteUploads() throws MinioOperationException
    {
        return getIncompleteUploads(MinioUtils.NULL(CharSequence.class), false);
    }

    @NonNull
    default Stream<MinioUpload> getIncompleteUploads(final boolean recursive) throws MinioOperationException
    {
        return getIncompleteUploads(MinioUtils.NULL(CharSequence.class), recursive);
    }

    @NonNull
    default Stream<MinioUpload> getIncompleteUploads(@Nullable final CharSequence prefix) throws MinioOperationException
    {
        return getIncompleteUploads(prefix, false);
    }

    @NonNull
    Stream<MinioUpload> getIncompleteUploads(@Nullable CharSequence prefix, boolean recursive) throws MinioOperationException;
}
