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

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import co.mercenary.creators.minio.errors.MinioDataException;
import co.mercenary.creators.minio.errors.MinioOperationException;
import co.mercenary.creators.minio.util.MinioUtils;
import co.mercenary.creators.minio.util.WithSelf;
import co.mercenary.creators.minio.util.WithServerData;
import io.minio.ServerSideEncryption;
import io.minio.http.Method;

@JsonIgnoreType
public interface MinioBucketOperations extends WithSelf<MinioBucket>, WithServerData
{
    boolean deleteBucket() throws MinioOperationException;

    boolean isObject(@NonNull String name) throws MinioOperationException;

    boolean deleteObject(@NonNull String name) throws MinioOperationException;

    void setBucketPolicy(@NonNull Object policy) throws MinioOperationException, MinioDataException;

    @NonNull
    String getBucketPolicy() throws MinioOperationException;

    @NonNull
    <T> T getBucketPolicy(@NonNull Class<T> type) throws MinioOperationException, MinioDataException;

    @NonNull
    MinioUserMetaData getUserMetaData(@NonNull String name) throws MinioOperationException;

    @NonNull
    MinioObjectStatus getObjectStatus(@NonNull String name) throws MinioOperationException;

    @NonNull
    MinioObjectStatus getObjectStatus(@NonNull String name, @NonNull ServerSideEncryption keys) throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(@NonNull String name) throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(@NonNull String name, long skip) throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(@NonNull String name, long skip, long leng) throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(@NonNull String name, @NonNull ServerSideEncryption keys) throws MinioOperationException;

    void putObject(@NonNull String name, @NonNull File input, @Nullable String type) throws MinioOperationException;

    void putObject(@NonNull String name, @NonNull Path input, @Nullable String type) throws MinioOperationException;

    void putObject(@NonNull String name, @NonNull byte[] input, @Nullable String type) throws MinioOperationException;

    void putObject(@NonNull String name, @NonNull Resource input, @Nullable String type) throws MinioOperationException;

    void putObject(@NonNull String name, @NonNull InputStream input, @Nullable String type) throws MinioOperationException;

    default void putObject(@NonNull final String name, @NonNull final InputStream input) throws MinioOperationException
    {
        putObject(name, input, MinioUtils.NULL());
    }

    default void putObject(@NonNull final String name, @NonNull final byte[] input) throws MinioOperationException
    {
        putObject(name, input, MinioUtils.NULL());
    }

    default void putObject(@NonNull final String name, @NonNull final Resource input) throws MinioOperationException
    {
        putObject(name, input, MinioUtils.NULL());
    }

    default void putObject(@NonNull final String name, @NonNull final File input) throws MinioOperationException
    {
        putObject(name, input, MinioUtils.NULL());
    }

    default void putObject(@NonNull final String name, @NonNull final Path input) throws MinioOperationException
    {
        putObject(name, input, MinioUtils.NULL());
    }

    @NonNull
    Stream<MinioItem> findItems(@Nullable String prefix, boolean recursive) throws MinioOperationException;

    @NonNull
    default Stream<MinioItem> findItems(final boolean recursive) throws MinioOperationException
    {
        return findItems(MinioUtils.NULL(), recursive);
    }

    @NonNull
    default Stream<MinioItem> findItems() throws MinioOperationException
    {
        return findItems(true);
    }

    @NonNull
    default Optional<MinioItem> findItem(@NonNull final String name) throws MinioOperationException
    {
        return findItems(MinioUtils.requireNonNull(name), false).findFirst();
    }

    @NonNull
    default String getSignedObjectUrl(@NonNull final String name) throws MinioOperationException
    {
        return getSignedObjectUrl(Method.GET, name);
    }

    @NonNull
    default String getSignedObjectUrl(@NonNull final String name, @NonNull final Long seconds) throws MinioOperationException
    {
        return getSignedObjectUrl(Method.GET, name, seconds);
    }

    @NonNull
    default String getSignedObjectUrl(@NonNull final String name, @NonNull final Duration seconds) throws MinioOperationException
    {
        return getSignedObjectUrl(Method.GET, name, seconds);
    }

    @NonNull
    default String getSignedObjectUrl(@NonNull final String name, @NonNull final Long time, @NonNull final TimeUnit unit) throws MinioOperationException
    {
        return getSignedObjectUrl(Method.GET, name, time, unit);
    }

    @NonNull
    String getSignedObjectUrl(@NonNull Method method, @NonNull String name) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull Method method, @NonNull String name, @NonNull Long seconds) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull Method method, @NonNull String name, @NonNull Duration seconds) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull Method method, @NonNull String name, @NonNull Long time, @NonNull TimeUnit unit) throws MinioOperationException;

    default boolean copyObject(@NonNull final String name, @NonNull final String target) throws MinioOperationException
    {
        return copyObject(name, target, MinioUtils.NULL(), MinioUtils.NULL());
    }

    default boolean copyObject(@NonNull final String name, @NonNull final String target, @Nullable final String object) throws MinioOperationException
    {
        return copyObject(name, target, object, MinioUtils.NULL());
    }

    default boolean copyObject(@NonNull final String name, @NonNull final String target, @Nullable final MinioCopyConditions conditions) throws MinioOperationException
    {
        return copyObject(name, target, MinioUtils.NULL(), conditions);
    }

    boolean copyObject(@NonNull String name, @NonNull String target, @Nullable String object, @Nullable MinioCopyConditions conditions) throws MinioOperationException;

    @NonNull
    default Stream<MinioUpload> getIncompleteUploads() throws MinioOperationException
    {
        return getIncompleteUploads(MinioUtils.NULL(), false);
    }

    @NonNull
    default Stream<MinioUpload> getIncompleteUploads(final boolean recursive) throws MinioOperationException
    {
        return getIncompleteUploads(MinioUtils.NULL(), recursive);
    }

    @NonNull
    default Stream<MinioUpload> getIncompleteUploads(@Nullable final String prefix) throws MinioOperationException
    {
        return getIncompleteUploads(prefix, false);
    }

    @NonNull
    Stream<MinioUpload> getIncompleteUploads(@Nullable String prefix, boolean recursive) throws MinioOperationException;

    void setUserMetaData(@NonNull String name, @Nullable MinioUserMetaData meta) throws MinioOperationException;

    void addUserMetaData(@NonNull String name, @Nullable MinioUserMetaData meta) throws MinioOperationException;
}
