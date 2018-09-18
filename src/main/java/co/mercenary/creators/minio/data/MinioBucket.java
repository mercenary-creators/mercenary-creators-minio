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
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import co.mercenary.creators.minio.MinioOperations;
import co.mercenary.creators.minio.errors.MinioDataException;
import co.mercenary.creators.minio.errors.MinioOperationException;
import co.mercenary.creators.minio.util.AbstractCommon;
import co.mercenary.creators.minio.util.MinioUtils;
import co.mercenary.creators.minio.util.WithOperations;
import io.minio.ServerSideEncryption;
import io.minio.http.Method;

public class MinioBucket extends AbstractCommon implements WithOperations<MinioBucketOperations>
{
    @Nullable
    private final Date                  time;

    @NonNull
    private final MinioBucketOperations oper;

    public MinioBucket(@NonNull final CharSequence name, @NonNull final Supplier<Date> time, @NonNull final MinioOperations oper)
    {
        super(name);

        this.time = MinioUtils.toValueNonNull(time);

        this.oper = buildWithOperations(this, oper);
    }

    @NonNull
    @Override
    public MinioBucketOperations withOperations()
    {
        return oper;
    }

    @Nullable
    public Date getCreationTime()
    {
        return MinioUtils.COPY(time);
    }

    @NonNull
    @Override
    public String toDescription()
    {
        return MinioUtils.format("name=(%s), creationTime=(%s).", getName(), MinioUtils.format(time));
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
        if (other instanceof MinioBucket)
        {
            return toString().equals(other.toString());
        }
        return false;
    }

