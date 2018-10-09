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

public interface WithLoggerDefault extends WithLogger, ILogger
{
    @Override
    default boolean isInfoEnabled()
    {
        return logger().isInfoEnabled();
    }

    @Override
    default void info(@NonNull final Supplier<?> message)
    {
        if (isInfoEnabled())
        {
            logger().info(message);
        }
    }

    @Override
    default void info(@NonNull final Supplier<?> message, @NonNull final Throwable cause)
    {
        if (isInfoEnabled())
        {
            logger().info(message, cause);
        }
    }

    @Override
    default boolean isWarnEnabled()
    {
        return logger().isWarnEnabled();
    }

    @Override
    default void warn(final Supplier<?> message)
    {
        if (isWarnEnabled())
        {
            logger().warn(message);
        }
    }

    @Override
    default void warn(final Supplier<?> message, final Throwable cause)
    {
        if (isWarnEnabled())
        {
            logger().warn(message, cause);
        }
    }

    @Override
    default boolean isTraceEnabled()
    {
        return logger().isTraceEnabled();
    }

    @Override
    default void trace(final Supplier<?> message)
    {
        if (isTraceEnabled())
        {
            logger().trace(message);
        }
    }

    @Override
    default void trace(final Supplier<?> message, final Throwable cause)
    {
        if (isTraceEnabled())
        {
            logger().trace(message, cause);
        }
    }

    @Override
    default boolean isDebugEnabled()
    {
        return logger().isDebugEnabled();
    }

    @Override
    default void debug(final Supplier<?> message)
    {
        if (isDebugEnabled())
        {
            logger().debug(message);
        }
    }

    @Override
    default void debug(final Supplier<?> message, final Throwable cause)
    {
        if (isDebugEnabled())
        {
            logger().debug(message, cause);
        }
    }

    @Override
    default boolean isErrorEnabled()
    {
        return logger().isErrorEnabled();
    }

    @Override
    default void error(final Supplier<?> message)
    {
        if (isErrorEnabled())
        {
            logger().error(message);
        }
    }

    @Override
    default void error(final Supplier<?> message, final Throwable cause)
    {
        if (isErrorEnabled())
        {
            logger().error(message, cause);
        }
    }

    @Override
    default boolean isFatalEnabled()
    {
        return logger().isFatalEnabled();
    }

    @Override
    default void fatal(final Supplier<?> message)
    {
        if (isFatalEnabled())
        {
            logger().fatal(message);
        }
    }

    @Override
    default void fatal(final Supplier<?> message, final Throwable cause)
    {
        if (isFatalEnabled())
        {
            logger().fatal(message, cause);
        }
    }
}
