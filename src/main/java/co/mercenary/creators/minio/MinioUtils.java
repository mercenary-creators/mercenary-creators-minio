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

package co.mercenary.creators.minio;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import io.minio.Result;

public final class MinioUtils
{
    private static final long DEFAULT_EXPIRY_TIME = 7 * 24 * 3600;

    private MinioUtils()
    {
    }

    @Nullable
    public static <T> T NULL()
    {
        return null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T CAST(@Nullable final Object value)
    {
        return ((T) value);
    }

    @Nullable
    public static <T> T SAFE(@Nullable final Object value, @NonNull final Class<T> type)
    {
        if ((null != value) && (ClassUtils.isAssignableValue(type, value)))
        {
            return type.cast(value);
        }
        return NULL();
    }

    public static boolean isNull(@Nullable final Object value)
    {
        return (null == value);
    }

    public static boolean isNonNull(@Nullable final Object value)
    {
        return (null != value);
    }

    @NonNull
    public static <T> T requireNonNullOrElse(@Nullable final T value, @NonNull final T otherwise)
    {
        return (null != value) ? value : otherwise;
    }

    @NonNull
    public static <T> T requireNonNullOrElse(@Nullable final T value, @NonNull final Supplier<T> otherwise)
    {
        return (null != value) ? value : otherwise.get();
    }

    @NonNull
    public static <T> T requireNonNull(@Nullable final T value)
    {
        return Objects.requireNonNull(value);
    }

    @NonNull
    public static <T> T requireNonNull(@Nullable final T value, @NonNull final String reason)
    {
        return Objects.requireNonNull(value, reason);
    }

    @NonNull
    public static <T> T requireNonNull(@Nullable final T value, @NonNull final Supplier<String> reason)
    {
        return Objects.requireNonNull(value, reason);
    }

    @Nullable
    public static <T> T toValueNonNull(@NonNull final Supplier<T> supplier)
    {
        try
        {
            return supplier.get();
        }
        catch (final NullPointerException e)
        {
            return NULL();
        }
    }

    @Nullable
    public static String getCharSequence(@Nullable final CharSequence value)
    {
        if (null != value)
        {
            return value.toString();
        }
        return NULL();
    }

    @NonNull
    public static byte[] getBytes(@NonNull final CharSequence value)
    {
        return value.toString().getBytes(StandardCharsets.UTF_8);
    }

    @NonNull
    public static String requireToString(@Nullable final CharSequence value)
    {
        return requireNonNull(getCharSequence(value));
    }

    @NonNull
    public static String requireToStringOrElse(@Nullable final CharSequence value, @NonNull final String otherwise)
    {
        return requireNonNullOrElse(getCharSequence(value), otherwise);
    }

    @NonNull
    public static String requireToStringOrElse(@Nullable final CharSequence value, @NonNull final Supplier<String> otherwise)
    {
        return requireNonNullOrElse(getCharSequence(value), otherwise);
    }

    @Nullable
    public static String getETagSequence(@Nullable final CharSequence value)
    {
        final String string = getCharSequence(value);

        if ((null != string) && (string.indexOf('"') >= 0))
        {
            return string.replaceAll("\"", "");
        }
        return string;
    }

    @Nullable
    public static String fixContentType(@Nullable final CharSequence value)
    {
        return getCharSequence(value);
    }

    @NonNull
    public static <T> T getResultNonNull(@NonNull final Result<T> result) throws MinioOperationException
    {
        try
        {
            return requireNonNull(requireNonNull(result).get());
        }
        catch (final Exception e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Nullable
    public static <T> T getResultNullable(@NonNull final Result<T> result)
    {
        try
        {
            return result.get();
        }
        catch (final Exception e)
        {
            return NULL();
        }
    }

    @NonNull
    public static <T> Stream<T> getResultAsStream(@NonNull final Iterable<Result<T>> iterable)
    {
        return StreamSupport.stream(iterable.spliterator(), false).map(result -> getResultNullable(result)).filter(MinioUtils::isNonNull);
    }

    @NonNull
    public static String getDefaultContentType()
    {
        return "application/octet-stream";
    }

    @NonNull
    public static InputStream getInputStream(@NonNull final File file) throws IOException
    {
        return getInputStream(file.toPath());
    }

    @NonNull
    public static InputStream getInputStream(@NonNull final Path path) throws IOException
    {
        return Files.newInputStream(requireNonNull(path));
    }

    public static long getSize(@NonNull final File file) throws IOException
    {
        return file.length();
    }

    public static long getSize(@NonNull final Path path) throws IOException
    {
        return Files.size(requireNonNull(path));
    }

    public static int getDuration(final long time) throws MinioOperationException
    {
        if ((time < 1) || (time > DEFAULT_EXPIRY_TIME))
        {
            throw new MinioOperationException(String.format("bad duration %s", time));
        }
        return CAST(time);
    }

    public static int getDuration(@NonNull final Duration time) throws MinioOperationException
    {
        return getDuration(time.getSeconds());
    }

    public static int getDuration(final long time, @NonNull final TimeUnit unit) throws MinioOperationException
    {
        return getDuration(unit.toSeconds(time));
    }
}
