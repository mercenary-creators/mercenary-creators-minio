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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;

import co.mercenary.creators.minio.data.MinioItem;
import co.mercenary.creators.minio.errors.MinioOperationException;

public class MinioItemResource extends AbstractMinioResourceWith<MinioItem>
{
    public MinioItemResource(@NonNull final MinioItem item)
    {
        this(item, item.toDescription());
    }

    public MinioItemResource(@NonNull final MinioItem item, @NonNull final CharSequence description)
    {
        super(item, description);
    }

    @Override
    public boolean exists()
    {
        return self().isFile();
    }

    @NonNull
    @Override
    public URL getURL() throws IOException
    {
        try
        {
            return new URL(self().withOperations().getSignedObjectUrl());
        }
        catch (final MinioOperationException e)
        {
            throw new IOException(e);
        }
    }

    @Override
    public long contentLength() throws IOException
    {
        return self().getSize();
    }

    @Override
    public long lastModified() throws IOException
    {
        final Date date = self().getLastModified();

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
            return self().withOperations().getObjectInputStream();
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
        try
        {
            return self().withOperations().getItemRelative(path).map(self -> new MinioItemResource(self)).orElseThrow(() -> new IOException(path + " not found."));
        }
        catch (final MinioOperationException e)
        {
            throw new IOException(e);
        }
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
