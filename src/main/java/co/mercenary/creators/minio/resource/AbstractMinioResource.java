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

import org.springframework.core.io.AbstractResource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import co.mercenary.creators.minio.util.MinioUtils;

public abstract class AbstractMinioResource extends AbstractResource
{
    @NonNull
    private final String description;

    protected AbstractMinioResource(@NonNull final CharSequence description)
    {
        this.description = MinioUtils.requireToString(description);
    }

    protected AbstractMinioResource(@NonNull final CharSequence format, @NonNull final Object... args)
    {
        this(MinioUtils.format(format, args));
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
    public File getFile() throws IOException
    {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " can not be resolved to java.io.File objects. Use getInputStream() to retrieve the contents of the object!");
    }

    @NonNull
    @Override
    public String getDescription()
    {
        return description;
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
            return ((AbstractMinioResource) other).getDescription().equals(getDescription());
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
