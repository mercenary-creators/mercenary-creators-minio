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

import static com.fasterxml.jackson.core.JsonGenerator.Feature.AUTO_CLOSE_TARGET;
import static com.fasterxml.jackson.core.JsonGenerator.Feature.ESCAPE_NON_ASCII;
import static com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN;
import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS;
import static com.fasterxml.jackson.core.JsonParser.Feature.AUTO_CLOSE_SOURCE;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Path;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import co.mercenary.creators.minio.errors.MinioDataException;
import co.mercenary.creators.minio.util.MinioUtils;

public class JSONMapper extends ObjectMapper
{
    private static final long          serialVersionUID = 7742077499646363644L;

    @NonNull
    private static final PrettyPrinter TO_PRETTY_PRINTS = toDefaultPrettyPrints(MinioUtils.repeat(MinioUtils.SPACE_STRING_VALUED, 4));

    @NonNull
    private static PrettyPrinter toDefaultPrettyPrints(@NonNull final String indent)
    {
        return new DefaultPrettyPrinter().withArrayIndenter(new DefaultIndenter().withIndent(indent)).withObjectIndenter(new DefaultIndenter().withIndent(indent));
    }

    public JSONMapper()
    {
        this(false);
    }

    public JSONMapper(final boolean pretty)
    {
        registerModules(JSONMapperModules.EXTENDED_MODULES).enable(ALLOW_COMMENTS).enable(ESCAPE_NON_ASCII).disable(AUTO_CLOSE_SOURCE).disable(AUTO_CLOSE_TARGET).disable(FAIL_ON_UNKNOWN_PROPERTIES).enable(WRITE_BIGDECIMAL_AS_PLAIN);

        setDateFormat(MinioUtils.getDefaultDateFormat()).setTimeZone(MinioUtils.getDefaultTimeZone());

        if (pretty)
        {
            setDefaultPrettyPrinter(TO_PRETTY_PRINTS).enable(INDENT_OUTPUT);
        }
    }

    protected JSONMapper(@NonNull final JSONMapper parent)
    {
        super(MinioUtils.requireNonNull(parent));
    }

    @NonNull
    public String toJSONString(@NonNull final Object value) throws MinioDataException
    {
        MinioUtils.isEachNonNull(value);

        try
        {
            return MinioUtils.requireNonNull(writeValueAsString(value));
        }
        catch (final JsonProcessingException e)
        {
            throw new MinioDataException(e);
        }
    }

    @NonNull
    public byte[] toByteArray(@NonNull final Object value) throws MinioDataException
    {
        MinioUtils.isEachNonNull(value);

        try
        {
            return MinioUtils.requireNonNull(writeValueAsBytes(value));
        }
        catch (final JsonProcessingException e)
        {
            throw new MinioDataException(e);
        }
    }

    @Nullable
    public <T> T convert(@Nullable final Object value, @NonNull final Class<T> type) throws MinioDataException
    {
        if (null == value)
        {
            return MinioUtils.NULL();
        }
        if (ClassUtils.isAssignableValue(type, value))
        {
            return type.cast(value);
        }
        try
        {
            return super.convertValue(value, type);
        }
        catch (final IllegalArgumentException e)
        {
            throw new MinioDataException(e);
        }
    }

    @NonNull
    public <T> T toJSONObject(@NonNull final byte[] value, @NonNull final Class<T> type) throws MinioDataException
    {
        MinioUtils.isEachNonNull(value, type);

        try
        {
            return MinioUtils.requireNonNull(readerFor(type).readValue(value));
        }
        catch (final IOException e)
        {
            throw new MinioDataException(e);
        }
    }

    @NonNull
    public <T> T toJSONObject(@NonNull final String value, @NonNull final Class<T> type) throws MinioDataException
    {
        MinioUtils.isEachNonNull(value, type);

        try
        {
            return MinioUtils.requireNonNull(readerFor(type).readValue(value));
        }
        catch (final IOException e)
        {
            throw new MinioDataException(e);
        }
    }

    @NonNull
    public <T> T toJSONObject(@NonNull final Reader value, @NonNull final Class<T> type) throws MinioDataException
    {
        MinioUtils.isEachNonNull(value, type);

        try
        {
            return MinioUtils.requireNonNull(readerFor(type).readValue(value));
        }
        catch (final IOException e)
        {
            throw new MinioDataException(e);
        }
    }

    @NonNull
    public <T> T toJSONObject(@NonNull final InputStream value, @NonNull final Class<T> type) throws MinioDataException
    {
        MinioUtils.isEachNonNull(value, type);

        try
        {
            return MinioUtils.requireNonNull(readerFor(type).readValue(value));
        }
        catch (final IOException e)
        {
            throw new MinioDataException(e);
        }
    }

    @NonNull
    public <T> T toJSONObject(@NonNull final Resource value, @NonNull final Class<T> type) throws MinioDataException
    {
        MinioUtils.isEachNonNull(value, type);

        try (final InputStream stream = value.getInputStream())
        {
            return MinioUtils.requireNonNull(readerFor(type).readValue(stream));
        }
        catch (final IOException e)
        {
            throw new MinioDataException(e);
        }
    }

    @NonNull
    public <T> T toJSONObject(@NonNull final File value, @NonNull final Class<T> type) throws MinioDataException
    {
        MinioUtils.isEachNonNull(value, type);

        try (final InputStream stream = MinioUtils.getInputStream(value))
        {
            return MinioUtils.requireNonNull(readerFor(type).readValue(stream));
        }
        catch (final IOException e)
        {
            throw new MinioDataException(e);
        }
    }

    @NonNull
    public <T> T toJSONObject(@NonNull final Path value, @NonNull final Class<T> type) throws MinioDataException
    {
        MinioUtils.isEachNonNull(value, type);

        try (final InputStream stream = MinioUtils.getInputStream(value))
        {
            return MinioUtils.requireNonNull(readerFor(type).readValue(stream));
        }
        catch (final IOException e)
        {
            throw new MinioDataException(e);
        }
    }

    @Override
    public boolean canSerialize(@Nullable final Class<?> type)
    {
        if (null != type)
        {
            return super.canSerialize(type);
        }
        return false;
    }

    public boolean canSerializeObject(@Nullable final Object object)
    {
        if (null != object)
        {
            if (object instanceof Class)
            {
                return canSerialize(MinioUtils.CAST(object));
            }
            else
            {
                return super.canSerialize(object.getClass());
            }
        }
        return false;
    }

    @NonNull
    @Override
    public JSONMapper copy()
    {
        _checkInvalidCopy(JSONMapper.class);

        return new JSONMapper(this);
    }

    private static final class JSONMapperModules
    {
        @NonNull
        private static final List<Module> EXTENDED_MODULES = MinioUtils.toList(new ParameterNamesModule(), new JodaModule(), new Jdk8Module(), new JavaTimeModule(), new KotlinModule());
    }
}