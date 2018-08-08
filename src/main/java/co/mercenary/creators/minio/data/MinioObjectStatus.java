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

import co.mercenary.creators.minio.MinioUtils;

public class MinioObjectStatus extends MinioCommon
{
    @Nullable
    private final Date   m_time;

    @Nullable
    private final String m_type;

    public MinioObjectStatus(@NonNull final CharSequence name, @NonNull final CharSequence buck, final long size, @Nullable final CharSequence type, @Nullable final CharSequence etag, @NonNull final Supplier<Date> time)
    {
        super(name, buck, etag, size);

        m_type = MinioUtils.fixContentType(type);

        m_time = MinioUtils.toValueNonNull(time);
    }

    @Nullable
    public Date getCreationTime()
    {
        return m_time;
    }

    @Nullable
    public String getContentType()
    {
        return m_type;
    }
}
