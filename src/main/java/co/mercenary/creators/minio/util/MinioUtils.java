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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ClassUtils;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.xmlpull.v1.XmlPullParserException;

import io.minio.Result;
import io.minio.errors.MinioException;

public final class MinioUtils
{
    @NonNull
    public static final Long                      MINIMUM_EXPIRY_TIME = 1L;

    @NonNull
    public static final Long                      MAXIMUM_EXPIRY_TIME = 7L * 24L * 3600L;

    @NonNull
    public static final Long                      MAXINUM_OBJECT_SIZE = 5L * 1024 * 1024 * 1024 * 1024;

    @NonNull
    public static final String                    EMPTY_STRING_VALUED = "";

    @NonNull
    public static final String                    SPACE_STRING_VALUED = " ";

    @NonNull
    public static final String                    PATH_SEPARATOR_CHAR = "/";

    @NonNull
    public static final String                    QUOTE_STRING_VALUED = "\"";

    @NonNull
    public static final String                    NULLS_STRING_VALUED = "null";

    @NonNull
    public static final String                    FALSE_STRING_VALUED = "false";

    @NonNull
    public static final String                    DEFAULT_REGION_EAST = "us-east-1";

    @NonNull
    public static final String                    X_AMAZON_META_START = "x-amz-meta-";

    @NonNull
    public static final String                    AMAZON_S3_END_POINT = "s3.amazonaws.com";

    @NonNull
    public static final TimeZone                  MINIO_TIME_ZONE_UTC = TimeZone.getTimeZone("UTC");

    @NonNull
    public static final PathMatcher               GLOBAL_PATH_MATCHER = new AntPathMatcher(PATH_SEPARATOR_CHAR);

    @NonNull
    public static final ThreadLocal<DateFormat>   DEFAULT_DATE_FORMAT = ThreadLocal.withInitial(MinioUtils::getDefaultDateFormat);

    @NonNull
    public static final ThreadLocal<NumberFormat> DECIMAL_FORMAT_TO_3 = ThreadLocal.withInitial(() -> new DecimalFormat("#.000"));

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

    public static boolean isNull(@Nullable final Object value)
    {
        return (null == value);
    }

    public static boolean isNonNull(@Nullable final Object value)
    {
        return (null != value);
    }

    public static boolean isPresent(@NonNull final String name)
    {
        return isPresent(name, NULL());
    }

