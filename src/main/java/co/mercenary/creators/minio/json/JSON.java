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

package co.mercenary.creators.minio.json;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import co.mercenary.creators.minio.errors.MinioDataException;
import co.mercenary.creators.minio.errors.MinioRuntimeException;
import co.mercenary.creators.minio.util.MinioUtils;

public class JSON extends LinkedHashMap<String, Object> implements WithJSONOperations
{
    private static final long serialVersionUID = 3248651885486937763L;

    public JSON()
    {
        super();
    }

    public JSON(final int size)
    {
        super(size);
    }

    public JSON(@Nullable final Map<String, ?> map)
    {
        super(MinioUtils.toLinkedHashMap(map));
    }

    public JSON(@NonNull final String key, @Nullable final Object val)
    {
        put(key, val);
    }

    @NonNull
    @Override
    public String toJSONString(final boolean pretty) throws MinioDataException
    {
        return JSONUtils.toJSONString(this, pretty);
    }

    @NonNull
    @Override
    public String toString()
    {
        try
        {
            return toJSONString();
        }
        catch (final MinioDataException e)
        {
            throw new MinioRuntimeException(e);
        }
    }

    @NonNull
    public JSON add(@NonNull final String key, @Nullable final Object val)
    {
        put(key, val);

        return this;
    }

    @NonNull
    public JSON add(@Nullable final Map<String, ?> map)
    {
        if ((null != map) && (false == map.isEmpty()))
        {
            super.putAll(map);
        }
        return this;
    }

    @NonNull
    public byte[] toByteArray() throws MinioDataException
    {
        return JSONUtils.toByteArray(this);
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
        if (other instanceof JSON)
        {
            return toString().equals(other.toString());
        }
        return false;
    }
}
