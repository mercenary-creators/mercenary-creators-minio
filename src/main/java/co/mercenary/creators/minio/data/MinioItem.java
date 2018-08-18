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
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import co.mercenary.creators.minio.MinioOperations;
import co.mercenary.creators.minio.errors.MinioOperationException;
import co.mercenary.creators.minio.util.MinioUtils;
import co.mercenary.creators.minio.util.WithOperations;
import io.minio.ServerSideEncryption;
import io.minio.http.Method;

public class MinioItem extends MinioCommon implements WithOperations<MinioItemOperations>
{
    private final boolean         file;

    @Nullable
    private final Date            time;

    @NonNull
    private final MinioOperations oper;

    public MinioItem(@NonNull final CharSequence name, @NonNull final CharSequence buck, final long size, final boolean file, @Nullable final CharSequence etag, @NonNull final Supplier<Date> time, @NonNull final MinioOperations oper)
    {
        super(name, buck, etag, size);

        this.oper = MinioUtils.requireNonNull(oper);

        this.time = MinioUtils.toValueNonNull(time);

        this.file = file;
    }

    public boolean isFile()
    {
        return file;
    }

    @Nullable
    public Date getLastModified()
    {
        return MinioUtils.COPY(time);
    }

    @NonNull
    protected MinioOperations operations()
    {
        return oper;
    }

    @NonNull
    @Override
    public MinioItemOperations withOperations()
    {
        return new MinioItemOperations()
        {
            @NonNull
            @Override
            public MinioItem self()
            {
                return MinioItem.this;
            }

            @Override
            public boolean isFile()
            {
                return self().isFile();
            }

            @NonNull
            @Override
            public MinioObjectStatus getObjectStatus() throws MinioOperationException
            {
                return operations().getObjectStatus(getBucket(), getName());
            }

            @NonNull
            @Override
            public MinioObjectStatus getObjectStatus(@NonNull final ServerSideEncryption keys) throws MinioOperationException
            {
                return operations().getObjectStatus(getBucket(), getName(), MinioUtils.requireNonNull(keys));
            }

            @NonNull
            @Override
            public InputStream getObjectInputStream() throws MinioOperationException
            {
                return operations().getObjectInputStream(getBucket(), getName());
            }

            @NonNull
            @Override
            public InputStream getObjectInputStream(final long skip) throws MinioOperationException
            {
                return operations().getObjectInputStream(getBucket(), getName(), skip);
            }

            @NonNull
            @Override
            public InputStream getObjectInputStream(final long skip, final long leng) throws MinioOperationException
            {
                return operations().getObjectInputStream(getBucket(), getName(), skip, leng);
            }

            @NonNull
            @Override
            public InputStream getObjectInputStream(@NonNull final ServerSideEncryption keys) throws MinioOperationException
            {
                return operations().getObjectInputStream(getBucket(), getName(), MinioUtils.requireNonNull(keys));
            }

            @NonNull
            @Override
            public String getSignedObjectUrl() throws MinioOperationException
            {
                return operations().getSignedObjectUrl(getBucket(), getName());
            }

            @NonNull
            @Override
            public String getSignedObjectUrl(@NonNull final Long seconds) throws MinioOperationException
            {
                return operations().getSignedObjectUrl(getBucket(), getName(), MinioUtils.requireNonNull(seconds));
            }

            @NonNull
            @Override
            public String getSignedObjectUrl(@NonNull final Duration seconds) throws MinioOperationException
            {
                return operations().getSignedObjectUrl(getBucket(), getName(), MinioUtils.requireNonNull(seconds));
            }

            @NonNull
            @Override
            public String getSignedObjectUrl(@NonNull final Long time, @NonNull final TimeUnit unit) throws MinioOperationException
            {
                return operations().getSignedObjectUrl(getBucket(), getName(), MinioUtils.requireNonNull(time), MinioUtils.requireNonNull(unit));
            }

            @NonNull
            @Override
            public String getSignedObjectUrl(@NonNull final Method method) throws MinioOperationException
            {
                return operations().getSignedObjectUrl(MinioUtils.requireNonNull(method), getBucket(), getName());
            }

            @NonNull
            @Override
            public String getSignedObjectUrl(@NonNull final Method method, @NonNull final Long seconds) throws MinioOperationException
            {
                return operations().getSignedObjectUrl(MinioUtils.requireNonNull(method), getBucket(), getName(), MinioUtils.requireNonNull(seconds));
            }

            @NonNull
            @Override
            public String getSignedObjectUrl(@NonNull final Method method, @NonNull final Duration seconds) throws MinioOperationException
            {
                return operations().getSignedObjectUrl(MinioUtils.requireNonNull(method), getBucket(), getName(), MinioUtils.requireNonNull(seconds));
            }

            @NonNull
            @Override
            public String getSignedObjectUrl(@NonNull final Method method, @NonNull final Long time, @NonNull final TimeUnit unit) throws MinioOperationException
            {
                return operations().getSignedObjectUrl(MinioUtils.requireNonNull(method), getBucket(), getName(), MinioUtils.requireNonNull(time), MinioUtils.requireNonNull(unit));
            }

            @Override
            public boolean deleteObject() throws MinioOperationException
            {
                return operations().deleteObject(getBucket(), getName());
            }

            @Override
            public boolean copyObject(@NonNull final CharSequence bucket) throws MinioOperationException
            {
                return operations().copyObject(getBucket(), getName(), MinioUtils.requireNonNull(bucket));
            }

            @Override
            public boolean copyObject(@NonNull final CharSequence bucket, @Nullable final CharSequence name) throws MinioOperationException
            {
                return operations().copyObject(getBucket(), getName(), MinioUtils.requireNonNull(bucket), name);
            }

            @Override
            public boolean copyObject(@NonNull final CharSequence bucket, @Nullable final MinioCopyConditions conditions) throws MinioOperationException
            {
                return operations().copyObject(getBucket(), getName(), MinioUtils.requireNonNull(bucket), conditions);
            }

            @Override
            public boolean copyObject(@NonNull final CharSequence bucket, @Nullable final CharSequence name, @Nullable final MinioCopyConditions conditions) throws MinioOperationException
            {
                return operations().copyObject(getBucket(), getName(), MinioUtils.requireNonNull(bucket), name, conditions);
            }
        };
    }
}