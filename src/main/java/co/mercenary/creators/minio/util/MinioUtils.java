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

package co.mercenary.creators.minio.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.xmlpull.v1.XmlPullParserException;

import co.mercenary.creators.minio.errors.MinioDataException;
import io.minio.Result;
import io.minio.errors.MinioException;

public final class MinioUtils
{
    @NonNull
    public static final Long                    MINIMUM_EXPIRY_TIME = 1L;

    @NonNull
    public static final Long                    MAXIMUM_EXPIRY_TIME = 7L * 24L * 3600L;

    @NonNull
    public static final String                  EMPTY_STRING_VALUED = "";

    @NonNull
    public static final String                  SPACE_STRING_VALUED = " ";

    @NonNull
    public static final String                  PATH_SEPARATOR_CHAR = "/";

    @NonNull
    public static final String                  QUOTE_STRING_VALUED = "\"";

    @NonNull
    public static final String                  NULLS_STRING_VALUED = "null";

    @NonNull
    public static final String                  DEFAULT_REGION_EAST = "us-east-1";

    @NonNull
    public static final ThreadLocal<DateFormat> DEFAULT_DATE_FORMAT = ThreadLocal.withInitial(MinioUtils::getDefaultDateFormat);

    private MinioUtils()
    {
    }

    @Nullable
    public static <T> T NULL()
    {
        return null;
    }

