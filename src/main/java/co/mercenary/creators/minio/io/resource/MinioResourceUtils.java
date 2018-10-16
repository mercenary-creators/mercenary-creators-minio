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

package co.mercenary.creators.minio.io.resource;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import co.mercenary.creators.minio.util.MinioUtils;

public final class MinioResourceUtils
{
    @NonNull
    public static final String MINIO_RESOURCE_PROTOCOL = "minio://";

    private MinioResourceUtils()
    {
    }

    public static boolean isMinioResourceProtocol(@Nullable final String location)
    {
        if ((null == location) || (location.length() < 1))
        {
            return false;
        }
        return location.toLowerCase().startsWith(MINIO_RESOURCE_PROTOCOL);
    }

    @NonNull
    public static String noMinioResourceProtocol(@Nullable final String location)
    {
        if (isMinioResourceProtocol(location))
        {
            return location.substring(MINIO_RESOURCE_PROTOCOL.length());
        }
        throw new IllegalArgumentException("the location :'" + MinioUtils.toStringOrElse(location, MinioUtils.NULLS_STRING_VALUED) + "' is not a valid ninio location.");
    }

    @NonNull
    public static String getBucketNameFromLocation(@Nullable final String location)
    {
        if (isMinioResourceProtocol(location))
        {
            final String value = MinioUtils.requireNonNull(location);

            final int index = value.indexOf(MinioUtils.PATH_SEPARATOR_CHAR, MINIO_RESOURCE_PROTOCOL.length());

            if ((index < 0) || (index == MINIO_RESOURCE_PROTOCOL.length()))
            {
                throw new IllegalArgumentException("the location :'" + value + "' does not contain a valid bucket name.");
            }
            return value.substring(MINIO_RESOURCE_PROTOCOL.length(), index);
        }
        throw new IllegalArgumentException("the location :'" + MinioUtils.toStringOrElse(location, MinioUtils.NULLS_STRING_VALUED) + "' is not a valid ninio location.");
    }

    @NonNull
    public static String getObjectNameFromLocation(@NonNull final String location)
    {
        if (isMinioResourceProtocol(location))
        {
            final String value = MinioUtils.requireNonNull(location);

            final int index = value.indexOf(MinioUtils.PATH_SEPARATOR_CHAR, MINIO_RESOURCE_PROTOCOL.length());

            if ((index < 0) || (index == MINIO_RESOURCE_PROTOCOL.length()))
            {
                throw new IllegalArgumentException("the location :'" + value + "' does not contain a valid bucket name.");
            }
            if (value.endsWith(MinioUtils.PATH_SEPARATOR_CHAR))
            {
                return value.substring(index + 1, value.length() - 1);
            }
            return value.substring(index + 1, value.length());
        }
        throw new IllegalArgumentException("the location :'" + MinioUtils.toStringOrElse(location, MinioUtils.NULLS_STRING_VALUED) + "' is not a valid ninio location.");
    }

    @NonNull
    public static String getLocationForBucketAndObject(@NonNull final String bucket, @NonNull final String object)
    {
        final int size = MINIO_RESOURCE_PROTOCOL.length() + bucket.length() + MinioUtils.PATH_SEPARATOR_CHAR.length() + object.length();

        return new StringBuilder(size).append(MINIO_RESOURCE_PROTOCOL).append(bucket).append(MinioUtils.PATH_SEPARATOR_CHAR).append(object).toString();
    }
}
