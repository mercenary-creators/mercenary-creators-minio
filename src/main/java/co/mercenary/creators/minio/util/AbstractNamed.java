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

package co.mercenary.creators.minio.util;

import org.springframework.lang.NonNull;

public abstract class AbstractNamed implements WithName, WithDescription
{
    @NonNull
    private String name;

    protected AbstractNamed(@NonNull final CharSequence name)
    {
        this.name = MinioUtils.requireToString(name);
    }

    @NonNull
    @Override
    public String getName()
    {
        return name;
    }

    protected void setName(@NonNull final CharSequence name)
    {
        this.name = MinioUtils.requireToString(name);
    }

    @NonNull
    @Override
    public String toDescription()
    {
        return MinioUtils.format("name=(%s).", getName());
    }

    @NonNull
    @Override
    public String toString()
    {
        return toDescription();
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
        if (other instanceof AbstractNamed)
        {
            return toString().equals(other.toString());
        }
        return false;
    }
}
