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

import java.io.InputStream;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import co.mercenary.creators.minio.errors.MinioOperationException;
import co.mercenary.creators.minio.util.WithSelf;
import co.mercenary.creators.minio.util.WithServerData;
import io.minio.ServerSideEncryption;
import io.minio.http.Method;

@JsonIgnoreType
public interface MinioItemOperations extends WithSelf<MinioItem>, WithServerData
{
    boolean isFile();

    boolean deleteObject() throws MinioOperationException;

    @NonNull
    Resource getItemResource() throws MinioOperationException;

    @NonNull
    Optional<MinioItem> getItemRelative(@NonNull CharSequence path) throws MinioOperationException;

    @NonNull
    MinioUserMetaData getUserMetaData() throws MinioOperationException;

    @NonNull
    MinioObjectStatus getObjectStatus() throws MinioOperationException;

    @NonNull
    MinioObjectStatus getObjectStatus(@NonNull ServerSideEncryption keys) throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream() throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(long skip) throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(long skip, long leng) throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(@NonNull ServerSideEncryption keys) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl() throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull Long seconds) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull Duration seconds) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull Long time, @NonNull TimeUnit unit) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull Method method) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull Method method, @NonNull Long seconds) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull Method method, @NonNull Duration seconds) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull Method method, @NonNull Long time, @NonNull TimeUnit unit) throws MinioOperationException;

    boolean copyObject(@NonNull CharSequence bucket) throws MinioOperationException;

    boolean copyObject(@NonNull CharSequence bucket, @Nullable CharSequence name) throws MinioOperationException;

    boolean copyObject(@NonNull CharSequence bucket, @Nullable MinioCopyConditions conditions) throws MinioOperationException;

    boolean copyObject(@NonNull CharSequence bucket, @Nullable CharSequence name, @Nullable MinioCopyConditions conditions) throws MinioOperationException;
}
