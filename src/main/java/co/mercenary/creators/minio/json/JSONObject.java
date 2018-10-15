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

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import co.mercenary.creators.minio.errors.MinioDataException;
import co.mercenary.creators.minio.errors.MinioRuntimeException;

public class JSONObject extends LinkedHashMap<String, Object> implements WithJSONOperations
{
    private static final long serialVersionUID = 3248651885486937763L;

    public JSONObject()
    {
        super();
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
            return toJSONString(false);
        }
        catch (final MinioDataException e)
        {
            throw new MinioRuntimeException(e);
        }
    }

    @NonNull
    public JSONObject plus(@NonNull final String key, @Nullable final Object val)
    {
        super.put(key, val);

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
        if (other instanceof JSONObject)
        {
            return toString().equals(other.toString());
        }
        return false;
    }
}
