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

package co.mercenary.creators.minio.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Optional;

import org.springframework.core.io.AbstractResource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import co.mercenary.creators.minio.MinioOperations;
import co.mercenary.creators.minio.data.MinioItem;
import co.mercenary.creators.minio.errors.MinioOperationException;
import co.mercenary.creators.minio.util.MinioUtils;

public class MinioResource extends AbstractResource
{
    @NonNull
    private final String          bucket;

    @NonNull
    private final String          object;

    @NonNull
    private final MinioOperations miniop;

    public MinioResource(@NonNull final MinioOperations miniops, @NonNull final CharSequence bucket, @NonNull final CharSequence object)
    {
        this.miniop = MinioUtils.requireNonNull(miniops);

        this.bucket = MinioUtils.requireToString(bucket);

        this.object = MinioUtils.requireToString(object);
    }

    @Override
    public boolean isFile()
    {
        return false;
    }

    @Override
    public boolean exists()
    {
        try
        {
            return miniop.isObject(bucket, object);
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
            return new URL(miniop.getSignedObjectUrl(bucket, object));
        }
        catch (final MinioOperationException e)
        {
            throw new IOException(e);
        }
    }

    @NonNull
    @Override
    public String getDescription()
    {
        return MinioUtils.format("MinioResource server=(%s), object=(%s), bucket=(%s)", miniop.getServer(), object, bucket);
    }

    @NonNull
    @Override
    public String getFilename() throws IllegalStateException
    {
        return object;
    }

    @Nullable
    @Override
    public File getFile() throws IOException
    {
        throw new UnsupportedOperationException("MinioResource can not be resolved to java.io.File objects. Use getInputStream() to retrieve the contents of the object!");
    }

    @Override
    public long contentLength() throws IOException
    {
        try
        {
            return miniop.getObjectStatus(bucket, object).getSize();
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
            final Optional<MinioItem> item = miniop.getItem(bucket, object);

            if (item.isPresent())
            {
                final Date date = item.get().getLastModified();

                if (null != date)
                {
                    return date.getTime();
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
            return miniop.getObjectInputStream(bucket, object);
        }
        catch (final MinioOperationException e)
        {
            throw new IOException(e);
        }
    }

    @NonNull
    @Override
    public MinioResource createRelative(@NonNull final String path) throws IOException
    {
        return new MinioResource(miniop, bucket, MinioResourceUtils.getPathRelative(object, path));
    }

    @Override
    public boolean equals(final Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (other instanceof MinioResource)
        {
            return toString().equals(MinioUtils.CAST(other, MinioResource.class).toString());
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return getDescription().hashCode();
    }

    @NonNull
    @Override
    public String toString()
    {
        return getDescription();
    }
}
