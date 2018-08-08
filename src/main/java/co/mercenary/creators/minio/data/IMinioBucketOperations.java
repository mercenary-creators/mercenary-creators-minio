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
import java.security.KeyPair;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.crypto.SecretKey;

import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import co.mercenary.creators.minio.MinioMethod;
import co.mercenary.creators.minio.MinioOperationException;

public interface IMinioBucketOperations
{
    boolean deleteBucket() throws MinioOperationException;

    boolean isObject(@NonNull CharSequence name) throws MinioOperationException;

    boolean deleteObject(@NonNull CharSequence name) throws MinioOperationException;

    @NonNull
    String getBucketPolicy() throws MinioOperationException;

    void setBucketPolicy(@NonNull CharSequence policy) throws MinioOperationException;

    @NonNull
    MinioObjectStatus getObjectStatus(@NonNull CharSequence name) throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(@NonNull CharSequence name) throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(@NonNull CharSequence name, long skip) throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(@NonNull CharSequence name, long skip, long leng) throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(@NonNull CharSequence name, @NonNull KeyPair keys) throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(@NonNull CharSequence name, @NonNull SecretKey keys) throws MinioOperationException;

    void putObject(@NonNull CharSequence name, @NonNull File file, @Nullable CharSequence type) throws MinioOperationException;

    void putObject(@NonNull CharSequence name, @NonNull File file, long size, @Nullable CharSequence type) throws MinioOperationException;

    void putObject(@NonNull CharSequence name, @NonNull Path path, @Nullable CharSequence type) throws MinioOperationException;

    void putObject(@NonNull CharSequence name, @NonNull Path path, long size, @Nullable CharSequence type) throws MinioOperationException;

    void putObject(@NonNull CharSequence name, @NonNull byte[] input, @Nullable CharSequence type) throws MinioOperationException;

    void putObject(@NonNull CharSequence name, @NonNull Resource resource, @Nullable CharSequence type) throws MinioOperationException;

    void putObject(@NonNull CharSequence name, @NonNull InputStream input, @Nullable CharSequence type) throws MinioOperationException;

    void putObject(@NonNull CharSequence name, @NonNull InputStream input, long size, @Nullable CharSequence type) throws MinioOperationException;

    default void putObject(@NonNull final CharSequence name, @NonNull final InputStream input) throws MinioOperationException
    {
        putObject(name, input, null);
    }

    default void putObject(@NonNull final CharSequence name, @NonNull final InputStream input, final long size) throws MinioOperationException
    {
        putObject(name, input, size, null);
    }

    default void putObject(@NonNull final CharSequence name, @NonNull final byte[] input) throws MinioOperationException
    {
        putObject(name, input, null);
    }

    default void putObject(@NonNull final CharSequence name, @NonNull final Resource resource) throws MinioOperationException
    {
        putObject(name, resource, null);
    }

    default void putObject(@NonNull final CharSequence name, @NonNull final File file) throws MinioOperationException
    {
        putObject(name, file, null);
    }

    default void putObject(@NonNull final CharSequence name, @NonNull final File file, final long size) throws MinioOperationException
    {
        putObject(name, file, size, null);
    }

    default void putObject(@NonNull final CharSequence name, @NonNull final Path path) throws MinioOperationException
    {
        putObject(name, path, null);
    }

    default void putObject(@NonNull final CharSequence name, @NonNull final Path path, final long size) throws MinioOperationException
    {
        putObject(name, path, size, null);
    }

    @NonNull
    Stream<MinioItem> getItems(@Nullable CharSequence prefix, boolean recursive) throws MinioOperationException;

    @NonNull
    default Stream<MinioItem> getItems(final boolean recursive) throws MinioOperationException
    {
        return getItems(null, recursive);
    }

    @NonNull
    default Stream<MinioItem> getItems() throws MinioOperationException
    {
        return getItems(true);
    }

    @NonNull
    default String getSignedObjectUrl(@NonNull final CharSequence name) throws MinioOperationException
    {
        return getSignedObjectUrl(MinioMethod.GET, name);
    }

    @NonNull
    default String getSignedObjectUrl(@NonNull final CharSequence name, final long seconds) throws MinioOperationException
    {
        return getSignedObjectUrl(MinioMethod.GET, name, seconds);
    }

    @NonNull
    default String getSignedObjectUrl(@NonNull final CharSequence name, @NonNull final Duration seconds) throws MinioOperationException
    {
        return getSignedObjectUrl(MinioMethod.GET, name, seconds);
    }

    @NonNull
    default String getSignedObjectUrl(@NonNull final CharSequence name, final long time, @NonNull final TimeUnit unit) throws MinioOperationException
    {
        return getSignedObjectUrl(MinioMethod.GET, name, time, unit);
    }

    @NonNull
    String getSignedObjectUrl(@NonNull MinioMethod method, @NonNull CharSequence name) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull MinioMethod method, @NonNull CharSequence name, long seconds) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull MinioMethod method, @NonNull CharSequence name, @NonNull Duration seconds) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull MinioMethod method, @NonNull CharSequence name, long time, @NonNull TimeUnit unit) throws MinioOperationException;

    default boolean copyObject(@NonNull final CharSequence name, @NonNull final CharSequence destbucket) throws MinioOperationException
    {
        return copyObject(name, destbucket, null, null);
    }

    default boolean copyObject(@NonNull final CharSequence name, @NonNull final CharSequence destbucket, @Nullable final CharSequence destobject) throws MinioOperationException
    {
        return copyObject(name, destbucket, destobject, null);
    }

    boolean copyObject(@NonNull CharSequence name, @NonNull CharSequence destbucket, @Nullable MinioCopyConditions conditions) throws MinioOperationException;

    boolean copyObject(@NonNull CharSequence name, @NonNull CharSequence destbucket, @Nullable CharSequence destobject, @Nullable MinioCopyConditions conditions) throws MinioOperationException;
}
