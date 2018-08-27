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
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import co.mercenary.creators.minio.MinioOperations;
import co.mercenary.creators.minio.errors.MinioOperationException;
import co.mercenary.creators.minio.resource.MinioResourceUtils;
import co.mercenary.creators.minio.util.MinioUtils;
import co.mercenary.creators.minio.util.WithOperations;
import io.minio.ServerSideEncryption;
import io.minio.http.Method;

public class MinioItem extends MinioCommon implements WithOperations<MinioItemOperations>
{
    private final boolean             file;

    @Nullable
    private final Date                time;

    @NonNull
    private final MinioItemOperations oper;

    public MinioItem(@NonNull final CharSequence name, @NonNull final CharSequence buck, final long size, final boolean file, @Nullable final CharSequence etag, @NonNull final Supplier<Date> time, @NonNull final MinioOperations oper)
    {
        super(name, buck, etag, size);

        this.file = file;

        this.time = MinioUtils.toValueNonNull(time);

        this.oper = buildWithOperations(this, oper);
    }

    @NonNull
    @Override
    public MinioItemOperations withOperations()
    {
        return oper;
    }

    public boolean isFile()
    {
        return file;
    }

    @NonNull
    @Override
    public String getName()
    {
        return MinioResourceUtils.fixPathString(super.getName());
    }

    @Nullable
    public Date getLastModified()
    {
        return MinioUtils.COPY(time);
    }

    @NonNull
    @Override
    public String toDescription()
    {
        return MinioUtils.format("class=(%s), name=(%s), bucket=(%s), etag=(%s), size=(%s), file=(%s), lastModified=(%s).", getClass().getCanonicalName(), getName(), getBucket(), getEtag(), getSize(), isFile(), MinioUtils.format(time));
    }

    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }

    @Override
    public boolean equals(final Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (other instanceof MinioItem)
        {
            return toString().equals(other.toString());
        }
        return false;
    }

    @NonNull
    protected static MinioItemOperations buildWithOperations(@NonNull final MinioItem self, @NonNull final MinioOperations oper)
    {
        MinioUtils.testAllNonNull(self, oper);

        return new MinioItemOperations()
        {
            @NonNull
            @Override
            public MinioItem self()
            {
                return self;
            }

            @NonNull
            @Override
            public String getServer()
            {
                return oper.getServer();
            }

            @NonNull
            @Override
            public String getRegion()
            {
                return oper.getRegion();
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
                return oper.getObjectStatus(self().getBucket(), self().getName());
            }

            @NonNull
            @Override
            public MinioObjectStatus getObjectStatus(@NonNull final ServerSideEncryption keys) throws MinioOperationException
            {
                return oper.getObjectStatus(self().getBucket(), self().getName(), MinioUtils.requireNonNull(keys));
            }

            @NonNull
            @Override
            public InputStream getObjectInputStream() throws MinioOperationException
            {
                return oper.getObjectInputStream(self().getBucket(), self().getName());
            }

            @NonNull
            @Override
            public InputStream getObjectInputStream(final long skip) throws MinioOperationException
            {
                return oper.getObjectInputStream(self().getBucket(), self().getName(), skip);
            }

            @NonNull
            @Override
            public InputStream getObjectInputStream(final long skip, final long leng) throws MinioOperationException
            {
                return oper.getObjectInputStream(self().getBucket(), self().getName(), skip, leng);
            }

            @NonNull
            @Override
            public InputStream getObjectInputStream(@NonNull final ServerSideEncryption keys) throws MinioOperationException
            {
                return oper.getObjectInputStream(self().getBucket(), self().getName(), MinioUtils.requireNonNull(keys));
            }

            @NonNull
            @Override
            public String getSignedObjectUrl() throws MinioOperationException
            {
                return oper.getSignedObjectUrl(self().getBucket(), self().getName());
            }

            @NonNull
            @Override
            public String getSignedObjectUrl(@NonNull final Long seconds) throws MinioOperationException
            {
                return oper.getSignedObjectUrl(self().getBucket(), self().getName(), MinioUtils.requireNonNull(seconds));
            }

            @NonNull
            @Override
            public String getSignedObjectUrl(@NonNull final Duration seconds) throws MinioOperationException
            {
                return oper.getSignedObjectUrl(self().getBucket(), self().getName(), MinioUtils.requireNonNull(seconds));
            }

            @NonNull
            @Override
            public String getSignedObjectUrl(@NonNull final Long time, @NonNull final TimeUnit unit) throws MinioOperationException
            {
                return oper.getSignedObjectUrl(self().getBucket(), self().getName(), MinioUtils.requireNonNull(time), MinioUtils.requireNonNull(unit));
            }

            @NonNull
            @Override
            public String getSignedObjectUrl(@NonNull final Method method) throws MinioOperationException
            {
                return oper.getSignedObjectUrl(MinioUtils.requireNonNull(method), self().getBucket(), self().getName());
            }

            @NonNull
            @Override
            public String getSignedObjectUrl(@NonNull final Method method, @NonNull final Long seconds) throws MinioOperationException
            {
                return oper.getSignedObjectUrl(MinioUtils.requireNonNull(method), self().getBucket(), self().getName(), MinioUtils.requireNonNull(seconds));
            }

            @NonNull
            @Override
            public String getSignedObjectUrl(@NonNull final Method method, @NonNull final Duration seconds) throws MinioOperationException
            {
                return oper.getSignedObjectUrl(MinioUtils.requireNonNull(method), self().getBucket(), self().getName(), MinioUtils.requireNonNull(seconds));
            }

            @NonNull
            @Override
            public String getSignedObjectUrl(@NonNull final Method method, @NonNull final Long time, @NonNull final TimeUnit unit) throws MinioOperationException
            {
                return oper.getSignedObjectUrl(MinioUtils.requireNonNull(method), self().getBucket(), self().getName(), MinioUtils.requireNonNull(time), MinioUtils.requireNonNull(unit));
            }

            @Override
            public boolean deleteObject() throws MinioOperationException
            {
                return oper.deleteObject(self().getBucket(), self().getName());
            }

            @Override
            public boolean copyObject(@NonNull final CharSequence bucket) throws MinioOperationException
            {
                return oper.copyObject(self().getBucket(), self().getName(), MinioUtils.requireNonNull(bucket));
            }

            @Override
            public boolean copyObject(@NonNull final CharSequence bucket, @Nullable final CharSequence name) throws MinioOperationException
            {
                return oper.copyObject(self().getBucket(), self().getName(), MinioUtils.requireNonNull(bucket), name);
            }

            @Override
            public boolean copyObject(@NonNull final CharSequence bucket, @Nullable final MinioCopyConditions conditions) throws MinioOperationException
            {
                return oper.copyObject(self().getBucket(), self().getName(), MinioUtils.requireNonNull(bucket), conditions);
            }

            @Override
            public boolean copyObject(@NonNull final CharSequence bucket, @Nullable final CharSequence name, @Nullable final MinioCopyConditions conditions) throws MinioOperationException
            {
                return oper.copyObject(self().getBucket(), self().getName(), MinioUtils.requireNonNull(bucket), name, conditions);
            }

            @NonNull
            @Override
            public Optional<MinioItem> getItemRelative(@NonNull final CharSequence path) throws MinioOperationException
            {
                return oper.getItem(self().getBucket(), MinioResourceUtils.getPathRelative(self().getName(), path));
            }
        };
    }
}