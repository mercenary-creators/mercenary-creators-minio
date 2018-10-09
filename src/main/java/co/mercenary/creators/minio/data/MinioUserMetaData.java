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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import co.mercenary.creators.minio.util.MinioUtils;
import co.mercenary.creators.minio.util.WithUserMetaData;

@JsonInclude(Include.NON_EMPTY)
public class MinioUserMetaData extends LinkedHashMap<String, String> implements WithUserMetaData
{
    private static final long serialVersionUID = 5745617887209205963L;

    public MinioUserMetaData()
    {
        super();
    }

    public MinioUserMetaData(@Nullable final Map<String, List<String>> map)
    {
        if (null != map)
        {
            map.keySet().stream().filter(MinioUtils::isAmazonMetaPrefix).forEach(key -> MinioUtils.toOptional(map.get(key)).map(MinioUtils::toZero).ifPresent(val -> plus(MinioUtils.noAmazonMetaPrefix(key), val)));
        }
    }

    public MinioUserMetaData(@NonNull final String key, @Nullable final String val)
    {
        plus(key, val);
    }

    @Nullable
    @Override
    public String get(final Object key)
    {
        return super.get(MinioUtils.requireNonNull(key));
    }

    @Nullable
    @Override
    public String remove(final Object key)
    {
        return super.remove(MinioUtils.requireNonNull(key));
    }

    @Nullable
    @Override
    public String put(final String key, final String val)
    {
        return super.put(MinioUtils.requireToString(key), MinioUtils.requireToString(val));
    }

    @NonNull
    public MinioUserMetaData plus(@NonNull final String key, @Nullable final String val)
    {
        if (null != val)
        {
            put(key, val);
        }
        return this;
    }

    @NonNull
    public MinioUserMetaData minus(@NonNull final String key, final String... keys)
    {
        return minus(MinioUtils.concat(key, keys));
    }

    @NonNull
    public MinioUserMetaData minus(@NonNull final Collection<String> keys)
    {
        return minus(keys.stream());
    }

    @NonNull
    public MinioUserMetaData minus(@NonNull final Stream<String> stream)
    {
        stream.filter(MinioUtils::isNonNull).distinct().forEach(this::remove);

        return this;
    }

    @NonNull
    @Override
    @JsonIgnore
    public Map<String, String> getUserMetaData()
    {
        final MinioUserMetaData tmp = new MinioUserMetaData();

        keySet().forEach(key -> tmp.put(MinioUtils.toAmazonMetaPrefix(key), get(key)));

        return tmp;
    }
}
