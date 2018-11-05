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
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
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
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.xmlpull.v1.XmlPullParserException;

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
    public static final String                  FALSE_STRING_VALUED = "false";

    @NonNull
    public static final String                  DEFAULT_REGION_EAST = "us-east-1";

    @NonNull
    public static final String                  X_AMAZON_META_START = "x-amz-meta-";

    @NonNull
    public static final String                  AMAZON_S3_END_POINT = "s3.amazonaws.com";

    @NonNull
    public static final TimeZone                MINIO_TIME_ZONE_UTC = TimeZone.getTimeZone("UTC");

    @NonNull
    public static final PathMatcher             GLOBAL_PATH_MATCHER = new AntPathMatcher(PATH_SEPARATOR_CHAR);

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
        requireNonNull(type);

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
        requireNonNull(type);

        return ((T) requireNonNull(value));
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

    public static void isEachNonNull(@NonNull final Object... values)
    {
        requireNonNull(values, "values is null.");

        int i = 0;

        for (final Object value : values)
        {
            final int a = i++;

            requireNonNull(value, () -> String.format("isEachNonNull(): values[%s] is null.", a));
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

    @NonNull
    public static String toStringOrElse(final String value, @NonNull final String otherwise)
    {
        return toStringOrElse(value, () -> otherwise);
    }

    @NonNull
    public static String toStringOrElse(final String value, @NonNull final Supplier<String> otherwise)
    {
        if (null != value)
        {
            return value;
        }
        return otherwise.get();
    }

    @Nullable
    public static String toETagSequence(@Nullable final String value)
    {
        if (null != value)
        {
            return value.replace(QUOTE_STRING_VALUED, EMPTY_STRING_VALUED);
        }
        return NULL();
    }

    @Nullable
    public static String toStorageClass(@Nullable final String value)
    {
        if (null != value)
        {
            return value.toUpperCase();
        }
        return NULL();
    }

    @NonNull
    public static String fixPathString(@NonNull final String path)
    {
        return StringUtils.cleanPath(requireNonNull(path));
    }

    @NonNull
    public static String getPathRelative(@NonNull final String base, @NonNull final String path)
    {
        return fixPathString(StringUtils.applyRelativePath(requireNonNull(base), requireNonNull(path)));
    }

    @NonNull
    public static String fixContentType(@Nullable final String value)
    {
        return toStringOrElse(value, getDefaultContentType());
    }

    @Nullable
    public static String fixRegionString(@Nullable final String value, final boolean amazon)
    {
        if ((null == value) || (value.length() < 1))
        {
            return amazon ? DEFAULT_REGION_EAST : NULL();
        }
        final String string = value.trim();

        if ((string.isEmpty()) || (DEFAULT_REGION_EAST.equalsIgnoreCase(string)))
        {
            return amazon ? DEFAULT_REGION_EAST : NULL();
        }
        return string;
    }

    @NonNull
    public static String fixServerString(@NonNull final String value)
    {
        return requireNonNull(value);
    }

    @NonNull
    public static String fixRegionString(@Nullable final String value, @NonNull final String otherwise)
    {
        if ((null == value) || (value.length() < 1))
        {
            return otherwise;
        }
        final String string = value.trim();

        if ((string.isEmpty()) || (DEFAULT_REGION_EAST.equalsIgnoreCase(string)))
        {
            return otherwise;
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
    public static <K, V> LinkedHashMap<K, V> toLinkedHashMap(@Nullable final Map<K, V> map)
    {
        if (null == map)
        {
            return new LinkedHashMap<>();
        }
        return new LinkedHashMap<>(map);
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
    public static <K, V> Map<K, V> toMap(@NonNull final K key, @Nullable final V val)
    {
        return new LinkedHashMap<>(Collections.singletonMap(requireNonNull(key), val));
    }

    @NonNull
    public static Map<String, String> toHeaderMap(@Nullable final WithUserMetaData meta)
    {
        if (null == meta)
        {
            return new LinkedHashMap<>();
        }
        return new LinkedHashMap<>(meta.getUserMetaData());
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
    public static String getContentTypeCommon(@Nullable final String name)
    {
        if ((null == name) || (name.length() < 5))
        {
            return NULL();
        }
        if (name.endsWith(".json"))
        {
            return getJSONContentType();
        }
        if (name.endsWith(".html") || name.endsWith(".htm"))
        {
            return getHTMLContentType();
        }
        if (name.endsWith(".txt") || name.endsWith(".text"))
        {
            return getTEXTContentType();
        }
        if (name.endsWith(".java"))
        {
            return getJAVAContentType();
        }
        if (name.endsWith(".properties"))
        {
            return getPROPContentType();
        }
        if (name.endsWith(".yml") || name.endsWith(".yaml"))
        {
            return getYAMLContentType();
        }
        return NULL();
    }

    @NonNull
    public static String repeat(@Nullable final String string, final int times)
    {
        if (null == string)
        {
            return EMPTY_STRING_VALUED;
        }
        if (times < 2)
        {
            return string;
        }
        final int count = string.length();

        if (count < 1)
        {
            return string;
        }
        final StringBuilder builder = new StringBuilder(count * times);

        for (int i = 0; i < times; i++)
        {
            builder.append(string);
        }
        return builder.toString();
    }

    @NonNull
    public static String uuid()
    {
        return UUID.randomUUID().toString();
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
            throw new IllegalArgumentException(String.format("bad duration %s", time));
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

    public static long getCurrentNanos()
    {
        return System.nanoTime();
    }
}
