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

import co.mercenary.creators.minio.util.AbstractCommon;
import co.mercenary.creators.minio.util.MinioUtils;

public class MinioUpload extends AbstractCommon
{
    @NonNull
    private final String buck;

    public MinioUpload(@NonNull final CharSequence name, @NonNull final CharSequence buck)
    {
        super(name);

        this.buck = MinioUtils.requireToString(buck);
    }

    @NonNull
    public String getBucket()
    {
        return buck;
    }

    @NonNull
    @Override
    public String toDescription()
    {
        return MinioUtils.format("class=(%s), name=(%s), bucket=(%s).", getClass().getCanonicalName(), getName(), getBucket());
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
        if (other instanceof MinioUpload)
        {
            return toString().equals(other.toString());
        }
        return false;
    }
}