    @NonNull
    protected static MinioBucketOperations buildWithOperations(@NonNull final MinioBucket self, @NonNull final MinioOperations oper)
    {
        MinioUtils.testAllNonNull(self, oper);

        return new MinioBucketOperations()
        {
            @NonNull
            @Override
            public MinioBucket self()
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
            public boolean deleteBucket() throws MinioOperationException
            {
                return oper.deleteBucket(self().getName());
            }

            @Override
            public boolean deleteObject(@NonNull final CharSequence name) throws MinioOperationException
            {
                return oper.deleteObject(self().getName(), MinioUtils.requireNonNull(name));
            }

            @NonNull
            @Override
            public String getBucketPolicy() throws MinioOperationException
            {
                return oper.getBucketPolicy(self().getName());
            }

            @NonNull
            @Override
            public <T> T getBucketPolicy(@NonNull final Class<T> type) throws MinioOperationException, MinioDataException
            {
                return oper.getBucketPolicy(self().getName(), MinioUtils.requireNonNull(type));
            }

            @Override
            public void setBucketPolicy(@NonNull final Object policy) throws MinioOperationException, MinioDataException
            {
                oper.setBucketPolicy(self().getName(), MinioUtils.requireNonNull(policy));
            }

            @NonNull
            @Override
            public MinioObjectStatus getObjectStatus(@NonNull final CharSequence name) throws MinioOperationException
            {
                return oper.getObjectStatus(self().getName(), MinioUtils.requireNonNull(name));
            }

            @NonNull
            @Override
            public MinioObjectStatus getObjectStatus(@NonNull final CharSequence name, @NonNull final ServerSideEncryption keys) throws MinioOperationException
            {
                return oper.getObjectStatus(self().getName(), MinioUtils.requireNonNull(name), MinioUtils.requireNonNull(keys));
            }

            @NonNull
            @Override
            public InputStream getObjectInputStream(@NonNull final CharSequence name) throws MinioOperationException
            {
                return oper.getObjectInputStream(self().getName(), MinioUtils.requireNonNull(name));
            }

            @NonNull
            @Override
            public InputStream getObjectInputStream(@NonNull final CharSequence name, final long skip) throws MinioOperationException
            {
                return oper.getObjectInputStream(self().getName(), MinioUtils.requireNonNull(name), skip);
            }

            @NonNull
            @Override
            public InputStream getObjectInputStream(@NonNull final CharSequence name, final long skip, final long leng) throws MinioOperationException
            {
                return oper.getObjectInputStream(self().getName(), MinioUtils.requireNonNull(name), skip, leng);
            }

            @NonNull
            @Override
            public InputStream getObjectInputStream(@NonNull final CharSequence name, @NonNull final ServerSideEncryption keys) throws MinioOperationException
            {
                return oper.getObjectInputStream(self().getName(), MinioUtils.requireNonNull(name), MinioUtils.requireNonNull(keys));
            }

            @Override
            public void putObject(@NonNull final CharSequence name, @NonNull final InputStream input, @Nullable final CharSequence type) throws MinioOperationException
            {
                oper.putObject(self().getName(), MinioUtils.requireNonNull(name), MinioUtils.requireNonNull(input), type);
            }

            @Override
            public void putObject(@NonNull final CharSequence name, @NonNull final InputStream input, final long size, @Nullable final CharSequence type) throws MinioOperationException
            {
                oper.putObject(self().getName(), MinioUtils.requireNonNull(name), MinioUtils.requireNonNull(input), size, type);
            }

            @Override
            public void putObject(@NonNull final CharSequence name, @NonNull final byte[] input, @Nullable final CharSequence type) throws MinioOperationException
            {
                oper.putObject(self().getName(), MinioUtils.requireNonNull(name), MinioUtils.requireNonNull(input), type);
            }

            @Override
            public void putObject(@NonNull final CharSequence name, @NonNull final Resource input, @Nullable final CharSequence type) throws MinioOperationException
            {
                oper.putObject(self().getName(), MinioUtils.requireNonNull(name), MinioUtils.requireNonNull(input), type);
            }

            @Override
            public void putObject(@NonNull final CharSequence name, @NonNull final File input, @Nullable final CharSequence type) throws MinioOperationException
            {
                oper.putObject(self().getName(), MinioUtils.requireNonNull(name), MinioUtils.requireNonNull(input), type);
            }

            @Override
            public void putObject(@NonNull final CharSequence name, @NonNull final File input, final long size, @Nullable final CharSequence type) throws MinioOperationException
            {
                oper.putObject(self().getName(), MinioUtils.requireNonNull(name), MinioUtils.requireNonNull(input), size, type);
            }

            @Override
            public void putObject(@NonNull final CharSequence name, @NonNull final Path input, @Nullable final CharSequence type) throws MinioOperationException
            {
                oper.putObject(self().getName(), MinioUtils.requireNonNull(name), MinioUtils.requireNonNull(input), type);
            }

            @Override
            public void putObject(@NonNull final CharSequence name, @NonNull final Path input, final long size, @Nullable final CharSequence type) throws MinioOperationException
            {
                oper.putObject(self().getName(), MinioUtils.requireNonNull(name), MinioUtils.requireNonNull(input), size, type);
            }

            @NonNull
            @Override
            public Stream<MinioItem> getItems(@Nullable final CharSequence prefix, final boolean recursive) throws MinioOperationException
            {
                return oper.getItems(self().getName(), prefix, recursive);
            }

            @NonNull
            @Override
            public String getSignedObjectUrl(@NonNull final Method method, @NonNull final CharSequence name) throws MinioOperationException
            {
                return oper.getSignedObjectUrl(MinioUtils.requireNonNull(method), self().getName(), MinioUtils.requireNonNull(name));
            }

            @NonNull
            @Override
            public String getSignedObjectUrl(@NonNull final Method method, @NonNull final CharSequence name, @NonNull final Long seconds) throws MinioOperationException
            {
                return oper.getSignedObjectUrl(MinioUtils.requireNonNull(method), self().getName(), MinioUtils.requireNonNull(name), MinioUtils.requireNonNull(seconds));
            }

            @NonNull
            @Override
            public String getSignedObjectUrl(@NonNull final Method method, @NonNull final CharSequence name, @NonNull final Duration seconds) throws MinioOperationException
            {
                return oper.getSignedObjectUrl(MinioUtils.requireNonNull(method), self().getName(), MinioUtils.requireNonNull(name), MinioUtils.requireNonNull(seconds));
            }

            @NonNull
            @Override
            public String getSignedObjectUrl(@NonNull final Method method, @NonNull final CharSequence name, @NonNull final Long time, @NonNull final TimeUnit unit) throws MinioOperationException
            {
                return oper.getSignedObjectUrl(MinioUtils.requireNonNull(method), self().getName(), MinioUtils.requireNonNull(name), MinioUtils.requireNonNull(time), MinioUtils.requireNonNull(unit));
            }

            @Override
            public boolean copyObject(@NonNull final CharSequence name, @NonNull final CharSequence target, @Nullable final MinioCopyConditions conditions) throws MinioOperationException
            {
                return oper.copyObject(self().getName(), MinioUtils.requireNonNull(name), MinioUtils.requireNonNull(target), conditions);
            }

            @Override
            public boolean copyObject(@NonNull final CharSequence name, @NonNull final CharSequence target, @Nullable final CharSequence object, @Nullable final MinioCopyConditions conditions) throws MinioOperationException
            {
                return oper.copyObject(self().getName(), MinioUtils.requireNonNull(name), MinioUtils.requireNonNull(target), object, conditions);
            }

            @Override
            public boolean isObject(@NonNull final CharSequence name) throws MinioOperationException
            {
                return oper.isObject(self().getName(), MinioUtils.requireNonNull(name));
            }

            @NonNull
            @Override
            public Stream<MinioUpload> getIncompleteUploads(@Nullable final CharSequence prefix, final boolean recursive) throws MinioOperationException
            {
                return oper.getIncompleteUploads(self().getName(), prefix, recursive);
            }
        };
    }
}
