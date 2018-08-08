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

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import co.mercenary.creators.minio.MinioUtils;

public abstract class MinioCommon extends MinioNamed
{
    private final long   m_size;

    @Nullable
    private final String m_etag;

    @NonNull
    private final String m_buck;

    protected MinioCommon(@NonNull final CharSequence name, @NonNull final CharSequence buck, @Nullable final CharSequence etag, final long size)
    {
        super(name);

        m_buck = MinioUtils.requireToString(buck);

        m_etag = MinioUtils.getETagSequence(etag);

        m_size = size;
    }

    public long getSize()
    {
        return m_size;
    }

    @Nullable
    public String getEtag()
    {
        return m_etag;
    }

    @NonNull
    public String getBucket()
    {
        return m_buck;
    }
}