    @Nullable
    public static <T> T NULL(@NonNull final Class<T> type)
    {
        return NULL();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T CAST(@Nullable final Object value)
    {
        return ((T) value);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static <T> T CAST(@NonNull final Object value, @NonNull final Class<T> type)
    {
        return ((T) requireNonNull(value));
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

    @NonNull
    public static Log LOGS(@NonNull final Class<?> type)
    {
        return LogFactory.getLog(requireNonNull(type));
    }

    @Nullable
    public static Date COPY(@Nullable final Date date)
    {
        if (null != date)
        {
            return CAST(date.clone());
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

    public static void testAllNonNull(@NonNull final Object... values)
    {
        for (final Object value : values)
        {
            requireNonNull(value);
        }
    }

    @NonNull
    public static <T> T requireNonNull(final T value)
    {
        if (null == value)
        {
            throw new NullPointerException();
        }
        return value;
    }

    @NonNull
    public static <T> T requireNonNull(final T value, @NonNull final String reason)
    {
        if (null == value)
        {
            throw new NullPointerException(reason);
        }
        return value;
    }

    @NonNull
    public static <T> T requireNonNull(final T value, @NonNull final Supplier<String> reason)
    {
        if (null == value)
        {
            throw new NullPointerException(reason.get());
        }
        return value;
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
    public static <T> Optional<T> toOptional(@Nullable final T value)
    {
        if (null == value)
        {
            return Optional.empty();
        }
        return Optional.of(value);
    }

    @NonNull
    public static TimeZone getDefaultTimeZone()
    {
        return TimeZone.getTimeZone("UTC");
    }

    @NonNull
    public static SimpleDateFormat getDefaultDateFormat()
    {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS z");

        format.setTimeZone(getDefaultTimeZone());

        return format;
    }

    @Nullable
    public static String format(@Nullable final Date date)
    {
        if (null != date)
        {
            return DEFAULT_DATE_FORMAT.get().format(date);
        }
        return NULL();
    }

    @NonNull
    public static String format(@NonNull final CharSequence format, @NonNull final Object... args)
    {
        return String.format(format.toString(), args);
    }

    @NonNull
    public static byte[] getBytes(@NonNull final CharSequence value)
    {
        return value.toString().getBytes(StandardCharsets.UTF_8);
    }

    @NonNull
    public static String uuid()
    {
        return UUID.randomUUID().toString();
    }

    @NonNull
    public static String UUID()
    {
        return uuid().toUpperCase();
    }

    @NonNull
    public static String requireToString(final CharSequence value)
    {
        final String chars = getCharSequence(value);

        if (null != chars)
        {
            return chars;
        }
        throw new NullPointerException();
    }

    @NonNull
    public static String toStringOrElse(final CharSequence value, @NonNull final String otherwise)
    {
        final String chars = getCharSequence(value);

        if (null != chars)
        {
            return chars;
        }
        return otherwise;
    }

    @NonNull
    public static String toStringOrElse(final CharSequence value, @NonNull final Supplier<String> otherwise)
    {
        final String chars = getCharSequence(value);

        if (null != chars)
        {
            return chars;
        }
        return requireNonNull(otherwise.get());
    }

    @Nullable
    public static String getETagSequence(@Nullable final CharSequence value)
    {
        final String string = getCharSequence(value);

        if (null != string)
        {
            return string.replace(QUOTE_STRING_VALUED, EMPTY_STRING_VALUED);
        }
        return string;
    }

    @NonNull
    public static String fixContentType(@Nullable final CharSequence value)
    {
        return toStringOrElse(value, getDefaultContentType());
    }

    @NonNull
    public static String fixRegionString(@Nullable final CharSequence value, @NonNull final String otherwise)
    {
        if ((null == value) || (value.length() < 1))
        {
            return otherwise;
        }
        final String string = requireToString(value).trim();

        if ((string.isEmpty()) || (DEFAULT_REGION_EAST.equalsIgnoreCase(string)))
        {
            return otherwise;
        }
        return string;
    }

    @Nullable
    public static String fixRegionString(@Nullable final CharSequence value)
    {
        if ((null == value) || (value.length() < 1))
        {
            return NULL();
        }
        final String string = requireToString(value).trim();

        if ((string.isEmpty()) || (DEFAULT_REGION_EAST.equalsIgnoreCase(string)))
        {
            return NULL();
        }
        return string;
    }

    @Nullable
    public static String failIfNullBytePresent(@Nullable final CharSequence value)
    {
        if (null != value)
        {
            final int size = value.length();

            for (int i = 0; i < size; i++)
            {
                if (value.charAt(i) == 0)
                {
                    throw new IllegalArgumentException("null byte present in string, there are no known legitimate use cases for such data, but several injection attacks may use it.");
                }
            }
            return value.toString();
        }
        return NULL();
    }

    @Nullable
    public static <T> T getResultNullable(@NonNull final Result<T> result)
    {
        try
        {
            return result.get();
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
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
    @SafeVarargs
    public static <T> ArrayList<T> toList(@NonNull final T... source)
    {
        return new ArrayList<>(Arrays.asList(requireNonNull(source)));
    }

    @NonNull
    public static <T> ArrayList<T> toList(@NonNull final Stream<T> source)
    {
        return new ArrayList<>(source.collect(Collectors.toList()));
    }

    @NonNull
    public static String getDefaultContentType()
    {
        return "application/octet-stream";
    }

    @Nullable
    public static String repeat(@Nullable final CharSequence string, final int times)
    {
        if (null == string)
        {
            return NULL();
        }
        if (times < 2)
        {
            return string.toString();
        }
        final int count = string.length();

        if (count < 1)
        {
            return string.toString();
        }
        final StringBuilder builder = new StringBuilder(count * times);

        for (int i = 0; i < times; i++)
        {
            builder.append(string);
        }
        return builder.toString();
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

    @NonNull
    public static Integer getDuration(@NonNull final Long time)
    {
        requireNonNull(time);

        if ((time < MINIMUM_EXPIRY_TIME) || (time > MAXIMUM_EXPIRY_TIME))
        {
            throw new IllegalArgumentException(format("bad duration %s", time));
        }
        return time.intValue();
    }

    @NonNull
    public static Integer getDuration(@NonNull final Duration time)
    {
        return getDuration(time.getSeconds());
    }

    @NonNull
    public static Integer getDuration(@NonNull final Long time, @NonNull final TimeUnit unit)
    {
        return getDuration(unit.toSeconds(time.longValue()));
    }

    @NonNull
    public static String toJSONString(@NonNull final Object value) throws MinioDataException
    {
        return toJSONString(requireNonNull(value), true);
    }

    @NonNull
    public static String toJSONString(@NonNull final Object value, final boolean pretty) throws MinioDataException
    {
        return new JSONObjectMapper(pretty).toJSONString(requireNonNull(value));
    }

    @NonNull
    public static <T> T toJSONObject(@NonNull final CharSequence value, @NonNull final Class<T> type) throws MinioDataException
    {
        return new JSONObjectMapper(false).toJSONObject(requireNonNull(value), requireNonNull(type));
    }

    @NonNull
    public static <T> T toJSONObject(@NonNull final InputStream value, @NonNull final Class<T> type) throws MinioDataException
    {
        return new JSONObjectMapper(false).toJSONObject(requireNonNull(value), requireNonNull(type));
    }

    @NonNull
    public static <T> T toJSONObject(@NonNull final Resource value, @NonNull final Class<T> type) throws MinioDataException
    {
        return new JSONObjectMapper(false).toJSONObject(requireNonNull(value), requireNonNull(type));
    }
}
