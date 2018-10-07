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

package co.mercenary.creators.minio.resource;

import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import co.mercenary.creators.minio.MinioOperations;
import co.mercenary.creators.minio.util.MinioUtils;

public class MinioProtocolResolver implements ProtocolResolver
{
    @NonNull
    private final MinioOperations oper;

    public MinioProtocolResolver(@NonNull final MinioOperations oper)
    {
        this.oper = MinioUtils.requireNonNull(oper);
    }

    @Nullable
    @Override
    public Resource resolve(@NonNull final String location, @NonNull final ResourceLoader loader)
    {
        if (MinioResourceUtils.isMinioResourceProtocol(location))
        {
            return new MinioResource(oper, MinioResourceUtils.getBucketNameFromLocation(location), MinioResourceUtils.getObjectNameFromLocation(location));
        }
        else
        {
            return MinioUtils.NULL();
        }
    }
}
