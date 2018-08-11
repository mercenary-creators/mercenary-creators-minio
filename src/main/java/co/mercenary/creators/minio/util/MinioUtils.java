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

import static com.fasterxml.jackson.core.JsonGenerator.Feature.AUTO_CLOSE_TARGET;
import static com.fasterxml.jackson.core.JsonGenerator.Feature.ESCAPE_NON_ASCII;
import static com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN;
import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS;
import static com.fasterxml.jackson.core.JsonParser.Feature.AUTO_CLOSE_SOURCE;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.xmlpull.v1.XmlPullParserException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import co.mercenary.creators.minio.errors.MinioOperationException;
import io.minio.Result;
import io.minio.errors.MinioException;

public final class MinioUtils
{
    @NonNull
    public static final Long MINIMUM_EXPIRY_TIME = 1L;

    @NonNull
    public static final Long MAXIMUM_EXPIRY_TIME = 7L * 24L * 3600L;

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

    public static void testAllNonNull(@NonNull final Object... values)
    {
        for (final Object value : values)
        {
            requireNonNull(value);
        }
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
    public static String uuid()
    {
        return UUID.randomUUID().toString();
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

    @NonNull
    public static String fixContentType(@Nullable final CharSequence value)
    {
        return requireToStringOrElse(value, xgetDefaultContentType());
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
    public static String getJSONContentType()
    {
        return "application/json";
    }

    @NonNull
    public static String xgetDefaultContentType()
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

    @NonNull
    public static Integer getDuration(@NonNull final Long time) throws MinioOperationException
    {
        requireNonNull(time);

        if ((time < MINIMUM_EXPIRY_TIME) || (time > MAXIMUM_EXPIRY_TIME))
        {
            throw new MinioOperationException(String.format("bad duration %s", time));
        }
        return new Integer(time.intValue());
    }

    @NonNull
    public static Integer getDuration(@NonNull final Duration time) throws MinioOperationException
    {
        return getDuration(time.getSeconds());
    }

    @NonNull
    public static Integer getDuration(@NonNull final Long time, @NonNull final TimeUnit unit) throws MinioOperationException
    {
        return getDuration(unit.toSeconds(time.longValue()));
    }

    @NonNull
    public static String toJSONString(@NonNull final Object value) throws MinioOperationException
    {
        return JSONObjectMapper.instance().toJSONString(requireNonNull(value));
    }

    @NonNull
    public static <T> T toJSONObject(@NonNull final CharSequence value, @NonNull final Class<T> type) throws MinioOperationException
    {
        return JSONObjectMapper.instance().toJSONObject(requireNonNull(value), requireNonNull(type));
    }

    @NonNull
    public static <T> T toJSONObject(@NonNull final InputStream value, @NonNull final Class<T> type) throws MinioOperationException
    {
        return JSONObjectMapper.instance().toJSONObject(requireNonNull(value), requireNonNull(type));
    }

    public static class JSONObjectMapper extends ObjectMapper
    {
        private static final long              serialVersionUID = 7742077499646363644L;

        @NonNull
        private static final JSONObjectMapper  JSONOBJECTMAPPER = new JSONObjectMapper();

        @NonNull
        private static final ArrayList<Module> EXTENDED_MODULES = toList(new JodaModule(), new ParameterNamesModule(), new Jdk8Module(), new JavaTimeModule());

        @NonNull
        public static JSONObjectMapper instance()
        {
            return JSONOBJECTMAPPER;
        }

        public JSONObjectMapper()
        {
            registerModules(EXTENDED_MODULES).enable(ALLOW_COMMENTS).enable(ESCAPE_NON_ASCII).disable(AUTO_CLOSE_SOURCE).disable(AUTO_CLOSE_TARGET).disable(FAIL_ON_UNKNOWN_PROPERTIES).enable(WRITE_BIGDECIMAL_AS_PLAIN);
        }

        protected JSONObjectMapper(@NonNull final JSONObjectMapper parent)
        {
            super(requireNonNull(parent));
        }

        @NonNull
        public String toJSONString(@NonNull final Object value) throws MinioOperationException
        {
            try
            {
                return requireNonNull(writeValueAsString(requireNonNull(value)));
            }
            catch (final JsonProcessingException e)
            {
                throw new MinioOperationException(e);
            }
        }

        @NonNull
        public <T> T toJSONObject(@NonNull final CharSequence value, @NonNull final Class<T> type) throws MinioOperationException
        {
            requireNonNull(value);

            try
            {
                return requireNonNull(readerFor(requireNonNull(type)).readValue(value.toString()));
            }
            catch (final IOException e)
            {
                throw new MinioOperationException(e);
            }
        }

        @NonNull
        public <T> T toJSONObject(@NonNull final InputStream value, @NonNull final Class<T> type) throws MinioOperationException
        {
            requireNonNull(value);

            try
            {
                return requireNonNull(readerFor(requireNonNull(type)).readValue(value));
            }
            catch (final IOException e)
            {
                throw new MinioOperationException(e);
            }
        }

        @NonNull
        @Override
        public JSONObjectMapper copy()
        {
            _checkInvalidCopy(JSONObjectMapper.class);

            return new JSONObjectMapper(this);
        }
    }
}
