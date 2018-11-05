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

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import co.mercenary.creators.minio.MinioOperations;
import co.mercenary.creators.minio.MinioTemplate;
import co.mercenary.creators.minio.MinioTestConfig;
import co.mercenary.creators.minio.errors.MinioDataException;
import co.mercenary.creators.minio.json.JSONUtils;
import co.mercenary.creators.minio.json.WithJSONOperations;

@SpringJUnitConfig(MinioTestConfig.class)
@TestPropertySource("file:/opt/development/properties/mercenary-creators-minio/minio-test.properties")
public abstract class AbstractMinioTests
{
    @NonNull
    private final Ticker  ticker;

    @NonNull
    private final Marker  marker = LoggingOps.getMarker();

    @NonNull
    private final Logger  logger = LoggingOps.getLogger(getClass());

    @Nullable
    @Autowired
    private MinioTemplate minioTemplate;

    protected AbstractMinioTests()
    {
        super();

        ticker = getTimer();
    }

    @NonNull
    protected Optional<MinioTemplate> getTemplate()
    {
        return MinioUtils.toOptional(minioTemplate);
    }

    @NonNull
    protected MinioOperations getMinio()
    {
        return getTemplate().orElseThrow(() -> new AssertionError("getTemplate() null template."));
    }

    @NonNull
    protected MinioOperations getOperations()
    {
        return getTemplate().orElseThrow(() -> new AssertionError("getTemplate() null template."));
    }

    @NonNull
    protected Logger getLogger()
    {
        return logger;
    }

    @NonNull
    protected Marker getMarker()
    {
        return marker;
    }

    @NonNull
    protected Ticker getTimer()
    {
        return new NanoTicker();
    }

    @BeforeEach
    protected void doBeforeEachTest()
    {
        info(() -> getMinio().getContentTypeProbe().getClass().getName());

        ticker.reset();
    }

    @AfterEach
    protected void doAfterEachTest()
    {
        final String tick = ticker.toString();

        info(() -> tick);

        ticker.reset();
    }

    @NonNull
    protected Supplier<String> getMessage(@NonNull final String message)
    {
        return () -> message;
    }

    @NonNull
    protected Supplier<String> isEmptyMessage(@NonNull final String message)
    {
        return isEmptyMessage(() -> message);
    }

    @NonNull
    protected Supplier<String> isEmptyMessage(@NonNull final Supplier<String> message)
    {
        return () -> message.get() + " is empty.";
    }

    protected void info(@NonNull final Supplier<?> message)
    {
        if (getLogger().isInfoEnabled())
        {
            getLogger().info(getMarker(), safe(message.get()));
        }
    }

    protected void info(@NonNull final Supplier<?> message, @NonNull final Throwable cause)
    {
        if (getLogger().isInfoEnabled())
        {
            getLogger().info(getMarker(), safe(message.get()), cause);
        }
    }

    protected void warn(@NonNull final Supplier<?> message)
    {
        if (getLogger().isWarnEnabled())
        {
            getLogger().warn(getMarker(), safe(message.get()));
        }
    }

    protected void warn(@NonNull final Supplier<?> message, @NonNull final Throwable cause)
    {
        if (getLogger().isWarnEnabled())
        {
            getLogger().warn(getMarker(), safe(message.get()), cause);
        }
    }

    protected void debug(@NonNull final Supplier<?> message)
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug(getMarker(), safe(message.get()));
        }
    }

    protected void debug(@NonNull final Supplier<?> message, @NonNull final Throwable cause)
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug(getMarker(), safe(message.get()), cause);
        }
    }

    protected void error(@NonNull final Supplier<?> message)
    {
        if (getLogger().isErrorEnabled())
        {
            getLogger().error(getMarker(), safe(message.get()));
        }
    }

    protected void error(@NonNull final Supplier<?> message, @NonNull final Throwable cause)
    {
        if (getLogger().isErrorEnabled())
        {
            getLogger().error(getMarker(), safe(message.get()), cause);
        }
    }

    @NonNull
    protected <T> List<T> forInfo(@NonNull final List<T> list)
    {
        if (list.size() > 0)
        {
            final Logger logs = getLogger();

            if (logs.isInfoEnabled())
            {
                final Marker mark = getMarker();

                list.forEach(value -> logs.info(mark, safe(value)));
            }
        }
        return list;
    }

    @NonNull
    protected <T> List<T> forInfo(@NonNull final Stream<T> source)
    {
        return forInfo(toList(source.filter(MinioUtils::isNonNull)));
    }

    @NonNull
    protected String safe(@Nullable final Object value)
    {
        if (value instanceof WithJSONOperations)
        {
            return toJSONString(value, true);
        }
        return MinioUtils.requireNonNullOrElse(((null == value) ? MinioUtils.NULLS_STRING_VALUED : value.toString()), MinioUtils.NULLS_STRING_VALUED);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    protected <T> List<T> toList(@NonNull final T... source)
    {
        return MinioUtils.toList(source);
    }

    @NonNull
    protected <T> List<T> toList(@NonNull final Stream<T> source)
    {
        return MinioUtils.toList(source);
    }

    @NonNull
    protected String toJSONString(@NonNull final Object value)
    {
        return toJSONString(value, true);
    }

    @NonNull
    protected String toJSONString(@NonNull final Object value, final boolean pretty)
    {
        try
        {
            return JSONUtils.toJSONString(value, pretty);
        }
        catch (final MinioDataException e)
        {
            throw new AssertionError("toJSONString", e);
        }
    }

    @NonNull
    protected <T> T toJSONObject(@NonNull final String value, @NonNull final Class<T> type)
    {
        try
        {
            return JSONUtils.toJSONObject(value, type);
        }
        catch (final MinioDataException e)
        {
            throw new AssertionError("toJSONObject", e);
        }
    }

    @NonNull
    protected String uuid()
    {
        return MinioUtils.uuid();
    }

    protected void assertTrue(final boolean condition, @NonNull final Supplier<String> message)
    {
        Assertions.assertTrue(condition, message);
    }

    protected void assertFalse(final boolean condition, @NonNull final Supplier<String> message)
    {
        Assertions.assertFalse(condition, message);
    }

    protected void assertEquals(@Nullable final Object expected, @Nullable final Object actual, @NonNull final Supplier<String> message)
    {
        Assertions.assertEquals(expected, actual, message);
    }

    @NonNull
    protected Resource getResource()
    {
        return new ClassPathResource("management.json", AbstractMinioTests.class);
    }
}
