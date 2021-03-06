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
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

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
    @NonNull
    private final Optional<Date>        time;

    @NonNull
    private final MinioBucketOperations oper;

    public MinioBucket(@NonNull final String name, @NonNull final Supplier<Date> time, @NonNull final MinioOperations oper)
    {
        super(name);

        this.time = MinioUtils.toMaybeNonNull(time);

        this.oper = buildWithOperations(this, oper);
    }

    @NonNull
    @Override
    @JsonIgnore
    public MinioBucketOperations withOperations()
    {
        return oper;
    }

    @NonNull
    @JsonInclude(Include.NON_ABSENT)
    public Optional<Date> getCreationTime()
    {
        return time.map(date -> new Date(date.getTime()));
    }

    @NonNull
    @Override
    @JsonIgnore
    public String toDescription()
    {
        return String.format("name=(%s), creationTime=(%s).", getName(), toDateString(time));
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
    private static MinioBucketOperations buildWithOperations(@NonNull final MinioBucket self, @NonNull final MinioOperations oper)
    {
        MinioUtils.isEachNonNull(self, oper);

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
            public boolean deleteObject(@NonNull final String name) throws MinioOperationException
            {
                return oper.deleteObject(self().getName(), name);
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
                return oper.getBucketPolicy(self().getName(), type);
            }

            @Override
            public void setBucketPolicy(@NonNull final Object policy) throws MinioOperationException, MinioDataException
            {
                oper.setBucketPolicy(self().getName(), policy);
            }

            @NonNull
            @Override
            public MinioUserMetaData getUserMetaData(@NonNull final String name) throws MinioOperationException
            {
                return oper.getUserMetaData(self().getName(), name);
            }

            @NonNull
            @Override
            public MinioObjectStatus getObjectStatus(@NonNull final String name) throws MinioOperationException
            {
                return oper.getObjectStatus(self().getName(), name);
            }

            @NonNull
            @Override
            public MinioObjectStatus getObjectStatus(@NonNull final String name, @NonNull final ServerSideEncryption keys) throws MinioOperationException
            {
                return oper.getObjectStatus(self().getName(), name, keys);
            }

            @NonNull
            @Override
            public InputStream getObjectInputStream(@NonNull final String name) throws MinioOperationException
            {
                return oper.getObjectInputStream(self().getName(), name);
            }

            @NonNull
            @Override
            public InputStream getObjectInputStream(@NonNull final String name, final long skip) throws MinioOperationException
            {
                return oper.getObjectInputStream(self().getName(), name, skip);
            }

            @NonNull
            @Override
            public InputStream getObjectInputStream(@NonNull final String name, final long skip, final long leng) throws MinioOperationException
            {
                return oper.getObjectInputStream(self().getName(), name, skip, leng);
            }

            @NonNull
            @Override
            public InputStream getObjectInputStream(@NonNull final String name, @NonNull final ServerSideEncryption keys) throws MinioOperationException
            {
                return oper.getObjectInputStream(self().getName(), name, keys);
            }

            @Override
            public void putObject(@NonNull final String name, @NonNull final InputStream input, @Nullable final String type) throws MinioOperationException
            {
                oper.putObject(self().getName(), name, input, type);
            }

            @Override
            public void putObject(@NonNull final String name, @NonNull final byte[] input, @Nullable final String type) throws MinioOperationException
            {
                oper.putObject(self().getName(), name, input, type);
            }

            @Override
            public void putObject(@NonNull final String name, @NonNull final Resource input, @Nullable final String type) throws MinioOperationException
            {
                oper.putObject(self().getName(), name, input, type);
            }

            @Override
            public void putObject(@NonNull final String name, @NonNull final File input, @Nullable final String type) throws MinioOperationException
            {
                oper.putObject(self().getName(), name, input, type);
            }

            @Override
            public void putObject(@NonNull final String name, @NonNull final Path input, @Nullable final String type) throws MinioOperationException
            {
                oper.putObject(self().getName(), name, input, type);
            }

            @NonNull
            @Override
            public Stream<MinioItem> findItems(@Nullable final String prefix, final boolean recursive) throws MinioOperationException
            {
                return oper.findItems(self().getName(), prefix, recursive);
            }

            @NonNull
            @Override
            public String getSignedObjectUrl(@NonNull final Method method, @NonNull final String name) throws MinioOperationException
            {
                return oper.getSignedObjectUrl(method, self().getName(), name);
            }

            @NonNull
            @Override
            public String getSignedObjectUrl(@NonNull final Method method, @NonNull final String name, @NonNull final Long seconds) throws MinioOperationException
            {
                return oper.getSignedObjectUrl(method, self().getName(), name, seconds);
            }

            @NonNull
            @Override
            public String getSignedObjectUrl(@NonNull final Method method, @NonNull final String name, @NonNull final Duration seconds) throws MinioOperationException
            {
                return oper.getSignedObjectUrl(method, self().getName(), name, seconds);
            }

            @NonNull
            @Override
            public String getSignedObjectUrl(@NonNull final Method method, @NonNull final String name, @NonNull final Long time, @NonNull final TimeUnit unit) throws MinioOperationException
            {
                return oper.getSignedObjectUrl(method, self().getName(), name, time, unit);
            }

            @Override
            public boolean copyObject(@NonNull final String name, @NonNull final String target, @Nullable final MinioCopyConditions conditions) throws MinioOperationException
            {
                return oper.copyObject(self().getName(), name, target, conditions);
            }

            @Override
            public boolean copyObject(@NonNull final String name, @NonNull final String target, @Nullable final String object, @Nullable final MinioCopyConditions conditions) throws MinioOperationException
            {
                return oper.copyObject(self().getName(), name, target, object, conditions);
            }

            @Override
            public boolean isObject(@NonNull final String name) throws MinioOperationException
            {
                return oper.isObject(self().getName(), name);
            }

            @NonNull
            @Override
            public Stream<MinioUpload> getIncompleteUploads(@Nullable final String prefix, final boolean recursive) throws MinioOperationException
            {
                return oper.getIncompleteUploads(self().getName(), prefix, recursive);
            }

            @Override
            public void deleteUserMetaData(@NonNull final String name) throws MinioOperationException
            {
                oper.deleteUserMetaData(self().getName(), name);
            }

            @Override
            public void setUserMetaData(@NonNull final String name, @Nullable final MinioUserMetaData meta) throws MinioOperationException
            {
                oper.setUserMetaData(self().getName(), name, meta);
            }

            @Override
            public void addUserMetaData(@NonNull final String name, @Nullable final MinioUserMetaData meta) throws MinioOperationException
            {
                oper.addUserMetaData(self().getName(), name, meta);
            }
        };
    }
}
