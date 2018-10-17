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

import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import co.mercenary.creators.minio.MinioOperations;
import co.mercenary.creators.minio.errors.MinioOperationException;
import co.mercenary.creators.minio.io.resource.MinioItemResource;
import co.mercenary.creators.minio.util.MinioUtils;
import co.mercenary.creators.minio.util.WithOperations;
import io.minio.ServerSideEncryption;
import io.minio.http.Method;

public class MinioItem extends MinioCommon implements WithOperations<MinioItemOperations>
{
    private final boolean             file;

    @Nullable
    private final String              stor;

    @NonNull
    private final String              type;

    @NonNull
    private final Optional<Date>      time;

    @NonNull
    private final MinioItemOperations oper;

    public MinioItem(@NonNull final String name, @NonNull final String buck, final long size, final boolean file, @Nullable final String etag, @Nullable final String type, @NonNull final Supplier<Date> time, @Nullable final String stor, @NonNull final MinioOperations oper)
    {
        super(name, buck, etag, size);

        this.file = file;

        this.type = MinioUtils.fixContentType(type);

        this.time = MinioUtils.toMaybeNonNull(time);

        this.stor = MinioUtils.toStorageClass(stor);

        this.oper = buildWithOperations(this, oper);
    }

    @NonNull
    @Override
    @JsonIgnore
    public MinioItemOperations withOperations()
    {
        return oper;
    }

    public boolean isFile()
    {
        return file;
    }

    @NonNull
    public String getContentType()
    {
        return type;
    }

    @NonNull
    @JsonInclude(Include.NON_ABSENT)
    public Optional<Date> getLastModified()
    {
        return time.map(date -> new Date(date.getTime()));
    }

    @Nullable
    @JsonInclude(Include.NON_NULL)
    public String getStorageClass()
    {
        return stor;
    }

    @NonNull
    @Override
    @JsonIgnore
    public String toDescription()
    {
        return MinioUtils.format("name=(%s), bucket=(%s), etag=(%s), size=(%s), file=(%s), contentType=(%s), lastModified=(%s).", getName(), getBucket(), getEtag(), getSize(), isFile(), getContentType(), MinioUtils.format(time));
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
        MinioUtils.isEachNonNull(self, oper);

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
            public Resource getItemResource() throws MinioOperationException
            {
                return new MinioItemResource(self());
            }

            @NonNull
            @Override
            public MinioUserMetaData getUserMetaData() throws MinioOperationException
            {
                return oper.getUserMetaData(self().getBucket(), self().getName());
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
                return oper.getObjectStatus(self().getBucket(), self().getName(), keys);
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
                return oper.getObjectInputStream(self().getBucket(), self().getName(), keys);
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
                return oper.getSignedObjectUrl(self().getBucket(), self().getName(), seconds);
            }

            @NonNull
            @Override
            public String getSignedObjectUrl(@NonNull final Duration seconds) throws MinioOperationException
            {
                return oper.getSignedObjectUrl(self().getBucket(), self().getName(), seconds);
            }

            @NonNull
            @Override
            public String getSignedObjectUrl(@NonNull final Long time, @NonNull final TimeUnit unit) throws MinioOperationException
            {
                return oper.getSignedObjectUrl(self().getBucket(), self().getName(), time, unit);
            }

            @NonNull
            @Override
            public String getSignedObjectUrl(@NonNull final Method method) throws MinioOperationException
            {
                return oper.getSignedObjectUrl(method, self().getBucket(), self().getName());
            }

            @NonNull
            @Override
            public String getSignedObjectUrl(@NonNull final Method method, @NonNull final Long seconds) throws MinioOperationException
            {
                return oper.getSignedObjectUrl(method, self().getBucket(), self().getName(), seconds);
            }

            @NonNull
            @Override
            public String getSignedObjectUrl(@NonNull final Method method, @NonNull final Duration seconds) throws MinioOperationException
            {
                return oper.getSignedObjectUrl(method, self().getBucket(), self().getName(), seconds);
            }

            @NonNull
            @Override
            public String getSignedObjectUrl(@NonNull final Method method, @NonNull final Long time, @NonNull final TimeUnit unit) throws MinioOperationException
            {
                return oper.getSignedObjectUrl(method, self().getBucket(), self().getName(), time, unit);
            }

            @Override
            public boolean deleteObject() throws MinioOperationException
            {
                return oper.deleteObject(self().getBucket(), self().getName());
            }

            @Override
            public boolean copyObject(@NonNull final String bucket) throws MinioOperationException
            {
                return oper.copyObject(self().getBucket(), self().getName(), bucket);
            }

            @Override
            public boolean copyObject(@NonNull final String bucket, @Nullable final String name) throws MinioOperationException
            {
                return oper.copyObject(self().getBucket(), self().getName(), bucket, name);
            }

            @Override
            public boolean copyObject(@NonNull final String bucket, @Nullable final MinioCopyConditions conditions) throws MinioOperationException
            {
                return oper.copyObject(self().getBucket(), self().getName(), bucket, conditions);
            }

            @Override
            public boolean copyObject(@NonNull final String bucket, @Nullable final String name, @Nullable final MinioCopyConditions conditions) throws MinioOperationException
            {
                return oper.copyObject(self().getBucket(), self().getName(), bucket, name, conditions);
            }

            @NonNull
            @Override
            public Optional<MinioItem> getItemRelative(@NonNull final String path) throws MinioOperationException
            {
                return oper.getItem(self().getBucket(), MinioUtils.getPathRelative(self().getName(), path));
            }

            @Override
            public boolean setUserMetaData(@Nullable final MinioUserMetaData meta) throws MinioOperationException
            {
                return oper.setUserMetaData(self().getBucket(), self().getName(), meta);
            }

            @Override
            public boolean addUserMetaData(@Nullable final MinioUserMetaData meta) throws MinioOperationException
            {
                return oper.addUserMetaData(self().getBucket(), self().getName(), meta);
            }
        };
    }
}