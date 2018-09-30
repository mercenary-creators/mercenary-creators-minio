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
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import co.mercenary.creators.minio.errors.MinioDataException;

public class JSONObjectMapper extends ObjectMapper
{
    private static final long              serialVersionUID = 7742077499646363644L;

    @NonNull
    private static final PrettyPrinter     TO_PRETTY_PRINTS = toDefaultPrettyPrinter(MinioUtils.repeat(MinioUtils.SPACE_STRING_VALUED, 4));

    @NonNull
    private static final ArrayList<Module> EXTENDED_MODULES = MinioUtils.toList(new JodaModule(), new Jdk8Module(), new JavaTimeModule());

    @NonNull
    private static PrettyPrinter toDefaultPrettyPrinter(final String indent)
    {
        return new DefaultPrettyPrinter().withArrayIndenter(new DefaultIndenter().withIndent(indent)).withObjectIndenter(new DefaultIndenter().withIndent(indent));
    }

    public JSONObjectMapper(final boolean pretty)
    {
        registerModules(EXTENDED_MODULES).enable(ALLOW_COMMENTS).enable(ESCAPE_NON_ASCII).disable(AUTO_CLOSE_SOURCE).disable(AUTO_CLOSE_TARGET).disable(FAIL_ON_UNKNOWN_PROPERTIES).enable(WRITE_BIGDECIMAL_AS_PLAIN);

        setDateFormat(MinioUtils.getDefaultDateFormat()).setTimeZone(MinioUtils.getDefaultTimeZone());

        if (pretty)
        {
            setDefaultPrettyPrinter(TO_PRETTY_PRINTS).enable(INDENT_OUTPUT);
        }
    }

    protected JSONObjectMapper(@NonNull final JSONObjectMapper parent)
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
    public <T> T toJSONObject(@NonNull final CharSequence value, @NonNull final Class<T> type) throws MinioDataException
    {
        MinioUtils.isEachNonNull(value, type);

        try
        {
            return MinioUtils.requireNonNull(readerFor(type).readValue(value.toString()));
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

    public boolean canSerializeClass(@Nullable final Class<?> type)
    {
        if (null != type)
        {
            return canSerialize(type);
        }
        return false;
    }

    public boolean canSerializeValue(@Nullable final Object object)
    {
        if (null != object)
        {
            return canSerializeClass(object.getClass());
        }
        return false;
    }

    @NonNull
    @Override
    public JSONObjectMapper copy()
    {
        _checkInvalidCopy(JSONObjectMapper.class);

        return new JSONObjectMapper(this);
    }
}