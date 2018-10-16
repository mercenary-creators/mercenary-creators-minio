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

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import co.mercenary.creators.minio.util.MinioUtils;
import co.mercenary.creators.minio.util.WithUserMetaData;

public class MinioObjectStatus extends MinioCommon implements WithUserMetaData
{
    @NonNull
    private final String              type;

    @NonNull
    private final Optional<Date>      time;

    @NonNull
    private final Map<String, String> meta;

    public MinioObjectStatus(@NonNull final String name, @NonNull final String buck, final long size, @Nullable final String type, @Nullable final String etag, @NonNull final Supplier<Date> time, @NonNull final Map<String, String> meta)
    {
        super(name, buck, etag, size);

        this.type = MinioUtils.fixContentType(type);

        this.time = MinioUtils.toMaybeNonNull(time);

        this.meta = MinioUtils.requireNonNull(meta);
    }

    @NonNull
    public String getContentType()
    {
        return type;
    }

    @NonNull
    @JsonInclude(Include.NON_ABSENT)
    public Optional<Date> getCreationTime()
    {
        return time.map(date -> new Date(date.getTime()));
    }

    @NonNull
    @Override
    @JsonInclude(Include.NON_EMPTY)
    public Map<String, String> getUserMetaData()
    {
        if (meta.isEmpty())
        {
            return MinioUtils.emptyMap();
        }
        return MinioUtils.toUnmodifiable(meta);
    }

    @NonNull
    @Override
    @JsonIgnore
    public String toDescription()
    {
        return MinioUtils.format("name=(%s), bucket=(%s), etag=(%s), size=(%s), contentType=(%s), creationTime=(%s).", getName(), getBucket(), getEtag(), getSize(), getContentType(), MinioUtils.format(time));
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
        if (other instanceof MinioObjectStatus)
        {
            return toString().equals(other.toString());
        }
        return false;
    }
}
