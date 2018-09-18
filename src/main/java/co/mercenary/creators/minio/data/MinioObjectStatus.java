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
import java.util.function.Supplier;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import co.mercenary.creators.minio.util.MinioUtils;

public class MinioObjectStatus extends MinioCommon
{
    @Nullable
    private final Date   time;

    @NonNull
    private final String type;

    public MinioObjectStatus(@NonNull final CharSequence name, @NonNull final CharSequence buck, final long size, @Nullable final CharSequence type, @Nullable final CharSequence etag, @NonNull final Supplier<Date> time)
    {
        super(name, buck, etag, size);

        this.time = MinioUtils.toValueNonNull(time);

        this.type = MinioUtils.fixContentType(type);
    }

    @Nullable
    public Date getCreationTime()
    {
        return MinioUtils.COPY(time);
    }

    @NonNull
    public String getContentType()
    {
        return type;
    }

    @NonNull
    @Override
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
