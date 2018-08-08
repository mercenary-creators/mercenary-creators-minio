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

package co.mercenary.creators.minio;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.KeyPair;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.crypto.SecretKey;

import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import co.mercenary.creators.minio.data.MinioBucket;
import co.mercenary.creators.minio.data.MinioCopyConditions;
import co.mercenary.creators.minio.data.MinioItem;
import co.mercenary.creators.minio.data.MinioObjectStatus;
import co.mercenary.creators.minio.data.MinioUpload;

public interface MinioOperations
{
    @NonNull
    Stream<MinioBucket> getBuckets() throws MinioOperationException;

    @NonNull
    Stream<MinioBucket> getBucketsNamed(@NonNull Predicate<String> filter) throws MinioOperationException;

    @NonNull
    default Stream<MinioBucket> getBucketsNamed(@NonNull final Pattern regex) throws MinioOperationException
    {
        MinioUtils.requireNonNull(regex);

        return getBucketsNamed(named -> regex.matcher(named).matches());
    }

    @NonNull
    default Stream<MinioBucket> getBucketsNamed(@NonNull final CharSequence regex) throws MinioOperationException
    {
        return getBucketsNamed(Pattern.compile(MinioUtils.requireToString(regex)));
    }

    @NonNull
    default Optional<MinioBucket> getBucket(@NonNull final CharSequence bucket) throws MinioOperationException
    {
        final String look = MinioUtils.requireToString(bucket);

        return getBucketsNamed(named -> look.equals(named)).findFirst();
    }

    boolean isBucket(@NonNull CharSequence bucket) throws MinioOperationException;

    boolean deleteBucket(@NonNull CharSequence bucket) throws MinioOperationException;

    boolean isObject(@NonNull CharSequence bucket, @NonNull CharSequence name) throws MinioOperationException;

    boolean deleteObject(@NonNull CharSequence bucket, @NonNull CharSequence name) throws MinioOperationException;

    void setBucketPolicy(@NonNull CharSequence bucket, @NonNull CharSequence policy) throws MinioOperationException;

    @NonNull
    String getBucketPolicy(@NonNull CharSequence bucket) throws MinioOperationException;

    @NonNull
    MinioBucket createOrGetBucket(@NonNull CharSequence bucket) throws MinioOperationException;

    @NonNull
    MinioObjectStatus getObjectStatus(@NonNull CharSequence bucket, @NonNull CharSequence name) throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(@NonNull CharSequence bucket, @NonNull CharSequence name) throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(@NonNull CharSequence bucket, @NonNull CharSequence name, long skip) throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(@NonNull CharSequence bucket, @NonNull CharSequence name, long skip, long leng) throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(@NonNull CharSequence bucket, @NonNull CharSequence name, @NonNull KeyPair keys) throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(@NonNull CharSequence bucket, @NonNull CharSequence name, @NonNull SecretKey keys) throws MinioOperationException;

    @NonNull
    default String getSignedObjectUrl(@NonNull final CharSequence bucket, @NonNull final CharSequence name) throws MinioOperationException
    {
        return getSignedObjectUrl(MinioMethod.GET, bucket, name);
    }

    @NonNull
    default String getSignedObjectUrl(@NonNull final CharSequence bucket, @NonNull final CharSequence name, final long seconds) throws MinioOperationException
    {
        return getSignedObjectUrl(MinioMethod.GET, bucket, name, seconds);
    }

    @NonNull
    default String getSignedObjectUrl(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final Duration seconds) throws MinioOperationException
    {
        return getSignedObjectUrl(MinioMethod.GET, bucket, name, seconds);
    }

    @NonNull
    default String getSignedObjectUrl(@NonNull final CharSequence bucket, @NonNull final CharSequence name, final long time, @NonNull final TimeUnit unit) throws MinioOperationException
    {
        return getSignedObjectUrl(MinioMethod.GET, bucket, name, time, unit);
    }

