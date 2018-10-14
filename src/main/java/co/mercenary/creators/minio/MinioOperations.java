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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import co.mercenary.creators.minio.content.MinioContentTypeProbe;
import co.mercenary.creators.minio.data.MinioBucket;
import co.mercenary.creators.minio.data.MinioCopyConditions;
import co.mercenary.creators.minio.data.MinioItem;
import co.mercenary.creators.minio.data.MinioObjectStatus;
import co.mercenary.creators.minio.data.MinioUpload;
import co.mercenary.creators.minio.data.MinioUserMetaData;
import co.mercenary.creators.minio.errors.MinioDataException;
import co.mercenary.creators.minio.errors.MinioOperationException;
import co.mercenary.creators.minio.util.MinioUtils;
import co.mercenary.creators.minio.util.WithDescription;
import co.mercenary.creators.minio.util.WithServerData;
import io.minio.ServerSideEncryption;
import io.minio.http.Method;

@JsonIgnoreType
public interface MinioOperations extends WithDescription, WithServerData
{
    @NonNull
    MinioContentTypeProbe getContentTypeProbe();

    @NonNull
    Stream<MinioBucket> getBuckets() throws MinioOperationException;

    @NonNull
    Stream<MinioBucket> getBucketsNamed(@NonNull Predicate<String> filter) throws MinioOperationException;

    @NonNull
    default Stream<MinioBucket> getBucketsNamed(@NonNull final Pattern regex) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(regex);

