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
    default Stream<MinioBucket> getBucketsMatch(@NonNull final String regex) throws MinioOperationException
    {
        return getBucketsMatch(regex, new AntPathMatcher());
    }

    @NonNull
    default Stream<MinioBucket> getBucketsMatch(@NonNull final String regex, @NonNull final PathMatcher match) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(regex, match);

        if (match.isPattern(regex))
        {
            return getBucketsNamed(named -> match.match(regex, named));
        }
        return Stream.empty();
    }

    @NonNull
    default Optional<MinioBucket> getBucket(@NonNull final String bucket) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket);

        return getBucketsNamed(named -> bucket.equals(named)).findFirst();
    }

    boolean isBucket(@NonNull String bucket) throws MinioOperationException;

    boolean deleteBucket(@NonNull String bucket) throws MinioOperationException;

    boolean ensureBucket(@NonNull String bucket) throws MinioOperationException;

    boolean isObject(@NonNull String bucket, @NonNull String name) throws MinioOperationException;

    boolean deleteObject(@NonNull String bucket, @NonNull String name) throws MinioOperationException;

    void setBucketPolicy(@NonNull String bucket, @NonNull Object policy) throws MinioOperationException, MinioDataException;

    @NonNull
    String getBucketPolicy(@NonNull String bucket) throws MinioOperationException;

    @NonNull
    <T> T getBucketPolicy(@NonNull String bucket, @NonNull Class<T> type) throws MinioOperationException, MinioDataException;

    @NonNull
    MinioUserMetaData getUserMetaData(@NonNull String bucket, @NonNull String name) throws MinioOperationException;

    @NonNull
    MinioObjectStatus getObjectStatus(@NonNull String bucket, @NonNull String name) throws MinioOperationException;

    @NonNull
    MinioObjectStatus getObjectStatus(@NonNull String bucket, @NonNull String name, @NonNull ServerSideEncryption keys) throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(@NonNull String bucket, @NonNull String name) throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(@NonNull String bucket, @NonNull String name, long skip) throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(@NonNull String bucket, @NonNull String name, long skip, long leng) throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(@NonNull String bucket, @NonNull String name, @NonNull ServerSideEncryption keys) throws MinioOperationException;

    @NonNull
    default String getSignedObjectUrl(@NonNull final String bucket, @NonNull final String name) throws MinioOperationException
    {
        return getSignedObjectUrl(Method.GET, bucket, name);
    }

    @NonNull
    default String getSignedObjectUrl(@NonNull final String bucket, @NonNull final String name, @NonNull final Long seconds) throws MinioOperationException
    {
        return getSignedObjectUrl(Method.GET, bucket, name, seconds);
    }

    @NonNull
    default String getSignedObjectUrl(@NonNull final String bucket, @NonNull final String name, @NonNull final Duration seconds) throws MinioOperationException
    {
        return getSignedObjectUrl(Method.GET, bucket, name, seconds);
    }

    @NonNull
    default String getSignedObjectUrl(@NonNull final String bucket, @NonNull final String name, @NonNull final Long time, @NonNull final TimeUnit unit) throws MinioOperationException
    {
        return getSignedObjectUrl(Method.GET, bucket, name, time, unit);
    }

    @NonNull
    String getSignedObjectUrl(@NonNull Method method, @NonNull String bucket, @NonNull String name) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull Method method, @NonNull String bucket, @NonNull String name, @NonNull Long seconds) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull Method method, @NonNull String bucket, @NonNull String name, @NonNull Duration seconds) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull Method method, @NonNull String bucket, @NonNull String name, @NonNull Long time, @NonNull TimeUnit unit) throws MinioOperationException;

    void putObject(@NonNull String bucket, @NonNull String name, @NonNull byte[] input, @Nullable String type) throws MinioOperationException;

    void putObject(@NonNull String bucket, @NonNull String name, @NonNull InputStream input, @Nullable String type) throws MinioOperationException;

    void putObject(@NonNull String bucket, @NonNull String name, @NonNull byte[] input, @Nullable String type, @Nullable MinioUserMetaData meta) throws MinioOperationException;

    void putObject(@NonNull String bucket, @NonNull String name, @NonNull byte[] input, @Nullable MinioUserMetaData meta) throws MinioOperationException;

    default void putObject(@NonNull final String bucket, @NonNull final String name, @NonNull final byte[] input) throws MinioOperationException
    {
        putObject(bucket, name, input, MinioUtils.NULL(), MinioUtils.NULL());
    }

    default void putObject(@NonNull final String bucket, @NonNull final String name, @NonNull final InputStream input) throws MinioOperationException
    {
        putObject(bucket, name, input, MinioUtils.NULL());
    }

    void putObject(@NonNull String bucket, @NonNull String name, @NonNull Resource input, @Nullable String type) throws MinioOperationException;

    default void putObject(@NonNull final String bucket, @NonNull final String name, @NonNull final Resource input) throws MinioOperationException
    {
        putObject(bucket, name, input, MinioUtils.NULL());
    }

    void putObject(@NonNull String bucket, @NonNull String name, @NonNull File input, @Nullable String type) throws MinioOperationException;

    default void putObject(@NonNull final String bucket, @NonNull final String name, @NonNull final File input) throws MinioOperationException
    {
        putObject(bucket, name, input, MinioUtils.NULL());
    }

    void putObject(@NonNull String bucket, @NonNull String name, @NonNull Path input, @Nullable String type) throws MinioOperationException;

    default boolean copyObject(@NonNull final String bucket, @NonNull final String name, @NonNull final String target) throws MinioOperationException
    {
        return copyObject(bucket, name, target, MinioUtils.NULL(), MinioUtils.NULL());
    }

    default boolean copyObject(@NonNull final String bucket, @NonNull final String name, @NonNull final String target, @Nullable final String object) throws MinioOperationException
    {
        return copyObject(bucket, name, target, object, MinioUtils.NULL());
    }

    boolean copyObject(@NonNull String bucket, @NonNull String name, @NonNull String target, @Nullable MinioCopyConditions conditions) throws MinioOperationException;

    boolean copyObject(@NonNull String bucket, @NonNull String name, @NonNull String target, @Nullable String object, @Nullable MinioCopyConditions conditions) throws MinioOperationException;

    @NonNull
    Stream<MinioItem> getItems(@NonNull String bucket, @Nullable String prefix, boolean recursive) throws MinioOperationException;

    @NonNull
    default Stream<MinioItem> getItems(@NonNull final String bucket) throws MinioOperationException
    {
        return getItems(bucket, MinioUtils.NULL());
    }

    @NonNull
    default Stream<MinioItem> getItems(@NonNull final String bucket, final boolean recursive) throws MinioOperationException
    {
        return getItems(bucket, MinioUtils.NULL(), recursive);
    }

    @NonNull
    default Optional<MinioItem> getItem(@NonNull final String bucket, @NonNull final String name) throws MinioOperationException
    {
        return getItems(bucket, name, false).findFirst();
    }

    @NonNull
    default Stream<MinioItem> getItems(@NonNull final String bucket, @Nullable final String prefix) throws MinioOperationException
    {
        return getItems(bucket, prefix, true);
    }

    @NonNull
    default Stream<MinioUpload> getIncompleteUploads(@NonNull final String bucket) throws MinioOperationException
    {
        return getIncompleteUploads(bucket, false);
    }

    @NonNull
    default Stream<MinioUpload> getIncompleteUploads(@NonNull final String bucket, final boolean recursive) throws MinioOperationException
    {
        return getIncompleteUploads(bucket, MinioUtils.NULL(), recursive);
    }

    @NonNull
    default Stream<MinioUpload> getIncompleteUploads(@NonNull final String bucket, @Nullable final String prefix) throws MinioOperationException
    {
        return getIncompleteUploads(bucket, prefix, false);
    }

    @NonNull
    Stream<MinioUpload> getIncompleteUploads(@NonNull String bucket, @Nullable String prefix, boolean recursive) throws MinioOperationException;

    boolean removeUpload(@NonNull String bucket, @NonNull String name) throws MinioOperationException;

    void traceStreamOff();

    void setTraceStream(@Nullable OutputStream stream);

    boolean setUserMetaData(@NonNull String bucket, @NonNull String name, @Nullable MinioUserMetaData meta) throws MinioOperationException;

    boolean addUserMetaData(@NonNull String bucket, @NonNull String name, @Nullable MinioUserMetaData meta) throws MinioOperationException;
}