    @NonNull
    String getSignedObjectUrl(@NonNull MinioMethod method, @NonNull CharSequence bucket, @NonNull CharSequence name) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull MinioMethod method, @NonNull CharSequence bucket, @NonNull CharSequence name, long seconds) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull MinioMethod method, @NonNull CharSequence bucket, @NonNull CharSequence name, @NonNull Duration seconds) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull MinioMethod method, @NonNull CharSequence bucket, @NonNull CharSequence name, long time, @NonNull TimeUnit unit) throws MinioOperationException;

    default void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final byte[] input) throws MinioOperationException
    {
        putObject(bucket, name, input, null);
    }

    default void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final byte[] input, @Nullable final CharSequence type) throws MinioOperationException
    {
        putObject(bucket, name, new ByteArrayInputStream(input), input.length, type);
    }

    void putObject(@NonNull CharSequence bucket, @NonNull CharSequence name, @NonNull InputStream input, @Nullable CharSequence type) throws MinioOperationException;

    void putObject(@NonNull CharSequence bucket, @NonNull CharSequence name, @NonNull InputStream input, long size, @Nullable CharSequence type) throws MinioOperationException;

    void putObject(@NonNull CharSequence bucket, @NonNull CharSequence name, @NonNull InputStream input, long size, @Nullable CharSequence type, @NonNull KeyPair keys) throws MinioOperationException;

    void putObject(@NonNull CharSequence bucket, @NonNull CharSequence name, @NonNull InputStream input, long size, @Nullable CharSequence type, @NonNull SecretKey keys) throws MinioOperationException;

    default void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final InputStream input) throws MinioOperationException
    {
        putObject(bucket, name, input, null);
    }

    default void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final InputStream input, final long size) throws MinioOperationException
    {
        putObject(bucket, name, input, size, null);
    }

    void putObject(@NonNull CharSequence bucket, @NonNull CharSequence name, @NonNull Resource resource, @Nullable CharSequence type) throws MinioOperationException;

    default void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final Resource resource) throws MinioOperationException
    {
        putObject(bucket, name, resource, null);
    }

    void putObject(@NonNull CharSequence bucket, @NonNull CharSequence name, @NonNull File file, @Nullable CharSequence type) throws MinioOperationException;

    default void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final File file) throws MinioOperationException
    {
        putObject(bucket, name, file, null);
    }

    void putObject(@NonNull CharSequence bucket, @NonNull CharSequence name, @NonNull File file, long size, @Nullable CharSequence type) throws MinioOperationException;

    default void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final File file, final long size) throws MinioOperationException
    {
        putObject(bucket, name, file, size, null);
    }

    void putObject(@NonNull CharSequence bucket, @NonNull CharSequence name, @NonNull Path path, @Nullable CharSequence type) throws MinioOperationException;

    void putObject(@NonNull CharSequence bucket, @NonNull CharSequence name, @NonNull Path path, long size, @Nullable CharSequence type) throws MinioOperationException;

    default boolean copyObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final CharSequence destbucket) throws MinioOperationException
    {
        return copyObject(bucket, name, destbucket, null, null);
    }

    default boolean copyObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final CharSequence destbucket, @Nullable final CharSequence destobject) throws MinioOperationException
    {
        return copyObject(bucket, name, destbucket, destobject, null);
    }

    boolean copyObject(@NonNull CharSequence bucket, @NonNull CharSequence name, @NonNull CharSequence destbucket, @Nullable MinioCopyConditions conditions) throws MinioOperationException;

    boolean copyObject(@NonNull CharSequence bucket, @NonNull CharSequence name, @NonNull CharSequence destbucket, @Nullable CharSequence destobject, @Nullable MinioCopyConditions conditions) throws MinioOperationException;

    @NonNull
    Stream<MinioItem> getItems(@NonNull CharSequence bucket, @Nullable CharSequence prefix, boolean recursive) throws MinioOperationException;

    @NonNull
    default Stream<MinioItem> getItems(@NonNull final CharSequence bucket) throws MinioOperationException
    {
        return getItems(bucket, null);
    }

    @NonNull
    default Stream<MinioItem> getItems(@NonNull final CharSequence bucket, final boolean recursive) throws MinioOperationException
    {
        return getItems(bucket, null, recursive);
    }

    @NonNull
    default Stream<MinioItem> getItems(@NonNull final CharSequence bucket, @Nullable final CharSequence prefix) throws MinioOperationException
    {
        return getItems(bucket, prefix, true);
    }

    @NonNull
    default Stream<MinioUpload> getIncompleteUploads(@NonNull final CharSequence bucket) throws MinioOperationException
    {
        return getIncompleteUploads(bucket, null);
    }

    @NonNull
    default Stream<MinioUpload> getIncompleteUploads(@NonNull final CharSequence bucket, @Nullable final CharSequence prefix) throws MinioOperationException
    {
        return getIncompleteUploads(bucket, prefix, true);
    }

    @NonNull
    Stream<MinioUpload> getIncompleteUploads(@NonNull final CharSequence bucket, @Nullable final CharSequence prefix, boolean recursive) throws MinioOperationException;
}
