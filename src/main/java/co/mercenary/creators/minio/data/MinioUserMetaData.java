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

package co.mercenary.creators.minio.data;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import co.mercenary.creators.minio.errors.MinioDataException;
import co.mercenary.creators.minio.json.JSONUtils;
import co.mercenary.creators.minio.json.WithJSONOperations;
import co.mercenary.creators.minio.util.MinioUtils;
import co.mercenary.creators.minio.util.WithUserMetaData;

public class MinioUserMetaData extends LinkedHashMap<String, String> implements WithUserMetaData, WithJSONOperations
{
    private static final long serialVersionUID = 5745617887209205963L;

    public MinioUserMetaData()
    {
        super();
    }

    public MinioUserMetaData(final int size)
    {
        super(size);
    }

    public MinioUserMetaData(@Nullable final Map<String, List<String>> map)
    {
        if (null != map)
        {
            map.keySet().stream().filter(MinioUtils::isAmazonMetaPrefix).forEach(key -> MinioUtils.toOptional(map.get(key)).map(MinioUtils::toZero).ifPresent(val -> add(MinioUtils.noAmazonMetaPrefix(key), val)));
        }
    }

    public MinioUserMetaData(@NonNull final String key, @Nullable final String val)
    {
        add(key, val);
    }

    @Nullable
    @Override
    public String get(@NonNull final Object key)
    {
        return super.get(MinioUtils.requireNonNull(key));
    }

    @Nullable
    @Override
    public String remove(@NonNull final Object key)
    {
        return super.remove(MinioUtils.requireNonNull(key));
    }

    @Nullable
    @Override
    public String put(@NonNull final String key, final String val)
    {
        return super.put(MinioUtils.requireNonNull(key), MinioUtils.requireNonNull(val));
    }

    @NonNull
    public MinioUserMetaData add(@NonNull final String key, @Nullable final String val)
    {
        if (null != val)
        {
            put(key, val);
        }
        return this;
    }

    @NonNull
    public MinioUserMetaData add(@Nullable final Map<String, String> map)
    {
        if ((null != map) && (false == map.isEmpty()))
        {
            super.putAll(MinioUtils.toLinkedHashMap(map, true));
        }
        return this;
    }

    @NonNull
    public MinioUserMetaData del(@NonNull final String key, @NonNull final String... keys)
    {
        return del(Stream.concat(Stream.of(MinioUtils.requireNonNull(key)), Stream.of(MinioUtils.requireNonNull(keys))));
    }

    @NonNull
    public MinioUserMetaData del(@NonNull final Collection<String> keys)
    {
        return del(keys.stream());
    }

    @NonNull
    public MinioUserMetaData del(@NonNull final Stream<String> stream)
    {
        stream.filter(MinioUtils::isNonNull).distinct().forEach(this::remove);

        return this;
    }

    @NonNull
    @Override
    @JsonIgnore
    public Map<String, String> getUserMetaData()
    {
        final MinioUserMetaData map = new MinioUserMetaData(size());

        keySet().forEach(key -> map.add(MinioUtils.toAmazonMetaPrefix(key), get(key)));

        return MinioUtils.toUnmodifiable(map);
    }

    @NonNull
    @Override
    public String toJSONString(final boolean pretty) throws MinioDataException
    {
        return JSONUtils.toJSONString(getUserMetaData(), pretty);
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
            return super.toString();
        }
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
        if (other instanceof MinioUserMetaData)
        {
            return toString().equals(other.toString());
        }
        return false;
    }
}
