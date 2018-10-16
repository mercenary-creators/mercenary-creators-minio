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

package co.mercenary.creators.minio.io.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import co.mercenary.creators.minio.MinioOperations;
import co.mercenary.creators.minio.data.MinioItem;
import co.mercenary.creators.minio.errors.MinioOperationException;
import co.mercenary.creators.minio.util.MinioUtils;

@JsonIgnoreType
public class MinioResource extends AbstractMinioResource<MinioOperations>
{
    @NonNull
    private final String bucket;

    @NonNull
    private final String object;

    public MinioResource(@NonNull final MinioOperations minops, @NonNull final String bucket, @NonNull final String object)
    {
        super(minops, "server=(%s), object=(%s), bucket=(%s).", minops.getServer(), object, bucket);

        this.bucket = MinioUtils.requireNonNull(bucket);

        this.object = MinioUtils.requireNonNull(object);
    }

    @Override
    public boolean exists()
    {
        try
        {
            return self().isObject(bucket, object);
        }
        catch (final MinioOperationException e)
        {
            return false;
        }
    }

    @NonNull
    @Override
    public URL getURL() throws IOException
    {
        try
        {
            return new URL(self().getSignedObjectUrl(bucket, object));
        }
        catch (final MinioOperationException e)
        {
            throw new IOException(e);
        }
    }

    @Override
    public long contentLength() throws IOException
    {
        try
        {
            return self().getObjectStatus(bucket, object).getSize();
        }
        catch (final MinioOperationException e)
        {
            throw new IOException(e);
        }
    }

    @Override
    public long lastModified() throws IOException
    {
        try
        {
            final Optional<MinioItem> item = self().getItem(bucket, object);

            if (item.isPresent())
            {
                final Optional<Date> date = item.get().getLastModified();

                if (date.isPresent())
                {
                    return date.get().getTime();
                }
            }
            throw new IOException(getDescription() + " not found.");
        }
        catch (final MinioOperationException e)
        {
            throw new IOException(e);
        }
    }

    @NonNull
    @Override
    public InputStream getInputStream() throws IOException
    {
        try
        {
            return self().getObjectInputStream(bucket, object);
        }
        catch (final MinioOperationException e)
        {
            throw new IOException(e);
        }
    }

    @NonNull
    @Override
    public Resource createRelative(@NonNull final String path) throws IOException
    {
        return new MinioResource(self(), bucket, MinioUtils.getPathRelative(object, path));
    }

    @Override
    public boolean equals(final Object other)
    {
        return super.equals(other);
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    @NonNull
    @Override
    public String toString()
    {
        return super.toString();
    }
}
