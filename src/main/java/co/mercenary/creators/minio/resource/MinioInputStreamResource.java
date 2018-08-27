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
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.core.io.AbstractResource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import co.mercenary.creators.minio.util.MinioUtils;

public class MinioInputStreamResource extends AbstractResource
{
    @NonNull
    private static final String DESCRP = "MinioInputStreamResource loaded through InputStream";

    @NonNull
    private final String        descrp;

    @NonNull
    private final InputStream   stream;

    @NonNull
    private final AtomicBoolean isread = new AtomicBoolean(false);

    public MinioInputStreamResource(@NonNull final InputStream stream)
    {
        this(stream, DESCRP);
    }

    public MinioInputStreamResource(@NonNull final InputStream stream, @Nullable final CharSequence descrp)
    {
        this.stream = MinioUtils.requireNonNull(stream);

        this.descrp = MinioUtils.toStringOrElse(descrp, DESCRP);
    }

    @Override
    public boolean exists()
    {
        return true;
    }

    @Override
    public boolean isOpen()
    {
        return true;
    }

    @NonNull
    @Override
    public String getDescription()
    {
        return descrp;
    }

    @Nullable
    @Override
    public File getFile() throws IOException
    {
        throw new UnsupportedOperationException("MinioInputStreamResource can not be resolved to java.io.File objects. Use getInputStream() to retrieve the contents of the object!");
    }

    @NonNull
    @Override
    public InputStream getInputStream() throws IOException
    {
        if (isread.getAndSet(true))
        {
            throw new IOException(getDescription());
        }
        return stream;
    }

    @Override
    public boolean equals(final Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (other instanceof MinioInputStreamResource)
        {
            return stream.equals(MinioUtils.CAST(other, MinioInputStreamResource.class).stream);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return stream.hashCode();
    }

    @NonNull
    @Override
    public String toString()
    {
        return getDescription();
    }
}
