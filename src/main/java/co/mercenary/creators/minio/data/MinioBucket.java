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
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.crypto.SecretKey;

import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import co.mercenary.creators.minio.MinioMethod;
import co.mercenary.creators.minio.MinioOperationException;
import co.mercenary.creators.minio.MinioOperations;
import co.mercenary.creators.minio.MinioUtils;

public class MinioBucket extends MinioNamed implements IWithOperations<IMinioBucketOperations>
{
    @Nullable
    private final Date            m_time;

    @NonNull
    private final MinioOperations m_oper;

    public MinioBucket(@NonNull final CharSequence name, @NonNull final Supplier<Date> time, @NonNull final MinioOperations oper)
    {
        super(name);

        m_oper = MinioUtils.requireNonNull(oper);

        m_time = MinioUtils.toValueNonNull(time);
    }

    @Nullable
    public Date getCreationTime()
    {
        return m_time;
    }

    @NonNull
    protected MinioOperations operations()
    {
        return m_oper;
    }

    @NonNull
    @Override
    public IMinioBucketOperations withOperations()
    {
        return new IMinioBucketOperations()
        {
            @Override
            public boolean deleteBucket() throws MinioOperationException
            {
                return operations().deleteBucket(getName());
            }

            @Override
            public boolean deleteObject(@NonNull final CharSequence name) throws MinioOperationException
            {
                return operations().deleteObject(getName(), name);
            }

            @NonNull
            @Override
            public String getBucketPolicy() throws MinioOperationException
            {
                return operations().getBucketPolicy(getName());
            }

            @Override
            public void setBucketPolicy(@NonNull final CharSequence policy) throws MinioOperationException
            {
                operations().setBucketPolicy(getName(), policy);
            }

            @NonNull
            @Override
            public MinioObjectStatus getObjectStatus(@NonNull final CharSequence name) throws MinioOperationException
            {
                return operations().getObjectStatus(getName(), name);
            }

            @NonNull
            @Override
            public InputStream getObjectInputStream(@NonNull final CharSequence name) throws MinioOperationException
            {
                return operations().getObjectInputStream(getName(), name);
            }

            @NonNull
            @Override
            public InputStream getObjectInputStream(@NonNull final CharSequence name, final long skip) throws MinioOperationException
            {
                return operations().getObjectInputStream(getName(), name, skip);
            }

            @NonNull
            @Override
            public InputStream getObjectInputStream(@NonNull final CharSequence name, final long skip, final long leng) throws MinioOperationException
            {
                return operations().getObjectInputStream(getName(), name, skip, leng);
            }

            @NonNull
            @Override
            public InputStream getObjectInputStream(@NonNull final CharSequence name, @NonNull final KeyPair keys) throws MinioOperationException
            {
                return operations().getObjectInputStream(getName(), name, keys);
            }

            @NonNull
            @Override
            public InputStream getObjectInputStream(@NonNull final CharSequence name, @NonNull final SecretKey keys) throws MinioOperationException
            {
                return operations().getObjectInputStream(getName(), name, keys);
            }

            @Override
            public void putObject(@NonNull final CharSequence name, @NonNull final InputStream input, @Nullable final CharSequence type) throws MinioOperationException
            {
                operations().putObject(getName(), name, input, type);
            }

            @Override
            public void putObject(@NonNull final CharSequence name, @NonNull final InputStream input, final long size, @Nullable final CharSequence type) throws MinioOperationException
            {
                operations().putObject(getName(), name, input, size, type);
            }

            @Override
            public void putObject(@NonNull final CharSequence name, @NonNull final byte[] input, @Nullable final CharSequence type) throws MinioOperationException
            {
                operations().putObject(getName(), name, input, type);
            }

            @Override
            public void putObject(@NonNull final CharSequence name, @NonNull final Resource resource, @Nullable final CharSequence type) throws MinioOperationException
            {
                operations().putObject(getName(), name, resource, type);
            }

            @Override
            public void putObject(@NonNull final CharSequence name, @NonNull final File file, @Nullable final CharSequence type) throws MinioOperationException
            {
                operations().putObject(getName(), name, file, type);
            }

            @Override
            public void putObject(@NonNull final CharSequence name, @NonNull final File file, final long size, @Nullable final CharSequence type) throws MinioOperationException
            {
                operations().putObject(getName(), name, file, size, type);
            }

            @Override
            public void putObject(@NonNull final CharSequence name, @NonNull final Path path, @Nullable final CharSequence type) throws MinioOperationException
            {
                operations().putObject(getName(), name, path, type);
            }

            @Override
            public void putObject(@NonNull final CharSequence name, @NonNull final Path path, final long size, @Nullable final CharSequence type) throws MinioOperationException
            {
                operations().putObject(getName(), name, path, size, type);
            }

            @NonNull
            @Override
            public Stream<MinioItem> getItems(@Nullable final CharSequence prefix, final boolean recursive) throws MinioOperationException
            {
                return operations().getItems(getName(), prefix, recursive);
            }

            @NonNull
            @Override
            public String getSignedObjectUrl(@NonNull final MinioMethod method, @NonNull final CharSequence name) throws MinioOperationException
            {
                return operations().getSignedObjectUrl(method, getName(), name);
            }

            @NonNull
            @Override
            public String getSignedObjectUrl(@NonNull final MinioMethod method, @NonNull final CharSequence name, final long seconds) throws MinioOperationException
            {
                return operations().getSignedObjectUrl(method, getName(), name, seconds);
            }

            @NonNull
            @Override
            public String getSignedObjectUrl(@NonNull final MinioMethod method, @NonNull final CharSequence name, @NonNull final Duration seconds) throws MinioOperationException
            {
                return operations().getSignedObjectUrl(method, getName(), name, seconds);
            }

            @NonNull
            @Override
            public String getSignedObjectUrl(@NonNull final MinioMethod method, @NonNull final CharSequence name, final long time, @NonNull final TimeUnit unit) throws MinioOperationException
            {
                return operations().getSignedObjectUrl(method, getName(), name, time, unit);
            }

            @Override
            public boolean copyObject(@NonNull final CharSequence name, @NonNull final CharSequence destbucket, @Nullable final MinioCopyConditions conditions) throws MinioOperationException
            {
                return operations().copyObject(getName(), name, destbucket, conditions);
            }

            @Override
            public boolean copyObject(@NonNull final CharSequence name, @NonNull final CharSequence destbucket, @Nullable final CharSequence destobject, @Nullable final MinioCopyConditions conditions) throws MinioOperationException
            {
                return operations().copyObject(getName(), name, destbucket, destobject, conditions);
            }

            @Override
            public boolean isObject(@NonNull final CharSequence name) throws MinioOperationException
            {
                return operations().isObject(getName(), name);
            }
        };
    }
}
