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

package co.mercenary.creators.minio.logging;

import java.util.function.Supplier;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@FunctionalInterface
public interface WithLogging
{
    @NonNull
    ILogger getLogger();

    default boolean isInfoEnabled()
    {
        return getLogger().isInfoEnabled();
    }

    default void info(@NonNull final Supplier<?> message)
    {
        if (isInfoEnabled())
        {
            getLogger().info(message);
        }
    }

    default void info(@NonNull final Supplier<?> message, @Nullable final Throwable reason)
    {
        if (isInfoEnabled())
        {
            getLogger().info(message, reason);
        }
    }

    default boolean isWarnEnabled()
    {
        return getLogger().isWarnEnabled();
    }

    default void warn(@NonNull final Supplier<?> message)
    {
        if (isWarnEnabled())
        {
            getLogger().warn(message);
        }
    }

    default void warn(@NonNull final Supplier<?> message, @Nullable final Throwable reason)
    {
        if (isWarnEnabled())
        {
            getLogger().warn(message, reason);
        }
    }

    default boolean isTraceEnabled()
    {
        return getLogger().isTraceEnabled();
    }

    default void trace(@NonNull final Supplier<?> message)
    {
        if (isTraceEnabled())
        {
            getLogger().trace(message);
        }
    }

    default void trace(@NonNull final Supplier<?> message, @Nullable final Throwable reason)
    {
        if (isTraceEnabled())
        {
            getLogger().trace(message, reason);
        }
    }

    default boolean isDebugEnabled()
    {
        return getLogger().isDebugEnabled();
    }

    default void debug(@NonNull final Supplier<?> message)
    {
        if (isDebugEnabled())
        {
            getLogger().debug(message);
        }
    }

    default void debug(@NonNull final Supplier<?> message, @Nullable final Throwable reason)
    {
        if (isDebugEnabled())
        {
            getLogger().debug(message, reason);
        }
    }

    default boolean isErrorEnabled()
    {
        return getLogger().isErrorEnabled();
    }

    default void error(@NonNull final Supplier<?> message)
    {
        if (isErrorEnabled())
        {
            getLogger().error(message);
        }
    }

    default void error(@NonNull final Supplier<?> message, @Nullable final Throwable reason)
    {
        if (isErrorEnabled())
        {
            getLogger().error(message, reason);
        }
    }

    default boolean isFatalEnabled()
    {
        return getLogger().isFatalEnabled();
    }

    default void fatal(@NonNull final Supplier<?> message)
    {
        if (isFatalEnabled())
        {
            getLogger().fatal(message);
        }
    }

    default void fatal(@NonNull final Supplier<?> message, @Nullable final Throwable reason)
    {
        if (isFatalEnabled())
        {
            getLogger().fatal(message, reason);
        }
    }
}
