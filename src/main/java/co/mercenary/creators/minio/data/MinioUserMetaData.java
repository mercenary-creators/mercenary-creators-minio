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

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import co.mercenary.creators.minio.errors.MinioDataException;
import co.mercenary.creators.minio.errors.MinioRuntimeException;
import co.mercenary.creators.minio.util.MinioUtils;
import co.mercenary.creators.minio.util.WithJSONOperations;
import co.mercenary.creators.minio.util.WithUserMetaData;

@JsonInclude(Include.NON_EMPTY)
public class MinioUserMetaData extends LinkedHashMap<String, String> implements WithJSONOperations, WithUserMetaData
{
    private static final long serialVersionUID = 5745617887209205963L;

    @NonNull
    public static MinioUserMetaData from(@Nullable final Map<String, List<String>> map)
    {
        final MinioUserMetaData tmp = new MinioUserMetaData();

        if ((null == map) || (map.isEmpty()))
        {
            return tmp;
        }
        MinioUtils.toKeys(map).stream().filter(MinioUtils::isAmazonMetaPrefix).distinct().forEach(key -> MinioUtils.toOptional(map.get(key)).map(MinioUtils::toZero).ifPresent(val -> tmp.put(key, val)));

        return tmp;
    }

    public MinioUserMetaData()
    {
        super();
    }

    public MinioUserMetaData(@Nullable final Map<String, String> map)
    {
        super(MinioUtils.toLinkedHashMap(map, true));
    }

    public MinioUserMetaData(@NonNull final String key, @NonNull final String val)
    {
        super(MinioUtils.toLinkedHashMap(MinioUtils.requireToString(key), MinioUtils.requireToString(val), true));
    }

    @Nullable
    @Override
    public String get(final Object key)
    {
        return super.get(MinioUtils.requireNonNull(key));
    }

    @Nullable
    @Override
    public String put(final String key, final String val)
    {
        return super.put(MinioUtils.requireToString(key), MinioUtils.requireToString(val));
    }

    @NonNull
    public MinioUserMetaData add(@NonNull final String key, @Nullable final String val)
    {
        if (null != val)
        {
            super.put(MinioUtils.requireToString(key), MinioUtils.requireToString(val));
        }
        return this;
    }

    @NonNull
    public MinioUserMetaData add(@Nullable final Map<String, String> map)
    {
        putAll(MinioUtils.toLinkedHashMap(map, true));

        return this;
    }

    @NonNull
    public MinioUserMetaData normalize()
    {
        final MinioUserMetaData tmp = new MinioUserMetaData();

        final MinioUserMetaData map = new MinioUserMetaData(this);

        map.keys().stream().filter(MinioUtils::isAmazonMetaPrefix).forEach(key -> tmp.add(MinioUtils.noAmazonMetaPrefix(key), map.get(key)));

        return tmp;
    }

    @NonNull
    public Collection<String> keys()
    {
        return MinioUtils.toKeys(this);
    }

    @NonNull
    @Override
    public Collection<String> values()
    {
        return MinioUtils.toVals(this);
    }

    @NonNull
    @Override
    public String toJSONString(final boolean pretty) throws MinioDataException
    {
        return MinioUtils.toJSONString(normalize(), pretty);
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
    @Override
    @JsonIgnore
    public Map<String, String> getUserMetaData()
    {
        final MinioUserMetaData map = normalize();

        final MinioUserMetaData tmp = new MinioUserMetaData();

        map.keys().forEach(key -> tmp.add(MinioUtils.toAmazonMetaPrefix(key), map.get(key)));

        return tmp;
    }
}
