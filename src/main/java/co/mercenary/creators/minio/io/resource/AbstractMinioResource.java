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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.springframework.core.io.AbstractResource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import co.mercenary.creators.minio.util.MinioUtils;
import co.mercenary.creators.minio.util.WithSelf;

@JsonIgnoreType
public abstract class AbstractMinioResource<T> extends AbstractResource implements WithSelf<T>
{
    @NonNull
    private final T      with;

    @NonNull
    private final String desc;

    protected AbstractMinioResource(@NonNull final T with, @NonNull final String desc)
    {
        this.with = MinioUtils.requireNonNull(with);

        this.desc = MinioUtils.requireNonNull(desc);
    }

    protected AbstractMinioResource(@NonNull final T with, @NonNull final String format, @NonNull final Object... args)
    {
        this(with, MinioUtils.format(format, args));
    }

    @NonNull
    @Override
    public T self()
    {
        return with;
    }

    @Override
    public boolean isFile()
    {
        return false;
    }

    @Override
    public boolean isOpen()
    {
        return false;
    }

    @Override
    public boolean isReadable()
    {
        return true;
    }

    @Nullable
    @Override
    public String getFilename()
    {
        return MinioUtils.NULL();
    }

    @Nullable
    @Override
    public URL getURL() throws IOException
    {
        throw new FileNotFoundException(getDescription() + " can not be resolved to a URL.");
    }

    @Nullable
    @Override
    public URI getURI() throws IOException
    {
        return super.getURI();
    }

    @Nullable
    @Override
    public File getFile() throws IOException
    {
        throw new FileNotFoundException(getDescription() + " can not be resolved to java.io.File objects. Use getInputStream() to retrieve the contents of the object!");
    }

    @NonNull
    @Override
    public String getDescription()
    {
        return desc;
    }

    @Override
    public boolean equals(final Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (other instanceof AbstractMinioResource)
        {
            final AbstractMinioResource<?> temp = MinioUtils.CAST(other, AbstractMinioResource.class);

            if (getDescription().equals(temp.getDescription()))
            {
                return self().equals(temp.self());
            }
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