        return getBucketsNamed(named -> regex.matcher(named).matches());
    }

    @NonNull
    default Stream<MinioBucket> getBucketsMatch(@NonNull final CharSequence regex) throws MinioOperationException
    {
        return getBucketsMatch(regex, new AntPathMatcher());
    }

    @NonNull
    default Stream<MinioBucket> getBucketsMatch(@NonNull final CharSequence regex, @NonNull final PathMatcher match) throws MinioOperationException
    {
        final String value = MinioUtils.requireToString(regex);

        if (match.isPattern(value))
        {
            return getBucketsNamed(named -> match.match(value, named));
        }
        return Stream.empty();
    }

    @NonNull
    default Optional<MinioBucket> getBucket(@NonNull final CharSequence bucket) throws MinioOperationException
    {
        final String value = MinioUtils.requireToString(bucket);

        return getBucketsNamed(named -> value.equals(named)).findFirst();
    }

    boolean isBucket(@NonNull CharSequence bucket) throws MinioOperationException;

    boolean deleteBucket(@NonNull CharSequence bucket) throws MinioOperationException;

    boolean ensureBucket(@NonNull CharSequence bucket) throws MinioOperationException;

    boolean isObject(@NonNull CharSequence bucket, @NonNull CharSequence name) throws MinioOperationException;

    boolean deleteObject(@NonNull CharSequence bucket, @NonNull CharSequence name) throws MinioOperationException;

    void setBucketPolicy(@NonNull CharSequence bucket, @NonNull Object policy) throws MinioOperationException, MinioDataException;

    @NonNull
    String getBucketPolicy(@NonNull CharSequence bucket) throws MinioOperationException;

    @NonNull
    <T> T getBucketPolicy(@NonNull CharSequence bucket, @NonNull Class<T> type) throws MinioOperationException, MinioDataException;

    @NonNull
    MinioUserMetaData getUserMetaData(@NonNull CharSequence bucket, @NonNull CharSequence name) throws MinioOperationException;

    @NonNull
    MinioObjectStatus getObjectStatus(@NonNull CharSequence bucket, @NonNull CharSequence name) throws MinioOperationException;

    @NonNull
    MinioObjectStatus getObjectStatus(@NonNull CharSequence bucket, @NonNull CharSequence name, @NonNull ServerSideEncryption keys) throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(@NonNull CharSequence bucket, @NonNull CharSequence name) throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(@NonNull CharSequence bucket, @NonNull CharSequence name, long skip) throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(@NonNull CharSequence bucket, @NonNull CharSequence name, long skip, long leng) throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(@NonNull CharSequence bucket, @NonNull CharSequence name, @NonNull ServerSideEncryption keys) throws MinioOperationException;

    @NonNull
    default String getSignedObjectUrl(@NonNull final CharSequence bucket, @NonNull final CharSequence name) throws MinioOperationException
    {
        return getSignedObjectUrl(Method.GET, MinioUtils.requireNonNull(bucket), MinioUtils.requireNonNull(name));
    }

    @NonNull
    default String getSignedObjectUrl(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final Long seconds) throws MinioOperationException
    {
        return getSignedObjectUrl(Method.GET, MinioUtils.requireNonNull(bucket), MinioUtils.requireNonNull(name), MinioUtils.requireNonNull(seconds));
    }

    @NonNull
    default String getSignedObjectUrl(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final Duration seconds) throws MinioOperationException
    {
        return getSignedObjectUrl(Method.GET, MinioUtils.requireNonNull(bucket), MinioUtils.requireNonNull(name), MinioUtils.requireNonNull(seconds));
    }

    @NonNull
    default String getSignedObjectUrl(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final Long time, @NonNull final TimeUnit unit) throws MinioOperationException
    {
        return getSignedObjectUrl(Method.GET, MinioUtils.requireNonNull(bucket), MinioUtils.requireNonNull(name), MinioUtils.requireNonNull(time), MinioUtils.requireNonNull(unit));
    }

    @NonNull
    String getSignedObjectUrl(@NonNull Method method, @NonNull CharSequence bucket, @NonNull CharSequence name) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull Method method, @NonNull CharSequence bucket, @NonNull CharSequence name, @NonNull Long seconds) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull Method method, @NonNull CharSequence bucket, @NonNull CharSequence name, @NonNull Duration seconds) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull Method method, @NonNull CharSequence bucket, @NonNull CharSequence name, @NonNull Long time, @NonNull TimeUnit unit) throws MinioOperationException;

    void putObject(@NonNull CharSequence bucket, @NonNull CharSequence name, @NonNull byte[] input, @Nullable CharSequence type) throws MinioOperationException;

    void putObject(@NonNull CharSequence bucket, @NonNull CharSequence name, @NonNull InputStream input, @Nullable CharSequence type) throws MinioOperationException;

    void putObject(@NonNull CharSequence bucket, @NonNull CharSequence name, @NonNull byte[] input, @Nullable CharSequence type, @Nullable MinioUserMetaData meta) throws MinioOperationException;

    void putObject(@NonNull CharSequence bucket, @NonNull CharSequence name, @NonNull byte[] input, @Nullable MinioUserMetaData meta) throws MinioOperationException;

    default void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final byte[] input) throws MinioOperationException
    {
        putObject(MinioUtils.requireNonNull(bucket), MinioUtils.requireNonNull(name), MinioUtils.requireNonNull(input), MinioUtils.NULL(CharSequence.class));
    }

    default void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final InputStream input) throws MinioOperationException
    {
        putObject(MinioUtils.requireNonNull(bucket), MinioUtils.requireNonNull(name), MinioUtils.requireNonNull(input), MinioUtils.NULL(CharSequence.class));
    }

    void putObject(@NonNull CharSequence bucket, @NonNull CharSequence name, @NonNull Resource input, @Nullable CharSequence type) throws MinioOperationException;

    default void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final Resource input) throws MinioOperationException
    {
        putObject(MinioUtils.requireNonNull(bucket), MinioUtils.requireNonNull(name), MinioUtils.requireNonNull(input), MinioUtils.NULL(CharSequence.class));
    }

    void putObject(@NonNull CharSequence bucket, @NonNull CharSequence name, @NonNull File input, @Nullable CharSequence type) throws MinioOperationException;

    default void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final File input) throws MinioOperationException
    {
        putObject(MinioUtils.requireNonNull(bucket), MinioUtils.requireNonNull(name), MinioUtils.requireNonNull(input), MinioUtils.NULL(CharSequence.class));
    }

    void putObject(@NonNull CharSequence bucket, @NonNull CharSequence name, @NonNull Path input, @Nullable CharSequence type) throws MinioOperationException;

    default boolean copyObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final CharSequence target) throws MinioOperationException
    {
        return copyObject(MinioUtils.requireNonNull(bucket), MinioUtils.requireNonNull(name), MinioUtils.requireNonNull(target), MinioUtils.NULL(CharSequence.class), MinioUtils.NULL(MinioCopyConditions.class));
    }

    default boolean copyObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final CharSequence target, @Nullable final CharSequence object) throws MinioOperationException
    {
        return copyObject(MinioUtils.requireNonNull(bucket), MinioUtils.requireNonNull(name), MinioUtils.requireNonNull(target), object, MinioUtils.NULL(MinioCopyConditions.class));
    }

    boolean copyObject(@NonNull CharSequence bucket, @NonNull CharSequence name, @NonNull CharSequence target, @Nullable MinioCopyConditions conditions) throws MinioOperationException;

    boolean copyObject(@NonNull CharSequence bucket, @NonNull CharSequence name, @NonNull CharSequence target, @Nullable CharSequence object, @Nullable MinioCopyConditions conditions) throws MinioOperationException;

    @NonNull
    Stream<MinioItem> getItems(@NonNull CharSequence bucket, @Nullable CharSequence prefix, boolean recursive) throws MinioOperationException;

    @NonNull
    default Stream<MinioItem> getItems(@NonNull final CharSequence bucket) throws MinioOperationException
    {
        return getItems(bucket, MinioUtils.NULL());
    }

    @NonNull
    default Stream<MinioItem> getItems(@NonNull final CharSequence bucket, final boolean recursive) throws MinioOperationException
    {
        return getItems(bucket, MinioUtils.NULL(), recursive);
    }

    @NonNull
    default Optional<MinioItem> getItem(@NonNull final CharSequence bucket, @NonNull final CharSequence name) throws MinioOperationException
    {
        return getItems(bucket, name.toString(), false).findFirst();
    }

    @NonNull
    default Stream<MinioItem> getItems(@NonNull final CharSequence bucket, @Nullable final CharSequence prefix) throws MinioOperationException
    {
        return getItems(bucket, prefix, true);
    }

    @NonNull
    default Stream<MinioUpload> getIncompleteUploads(@NonNull final CharSequence bucket) throws MinioOperationException
    {
        return getIncompleteUploads(bucket, false);
    }

    @NonNull
    default Stream<MinioUpload> getIncompleteUploads(@NonNull final CharSequence bucket, final boolean recursive) throws MinioOperationException
    {
        return getIncompleteUploads(bucket, MinioUtils.NULL(), recursive);
    }

    @NonNull
    default Stream<MinioUpload> getIncompleteUploads(@NonNull final CharSequence bucket, @Nullable final CharSequence prefix) throws MinioOperationException
    {
        return getIncompleteUploads(bucket, prefix, false);
    }

    @NonNull
    Stream<MinioUpload> getIncompleteUploads(@NonNull CharSequence bucket, @Nullable CharSequence prefix, boolean recursive) throws MinioOperationException;

    boolean removeUpload(@NonNull CharSequence bucket, @NonNull CharSequence name) throws MinioOperationException;

    void traceStreamOff();

    void setTraceStream(@Nullable OutputStream stream);

    boolean setUserMetaData(@NonNull CharSequence bucket, @NonNull CharSequence name, @Nullable MinioUserMetaData meta) throws MinioOperationException;

    boolean addUserMetaData(@NonNull CharSequence bucket, @NonNull CharSequence name, @Nullable MinioUserMetaData meta) throws MinioOperationException;
}