    public static boolean isPresent(@NonNull final String name, @Nullable final ClassLoader loader)
    {
        return ClassUtils.isPresent(requireNonNull(name), loader);
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

    public static void isEachNonNull(@NonNull final Object... values)
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

    @NonNull
    public static <T> Optional<T> toMaybeNonNull(@NonNull final Supplier<T> supplier)
    {
        return toOptional(toValueNonNull(supplier));
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
        return MINIO_TIME_ZONE_UTC;
    }

    @NonNull
    public static DateFormat getDefaultDateFormat()
    {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS z");

        format.setTimeZone(getDefaultTimeZone());

        return format;
    }

    @Nullable
    public static String format(@NonNull final Optional<Date> date)
    {
        if (date.isPresent())
        {
            return DEFAULT_DATE_FORMAT.get().format(date.get());
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
    public static String toStorageClass(@Nullable final CharSequence value)
    {
        final String string = getCharSequence(value);

        if (null != string)
        {
            return string.toUpperCase();
        }
        return "STANDARD";
    }

    @NonNull
    public static String fixBuckString(@NonNull final CharSequence buck)
    {
        return buck.toString();
    }

    @NonNull
    public static String fixPathString(@NonNull final CharSequence path)
    {
        return StringUtils.cleanPath(path.toString());
    }

    @NonNull
    public static String getPathRelative(@NonNull final CharSequence base, @NonNull final CharSequence path)
    {
        return fixPathString(StringUtils.applyRelativePath(requireToString(base), requireToString(path)));
    }

    @NonNull
    public static String fixContentType(@Nullable final CharSequence value)
    {
        return toStringOrElse(value, getDefaultContentType());
    }

    @Nullable
    public static String fixRegionString(@Nullable final CharSequence value, final boolean amazon)
    {
        if ((null == value) || (value.length() < 1))
        {
            return amazon ? DEFAULT_REGION_EAST : NULL();
        }
        final String string = requireToString(value).trim();

        if ((string.isEmpty()) || (DEFAULT_REGION_EAST.equalsIgnoreCase(string)))
        {
            return amazon ? DEFAULT_REGION_EAST : NULL();
        }
        return string;
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

    public static boolean isAmazonEndpoint(@NonNull final String value)
    {
        return value.toLowerCase().indexOf(AMAZON_S3_END_POINT) >= 0;
    }

    public static boolean isAmazonMetaPrefix(@NonNull final String value)
    {
        return ((value.regionMatches(true, 0, X_AMAZON_META_START, 0, X_AMAZON_META_START.length())) && (false == value.equalsIgnoreCase(X_AMAZON_META_START)));
    }

    @NonNull
    public static String noAmazonMetaPrefix(@NonNull final String value)
    {
        if (isAmazonMetaPrefix(value))
        {
            return value.substring(X_AMAZON_META_START.length());
        }
        return value;
    }

    @NonNull
    public static String toAmazonMetaPrefix(@NonNull final String value)
    {
        return X_AMAZON_META_START + noAmazonMetaPrefix(value);
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

    @Nullable
    public static <T> T toZero(final List<T> source)
    {
        if ((null == source) || (source.isEmpty()))
        {
            return NULL();
        }
        return source.get(0);
    }

    @NonNull
    public static <T> Stream<T> getResultAsStream(@NonNull final Iterable<Result<T>> iterable)
    {
        return StreamSupport.stream(iterable.spliterator(), false).map(result -> getResultNullable(result)).filter(MinioUtils::isNonNull);
    }

    @NonNull
    @SafeVarargs
    public static <T> List<T> toList(@NonNull final T... source)
    {
        return Arrays.asList(requireNonNull(source));
    }

    @NonNull
    public static <T> List<T> toList(@NonNull final Stream<T> source)
    {
        return source.collect(Collectors.toList());
    }

    @NonNull
    public static <T> List<T> emptyList()
    {
        return Collections.emptyList();
    }

    @NonNull
    public static <T> List<T> emptyList(final Class<T> type)
    {
        return Collections.emptyList();
    }

    @NonNull
    public static <K, V> Map<K, V> emptyMap()
    {
        return Collections.emptyMap();
    }

    @NonNull
    public static <K, V> Map<K, V> toUnmodifiable(@Nullable final Map<K, V> map)
    {
        if ((null == map) || (map.isEmpty()))
        {
            return emptyMap();
        }
        return Collections.unmodifiableMap(map);
    }

    @NonNull
    public static <K, V> LinkedHashMap<K, V> toLinkedHashMap(@Nullable final Map<K, V> map, final boolean fix)
    {
        final LinkedHashMap<K, V> tmp = new LinkedHashMap<>();

        if ((false == fix) || (null == map) || (map.isEmpty()))
        {
            return tmp;
        }
        map.forEach((k, v) -> {

            if ((null != k) && (null != v))
            {
                tmp.put(k, v);
            }
        });
        return tmp;
    }

    @NonNull
    public static <T> Collection<T> keys(@NonNull final Map<T, ?> map)
    {
        return Collections.unmodifiableCollection(map.keySet());
    }

    @NonNull
    public static String getTEXTContentType()
    {
        return "text/plain";
    }

    @NonNull
    public static String getHTMLContentType()
    {
        return "text/html";
    }

    @NonNull
    public static String getYAMLContentType()
    {
        return "text/x-yaml";
    }

    @NonNull
    public static String getJAVAContentType()
    {
        return "text/x-java-source";
    }

    @NonNull
    public static String getJSONContentType()
    {
        return "application/json";
    }

    @NonNull
    public static String getPROPContentType()
    {
        return "text/x-java-properties";
    }

    @NonNull
    public static String getDefaultContentType()
    {
        return "application/octet-stream";
    }

    @Nullable
    public static String getContentTypeCommon(@Nullable final CharSequence name)
    {
        if ((null == name) || (name.length() < 5))
        {
            return NULL();
        }
        final String path = name.toString();

        if (path.endsWith(".json"))
        {
            return getJSONContentType();
        }
        if (path.endsWith(".html") || path.endsWith(".htm"))
        {
            return getHTMLContentType();
        }
        if (path.endsWith(".txt") || path.endsWith(".text"))
        {
            return getTEXTContentType();
        }
        if (path.endsWith(".java"))
        {
            return getJAVAContentType();
        }
        if (path.endsWith(".properties"))
        {
            return getPROPContentType();
        }
        if (path.endsWith(".yml") || path.endsWith(".yaml"))
        {
            return getYAMLContentType();
        }
        return NULL();
    }

    @NonNull
    public static String repeat(@Nullable final CharSequence string, final int times)
    {
        if (null == string)
        {
            return EMPTY_STRING_VALUED;
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

    public static boolean isValidToRead(@NonNull final File file) throws IOException
    {
        return isValidToRead(file.toPath());
    }

    public static boolean isValidToRead(@NonNull final Path path) throws IOException
    {
        requireNonNull(path);

        return ((path.toFile().exists()) && (Files.isReadable(path)) && (path.toFile().isFile()) && (false == Files.isHidden(path)));
    }

    @NonNull
    public static InputStream getInputStream(@NonNull final File file) throws IOException
    {
        return getInputStream(file.toPath());
    }

    @NonNull
    public static InputStream getInputStream(@NonNull final Path path) throws IOException
    {
        if (isValidToRead(requireNonNull(path)))
        {
            return Files.newInputStream(path);
        }
        throw new IOException(path.toString());
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

    public static long getCurrentMills()
    {
        return System.currentTimeMillis();
    }

    public static long getCurrentNanos()
    {
        return System.nanoTime();
    }
}
