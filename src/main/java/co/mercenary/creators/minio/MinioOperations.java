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
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

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
    Stream<MinioBucket> findBuckets() throws MinioOperationException;

    @NonNull
    Stream<MinioBucket> findBuckets(@NonNull String regex) throws MinioOperationException;

    @NonNull
    Stream<MinioBucket> findBuckets(@NonNull Pattern regex) throws MinioOperationException;

    @NonNull
    Stream<MinioBucket> findBuckets(@NonNull Predicate<String> filter) throws MinioOperationException;

    @NonNull
    Stream<MinioBucket> findBuckets(@NonNull Collection<String> filter) throws MinioOperationException;

    @NonNull
    Optional<MinioBucket> findBucket(@NonNull String bucket) throws MinioOperationException;

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
    String getSignedObjectUrl(@NonNull String bucket, @NonNull String name) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull String bucket, @NonNull String name, @NonNull Long seconds) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull String bucket, @NonNull String name, @NonNull Duration seconds) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull String bucket, @NonNull String name, @NonNull Long time, @NonNull TimeUnit unit) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull Method method, @NonNull String bucket, @NonNull String name) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull Method method, @NonNull String bucket, @NonNull String name, @NonNull Long seconds) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull Method method, @NonNull String bucket, @NonNull String name, @NonNull Duration seconds) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull Method method, @NonNull String bucket, @NonNull String name, @NonNull Long time, @NonNull TimeUnit unit) throws MinioOperationException;

    void putObject(@NonNull String bucket, @NonNull String name, @NonNull byte[] input) throws MinioOperationException;

    void putObject(@NonNull String bucket, @NonNull String name, @NonNull byte[] input, @Nullable String type) throws MinioOperationException;

    void putObject(@NonNull String bucket, @NonNull String name, @NonNull byte[] input, @Nullable MinioUserMetaData meta) throws MinioOperationException;

    void putObject(@NonNull String bucket, @NonNull String name, @NonNull byte[] input, @Nullable String type, @Nullable MinioUserMetaData meta) throws MinioOperationException;

    void putObject(@NonNull String bucket, @NonNull String name, @NonNull InputStream input) throws MinioOperationException;

    void putObject(@NonNull String bucket, @NonNull String name, @NonNull InputStream input, @Nullable String type) throws MinioOperationException;

    void putObject(@NonNull String bucket, @NonNull String name, @NonNull InputStream input, @Nullable MinioUserMetaData meta) throws MinioOperationException;

    void putObject(@NonNull String bucket, @NonNull String name, @NonNull InputStream input, @Nullable String type, @Nullable MinioUserMetaData meta) throws MinioOperationException;

    void putObject(@NonNull String bucket, @NonNull String name, @NonNull Resource input) throws MinioOperationException;

    void putObject(@NonNull String bucket, @NonNull String name, @NonNull Resource input, @Nullable String type) throws MinioOperationException;

    void putObject(@NonNull String bucket, @NonNull String name, @NonNull Resource input, @Nullable MinioUserMetaData meta) throws MinioOperationException;

    void putObject(@NonNull String bucket, @NonNull String name, @NonNull Resource input, @Nullable String type, @Nullable MinioUserMetaData meta) throws MinioOperationException;

    void putObject(@NonNull String bucket, @NonNull String name, @NonNull File input) throws MinioOperationException;

    void putObject(@NonNull String bucket, @NonNull String name, @NonNull File input, @Nullable String type) throws MinioOperationException;

    void putObject(@NonNull String bucket, @NonNull String name, @NonNull File input, @Nullable MinioUserMetaData meta) throws MinioOperationException;

    void putObject(@NonNull String bucket, @NonNull String name, @NonNull File input, @Nullable String type, @Nullable MinioUserMetaData meta) throws MinioOperationException;

    void putObject(@NonNull String bucket, @NonNull String name, @NonNull Path input) throws MinioOperationException;

    void putObject(@NonNull String bucket, @NonNull String name, @NonNull Path input, @Nullable String type) throws MinioOperationException;

    void putObject(@NonNull String bucket, @NonNull String name, @NonNull Path input, @Nullable MinioUserMetaData meta) throws MinioOperationException;

    void putObject(@NonNull String bucket, @NonNull String name, @NonNull Path input, @Nullable String type, @Nullable MinioUserMetaData meta) throws MinioOperationException;

    void putObject(@NonNull String bucket, @NonNull String name, @NonNull URL input) throws MinioOperationException;

    void putObject(@NonNull String bucket, @NonNull String name, @NonNull URL input, @Nullable String type) throws MinioOperationException;

    void putObject(@NonNull String bucket, @NonNull String name, @NonNull URL input, @Nullable MinioUserMetaData meta) throws MinioOperationException;

    void putObject(@NonNull String bucket, @NonNull String name, @NonNull URL input, @Nullable String type, @Nullable MinioUserMetaData meta) throws MinioOperationException;

    boolean copyObject(@NonNull String bucket, @NonNull String name, @NonNull String target) throws MinioOperationException;

    boolean copyObject(@NonNull String bucket, @NonNull String name, @NonNull String target, @Nullable String object) throws MinioOperationException;

    boolean copyObject(@NonNull String bucket, @NonNull String name, @NonNull String target, @Nullable MinioCopyConditions conditions) throws MinioOperationException;

    boolean copyObject(@NonNull String bucket, @NonNull String name, @NonNull String target, @Nullable String object, @Nullable MinioCopyConditions conditions) throws MinioOperationException;

    @NonNull
    Stream<MinioItem> findItems(@NonNull String bucket, @Nullable String prefix, boolean recursive) throws MinioOperationException;

    @NonNull
    Stream<MinioItem> findItems(@NonNull String bucket) throws MinioOperationException;

    @NonNull
    Stream<MinioItem> findItems(@NonNull String bucket, boolean recursive) throws MinioOperationException;

    @NonNull
    Optional<MinioItem> findItem(@NonNull String bucket, @NonNull String name) throws MinioOperationException;

    @NonNull
    Stream<MinioItem> findItems(@NonNull String bucket, @Nullable String prefix) throws MinioOperationException;

    @NonNull
    Stream<MinioUpload> getIncompleteUploads(@NonNull String bucket) throws MinioOperationException;

    @NonNull
    Stream<MinioUpload> getIncompleteUploads(@NonNull String bucket, boolean recursive) throws MinioOperationException;

    @NonNull
    Stream<MinioUpload> getIncompleteUploads(@NonNull String bucket, @Nullable String prefix) throws MinioOperationException;

    @NonNull
    Stream<MinioUpload> getIncompleteUploads(@NonNull String bucket, @Nullable String prefix, boolean recursive) throws MinioOperationException;

    boolean removeUpload(@NonNull String bucket, @NonNull String name) throws MinioOperationException;

    void traceStreamOff();

    void setTraceStream(@Nullable OutputStream stream);

    void deleteUserMetaData(@NonNull String bucket, @NonNull String name) throws MinioOperationException;

    void setUserMetaData(@NonNull String bucket, @NonNull String name, @Nullable MinioUserMetaData meta) throws MinioOperationException;

    void addUserMetaData(@NonNull String bucket, @NonNull String name, @Nullable MinioUserMetaData meta) throws MinioOperationException;
}