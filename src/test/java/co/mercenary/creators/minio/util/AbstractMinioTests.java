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
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import co.mercenary.creators.minio.MinioTemplate;
import co.mercenary.creators.minio.errors.MinioDataException;

public abstract class AbstractMinioTests extends AbstractJUnit4SpringContextTests
{
    @Nullable
    @Autowired
    private MinioTemplate minioTemplate;

    @Nullable
    protected MinioTemplate getMinioTemplate()
    {
        return minioTemplate;
    }

    @NonNull
    protected Log getLogger()
    {
        return logger;
    }

    @Nullable
    protected ApplicationContext getApplicationContext()
    {
        return applicationContext;
    }

    protected void info(@NonNull final Supplier<String> message)
    {
        if (getLogger().isInfoEnabled())
        {
            getLogger().info(message.get());
        }
    }

    protected void info(@NonNull final Supplier<String> message, @NonNull final Throwable cause)
    {
        if (getLogger().isInfoEnabled())
        {
            getLogger().info(message.get(), cause);
        }
    }

    protected void warn(@NonNull final Supplier<String> message)
    {
        if (getLogger().isWarnEnabled())
        {
            getLogger().warn(message.get());
        }
    }

    protected void warn(@NonNull final Supplier<String> message, @NonNull final Throwable cause)
    {
        if (getLogger().isWarnEnabled())
        {
            getLogger().warn(message.get(), cause);
        }
    }

    protected void debug(@NonNull final Supplier<String> message)
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug(message.get());
        }
    }

    protected void debug(@NonNull final Supplier<String> message, @NonNull final Throwable cause)
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug(message.get(), cause);
        }
    }

    protected void error(@NonNull final Supplier<String> message)
    {
        if (getLogger().isErrorEnabled())
        {
            getLogger().error(message.get());
        }
    }

    protected void error(@NonNull final Supplier<String> message, @NonNull final Throwable cause)
    {
        if (getLogger().isErrorEnabled())
        {
            getLogger().error(message.get(), cause);
        }
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
            return MinioUtils.toJSONString(value, pretty);
        }
        catch (final MinioDataException e)
        {
            throw new AssertionError("toJSONString", e);
        }
    }

    @NonNull
    protected <T> T toJSONObject(@NonNull final CharSequence value, @NonNull final Class<T> type)
    {
        try
        {
            return MinioUtils.toJSONObject(value, type);
        }
        catch (final MinioDataException e)
        {
            throw new AssertionError("toJSONObject", e);
        }
    }

    protected void assertEquals(@Nullable final CharSequence message, @Nullable final Object expected, @Nullable final Object actual)
    {
        Assert.assertEquals(MinioUtils.getCharSequence(message), expected, actual);
    }

    protected void assertTrue(@Nullable final CharSequence message, final boolean condition)
    {
        Assert.assertTrue(MinioUtils.getCharSequence(message), condition);
    }

    protected void assertFalse(@Nullable final CharSequence message, final boolean condition)
    {
        Assert.assertFalse(MinioUtils.getCharSequence(message), condition);
    }
}
