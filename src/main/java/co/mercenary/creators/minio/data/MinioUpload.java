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

import com.fasterxml.jackson.annotation.JsonIgnore;

import co.mercenary.creators.minio.MinioOperations;
import co.mercenary.creators.minio.errors.MinioOperationException;
import co.mercenary.creators.minio.util.AbstractCommon;
import co.mercenary.creators.minio.util.MinioUtils;
import co.mercenary.creators.minio.util.WithOperations;

public class MinioUpload extends AbstractCommon implements WithOperations<MinioUploadOperations>
{
    @NonNull
    private final String                buck;

    @NonNull
    private final MinioUploadOperations oper;

    public MinioUpload(@NonNull final String name, @NonNull final String buck, @NonNull final MinioOperations oper)
    {
        super(MinioUtils.fixPathString(name));

        this.buck = MinioUtils.requireNonNull(buck);

        this.oper = buildWithOperations(this, oper);
    }

    @NonNull
    public String getBucket()
    {
        return buck;
    }

    @NonNull
    @Override
    @JsonIgnore
    public MinioUploadOperations withOperations()
    {
        return oper;
    }

    @NonNull
    @Override
    @JsonIgnore
    public String toDescription()
    {
        return MinioUtils.format("name=(%s), bucket=(%s).", getName(), getBucket());
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

    @NonNull
    protected static MinioUploadOperations buildWithOperations(@NonNull final MinioUpload self, @NonNull final MinioOperations oper)
    {
        MinioUtils.isEachNonNull(self, oper);

        return new MinioUploadOperations()
        {
            @NonNull
            @Override
            public MinioUpload self()
            {
                return self;
            }

            @NonNull
            @Override
            public String getServer()
            {
                return oper.getServer();
            }

            @NonNull
            @Override
            public String getRegion()
            {
                return oper.getRegion();
            }

            @Override
            public boolean removeUpload() throws MinioOperationException
            {
                return oper.removeUpload(self().getBucket(), self().getName());
            }
        };
    }
}
