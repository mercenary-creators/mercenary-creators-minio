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

import org.springframework.core.io.AbstractResource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import co.mercenary.creators.minio.data.MinioItem;
import co.mercenary.creators.minio.errors.MinioOperationException;
import co.mercenary.creators.minio.util.MinioUtils;

public class MinioItemResource extends AbstractResource
{
    @NonNull
    private final MinioItem item;

    public MinioItemResource(@NonNull final MinioItem item)
    {
        this.item = MinioUtils.requireNonNull(item);
    }

    @Override
    public boolean isFile()
    {
        return false;
    }

    @Override
    public boolean exists()
    {
        return item.isFile();
    }

    @NonNull
    @Override
    public URL getURL() throws IOException
    {
        try
        {
            return new URL(item.withOperations().getSignedObjectUrl());
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
        return item.toDescription();
    }

    @NonNull
    @Override
    public String getFilename() throws IllegalStateException
    {
        return item.getName();
    }

    @Nullable
    @Override
    public File getFile() throws IOException
    {
        throw new UnsupportedOperationException("MinioItemResource can not be resolved to java.io.File objects. Use getInputStream() to retrieve the contents of the object!");
    }

    @Override
    public long contentLength() throws IOException
    {
        return item.getSize();
    }

    @Override
    public long lastModified() throws IOException
    {
        final Date date = item.getLastModified();

        if (null != date)
        {
            return date.getTime();
        }
        throw new IOException(getDescription() + " not found.");
    }

    @NonNull
    @Override
    public InputStream getInputStream() throws IOException
    {
        try
        {
            return item.withOperations().getObjectInputStream();
        }
        catch (final MinioOperationException e)
        {
            throw new IOException(e);
        }
    }

    @NonNull
    @Override
    public MinioItemResource createRelative(@NonNull final String path) throws IOException
    {
        try
        {
            return item.withOperations().getItemRelative(path).map(self -> new MinioItemResource(self)).orElseThrow(() -> new IOException(path + " not found."));
        }
        catch (final MinioOperationException e)
        {
            throw new IOException(e);
        }
    }

    @Override
    public boolean equals(final Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (other instanceof MinioItemResource)
        {
            return toString().equals(MinioUtils.CAST(other, MinioItemResource.class).toString());
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
