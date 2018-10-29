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
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
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

@SpringJUnitConfig(MinioTestConfig.class)
@TestPropertySource("file:/opt/development/properties/mercenary-creators-minio/minio-test.properties")
public abstract class AbstractMinioTests
{
    @NonNull
    private final Logger  logger = LoggingOps.getLogger(getClass());

    @Nullable
    @Autowired
    private MinioTemplate minioTemplate;

    @Nullable
    protected MinioTemplate getMinioTemplate()
    {
        return minioTemplate;
    }

    @NonNull
    protected MinioOperations getOperations()
    {
        return getMinioTemplate();
    }

    @NonNull
    protected MinioOperations getMinio()
    {
        return getMinioTemplate();
    }

    @NonNull
    protected Resource getResource()
    {
        return new ClassPathResource("management.json", AbstractMinioTests.class);
    }

    @NonNull
    protected Logger getLogger()
    {
        return logger;
    }

    @NonNull
    protected Ticker getTicker()
    {
        return new NanoTicker();
    }

    @NonNull
    protected String uuid()
    {
        return UUID.randomUUID().toString();
    }

    @BeforeEach
    protected void doBeforeEachTest()
    {
        info(() -> getOperations().getContentTypeProbe().getClass().getName());
    }

    @AfterEach
    protected void doAfterEachTest()
    {
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
            getLogger().info(LoggingOps.MERCENARY_MARKER, message.get().toString());
        }
    }

    protected void info(@NonNull final Supplier<?> message, @NonNull final Throwable cause)
    {
        if (getLogger().isInfoEnabled())
        {
            getLogger().info(LoggingOps.MERCENARY_MARKER, message.get().toString(), cause);
        }
    }

    protected void warn(@NonNull final Supplier<?> message)
    {
        if (getLogger().isWarnEnabled())
        {
            getLogger().warn(LoggingOps.MERCENARY_MARKER, message.get().toString());
        }
    }

    protected void warn(@NonNull final Supplier<?> message, @NonNull final Throwable cause)
    {
        if (getLogger().isWarnEnabled())
        {
            getLogger().warn(LoggingOps.MERCENARY_MARKER, message.get().toString(), cause);
        }
    }

    protected void debug(@NonNull final Supplier<?> message)
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug(LoggingOps.MERCENARY_MARKER, message.get().toString());
        }
    }

    protected void debug(@NonNull final Supplier<?> message, @NonNull final Throwable cause)
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug(LoggingOps.MERCENARY_MARKER, message.get().toString(), cause);
        }
    }

    protected void error(@NonNull final Supplier<?> message)
    {
        if (getLogger().isErrorEnabled())
        {
            getLogger().error(LoggingOps.MERCENARY_MARKER, message.get().toString());
        }
    }

    protected void error(@NonNull final Supplier<?> message, @NonNull final Throwable cause)
    {
        if (getLogger().isErrorEnabled())
        {
            getLogger().error(LoggingOps.MERCENARY_MARKER, message.get().toString(), cause);
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
                list.forEach(value -> logs.info(LoggingOps.MERCENARY_MARKER, safe(value)));
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
        if (null == value)
        {
            return MinioUtils.NULLS_STRING_VALUED;
        }
        return value.toString();
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

    protected void assertEquals(@Nullable final Object expected, @Nullable final Object actual, @NonNull final Supplier<?> message)
    {
        Assertions.assertEquals(expected, actual, () -> message.get().toString());
    }

    protected void assertTrue(final boolean condition, @NonNull final Supplier<?> message)
    {
        Assertions.assertTrue(condition, () -> message.get().toString());
    }

    protected void assertFalse(final boolean condition, @NonNull final Supplier<?> message)
    {
        Assertions.assertFalse(condition, () -> message.get().toString());
    }
}
