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

import org.springframework.lang.NonNull;

import co.mercenary.creators.minio.util.MinioUtils;
import co.mercenary.creators.minio.util.WithSelf;

public abstract class AbstractMinioResourceWith<T> extends AbstractMinioResource implements WithSelf<T>
{
    @NonNull
    private final T with;

    protected AbstractMinioResourceWith(@NonNull final T with, @NonNull final CharSequence description)
    {
        super(description);

        this.with = MinioUtils.requireNonNull(with);
    }

    protected AbstractMinioResourceWith(@NonNull final T with, @NonNull final CharSequence format, @NonNull final Object... args)
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
