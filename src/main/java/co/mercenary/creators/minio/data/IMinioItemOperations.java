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
import java.security.KeyPair;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import co.mercenary.creators.minio.MinioMethod;
import co.mercenary.creators.minio.MinioOperationException;

public interface IMinioItemOperations
{
    boolean isFile();

    boolean deleteObject() throws MinioOperationException;

    @NonNull
    MinioObjectStatus getObjectStatus() throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream() throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(long skip) throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(long skip, long leng) throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(@NonNull KeyPair keys) throws MinioOperationException;

    @NonNull
    InputStream getObjectInputStream(@NonNull SecretKey keys) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl() throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(long seconds) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull Duration seconds) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(long time, @NonNull TimeUnit unit) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull MinioMethod method) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull MinioMethod method, long seconds) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull MinioMethod method, @NonNull Duration seconds) throws MinioOperationException;

    @NonNull
    String getSignedObjectUrl(@NonNull MinioMethod method, long time, @NonNull TimeUnit unit) throws MinioOperationException;

    boolean copyObject(@NonNull CharSequence bucket) throws MinioOperationException;

    boolean copyObject(@NonNull CharSequence bucket, @Nullable CharSequence name) throws MinioOperationException;

    boolean copyObject(@NonNull CharSequence bucket, @Nullable MinioCopyConditions conditions) throws MinioOperationException;

    boolean copyObject(@NonNull CharSequence bucket, @Nullable CharSequence name, @Nullable MinioCopyConditions conditions) throws MinioOperationException;
}
