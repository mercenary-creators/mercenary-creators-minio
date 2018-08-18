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

import co.mercenary.creators.minio.util.AbstractNamed;
import co.mercenary.creators.minio.util.MinioUtils;

public abstract class MinioCommon extends AbstractNamed
{
    private final long   size;

    @Nullable
    private final String etag;

    @NonNull
    private final String buck;

    protected MinioCommon(@NonNull final CharSequence name, @NonNull final CharSequence buck, @Nullable final CharSequence etag, final long size)
    {
        super(name);

        this.buck = MinioUtils.requireToString(buck);

        this.etag = MinioUtils.getETagSequence(etag);

        this.size = size;
    }

    public long getSize()
    {
        return size;
    }

    @Nullable
    public String getEtag()
    {
        return etag;
    }

    @NonNull
    public String getBucket()
    {
        return buck;
    }
}
