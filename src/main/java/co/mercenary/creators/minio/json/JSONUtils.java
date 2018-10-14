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

package co.mercenary.creators.minio.json;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.BaseStream;

import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import co.mercenary.creators.minio.errors.MinioDataException;
import co.mercenary.creators.minio.util.MinioUtils;

public final class JSONUtils
{
    @NonNull
    private static final JSONMapper NORMAL = new JSONMapper();

    @NonNull
    private static final JSONMapper PRETTY = new JSONMapper(true);

    private JSONUtils()
    {
    }

    @NonNull
    public static byte[] toByteArray(@NonNull final Object value) throws MinioDataException
    {
        return NORMAL.toByteArray(value);
    }

    @NonNull
    public static String toJSONString(@NonNull final Object value) throws MinioDataException
    {
        return PRETTY.toJSONString(value);
    }

    @NonNull
    public static String toJSONString(@NonNull final Object value, final boolean pretty) throws MinioDataException
    {
        return (pretty ? PRETTY : NORMAL).toJSONString(value);
    }

    @NonNull
    public static <T> T toJSONObject(@NonNull final byte[] value, @NonNull final Class<T> type) throws MinioDataException
    {
        return NORMAL.toJSONObject(value, type);
    }

    @NonNull
    public static <T> T toJSONObject(@NonNull final File value, @NonNull final Class<T> type) throws MinioDataException
    {
        return NORMAL.toJSONObject(value, type);
    }

    @NonNull
    public static <T> T toJSONObject(@NonNull final Path value, @NonNull final Class<T> type) throws MinioDataException
    {
        return NORMAL.toJSONObject(value, type);
    }

    @NonNull
    public static <T> T toJSONObject(@NonNull final CharSequence value, @NonNull final Class<T> type) throws MinioDataException
    {
        return NORMAL.toJSONObject(value, type);
    }

    @NonNull
    public static <T> T toJSONObject(@NonNull final Reader value, @NonNull final Class<T> type) throws MinioDataException
    {
        return NORMAL.toJSONObject(value, type);
    }

    @NonNull
    public static <T> T toJSONObject(@NonNull final InputStream value, @NonNull final Class<T> type) throws MinioDataException
    {
        return NORMAL.toJSONObject(value, type);
    }

    @NonNull
    public static <T> T toJSONObject(@NonNull final Resource value, @NonNull final Class<T> type) throws MinioDataException
    {
        return NORMAL.toJSONObject(value, type);
    }

    public static boolean canSerialize(@Nullable final Class<?> type)
    {
        return NORMAL.canSerialize(type);
    }

    public static boolean canSerialize(@Nullable final Object object)
    {
        return NORMAL.canSerializeObject(object);
    }

    @Nullable
    public static <T> T convert(@Nullable final Object object, @NonNull final Class<T> type) throws MinioDataException
    {
        return NORMAL.convert(object, type);
    }

    @NonNull
    public static JSONType toJSONType(@Nullable final Object object)
    {
        if (null == object)
        {
            return JSONType.NULL;
        }
        if (object instanceof CharSequence)
        {
            return JSONType.STRING;
        }
        if (object instanceof Number)
        {
            return JSONType.NUMBER;
        }
        if (object instanceof Date)
        {
            return JSONType.DATE;
        }
        if (object instanceof Map)
        {
            return JSONType.OBJECT;
        }
        if (object instanceof Iterable)
        {
            return JSONType.ARRAY;
        }
        if (object instanceof BaseStream)
        {
            return JSONType.ARRAY;
        }
        if (object.getClass().isArray())
        {
            return JSONType.ARRAY;
        }
        if (object instanceof Optional)
        {
            final Optional<?> value = MinioUtils.CAST(object);

            if (value.isPresent())
            {
                return toJSONType(value.get());
            }
            return JSONType.NULL;
        }
        try
        {
            return toJSONType(convert(object, LinkedHashMap.class));
        }
        catch (final MinioDataException e)
        {
            try
            {
                return toJSONType(convert(object, ArrayList.class));
            }
            catch (final MinioDataException t)
            {
                return JSONType.UNDEFINED;
            }
        }
    }
}