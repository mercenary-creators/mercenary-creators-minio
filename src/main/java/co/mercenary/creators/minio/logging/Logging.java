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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import co.mercenary.creators.minio.util.ConditionalSupplier;
import co.mercenary.creators.minio.util.MinioUtils;

public final class Logging
{
    @NonNull
    private static final ILogger LOGGER = getLogger(Logging.class);

    private Logging()
    {
    }

    @Nullable
    public static <T> T NULL(final boolean test)
    {
        return MinioUtils.NULL();
    }

    @NonNull
    public static ILogger getLogger(@NonNull final Class<?> type)
    {
        return new InternalLogger(LogFactory.getLog(type), type.getName());
    }

    @NonNull
    public static ILogger getLogger(@NonNull final CharSequence name)
    {
        return new InternalLogger(LogFactory.getLog(name.toString()), name.toString());
    }

    @Nullable
    public static <T> T handle(@Nullable final Throwable reason)
    {
        return handle(() -> "handle()", reason, Logging::NULL);
    }

    @Nullable
    public static <T> T handle(@Nullable final Throwable reason, @NonNull final ConditionalSupplier<T> result)
    {
        return handle(() -> "handle()", reason, result);
    }

    @Nullable
    public static <T> T handle(@NonNull final Supplier<?> message, @Nullable final Throwable reason)
    {
        return handle(message, reason, Logging::NULL);
    }

    @Nullable
    public static <T> T handle(@NonNull final Supplier<?> message, @Nullable final Throwable reason, @NonNull final ConditionalSupplier<T> result)
    {
        final boolean both = isDebugEnabled() && isErrorEnabled();

        if (reason instanceof ThreadDeath)
        {
            if (both)
            {
                LOGGER.error(message, reason);
            }
            throw ((ThreadDeath) reason);
        }
        if (reason instanceof VirtualMachineError)
        {
            if (both)
            {
                LOGGER.error(message, reason);
            }
            throw ((VirtualMachineError) reason);
        }
        if (both)
        {
            LOGGER.error(message, reason);
        }
        return result.get(both);
    }

    public static boolean isInfoEnabled()
    {
        return LOGGER.isInfoEnabled();
    }

    @Nullable
    public static <T> T info(@NonNull final Supplier<?> message)
    {
        return info(message, Logging::NULL);
    }

    @Nullable
    public static <T> T info(@NonNull final Supplier<?> message, @NonNull final ConditionalSupplier<T> result)
    {
        LOGGER.info(message);

        return result.get(isInfoEnabled());
    }

    @Nullable
    public static <T> T info(@NonNull final Supplier<?> message, @Nullable final Throwable reason)
    {
        return info(message, reason, Logging::NULL);
    }

    @Nullable
    public static <T> T info(@NonNull final Supplier<?> message, @Nullable final Throwable reason, @NonNull final ConditionalSupplier<T> result)
    {
        LOGGER.info(message, reason);

        return result.get(isInfoEnabled());
    }

    public static boolean isWarnEnabled()
    {
        return LOGGER.isWarnEnabled();
    }

    @Nullable
    public static <T> T warn(@NonNull final Supplier<?> message)
    {
        return warn(message, Logging::NULL);
    }

    @Nullable
    public static <T> T warn(@NonNull final Supplier<?> message, @NonNull final ConditionalSupplier<T> result)
    {
        LOGGER.warn(message);

        return result.get(isWarnEnabled());
    }

    @Nullable
    public static <T> T warn(@NonNull final Supplier<?> message, @Nullable final Throwable reason)
    {
        return warn(message, reason, Logging::NULL);
    }

    @Nullable
    public static <T> T warn(@NonNull final Supplier<?> message, @Nullable final Throwable reason, @NonNull final ConditionalSupplier<T> result)
    {
        LOGGER.warn(message, reason);

        return result.get(isWarnEnabled());
    }

    public static boolean isErrorEnabled()
    {
        return LOGGER.isErrorEnabled();
    }

    @Nullable
    public static <T> T error(@NonNull final Supplier<?> message)
    {
        return error(message, Logging::NULL);
    }

    @Nullable
    public static <T> T error(@NonNull final Supplier<?> message, @NonNull final ConditionalSupplier<T> result)
    {
        LOGGER.error(message);

        return result.get(isErrorEnabled());
    }

    @Nullable
    public static <T> T error(@NonNull final Supplier<?> message, @Nullable final Throwable reason)
    {
        return error(message, reason, Logging::NULL);
    }

    @Nullable
    public static <T> T error(@NonNull final Supplier<?> message, @Nullable final Throwable reason, @NonNull final ConditionalSupplier<T> result)
    {
        LOGGER.error(message, reason);

        return result.get(isErrorEnabled());
    }

    public static boolean isDebugEnabled()
    {
        return LOGGER.isDebugEnabled();
    }

    @Nullable
    public static <T> T debug(@NonNull final Supplier<?> message)
    {
        return debug(message, Logging::NULL);
    }

    @Nullable
    public static <T> T debug(@NonNull final Supplier<?> message, @NonNull final ConditionalSupplier<T> result)
    {
        LOGGER.debug(message);

        return result.get(isDebugEnabled());
    }

    @Nullable
    public static <T> T debug(@NonNull final Supplier<?> message, @Nullable final Throwable reason)
    {
        return debug(message, reason, Logging::NULL);
    }

    @Nullable
    public static <T> T debug(@NonNull final Supplier<?> message, @Nullable final Throwable reason, @NonNull final ConditionalSupplier<T> result)
    {
        LOGGER.debug(message, reason);

        return result.get(isDebugEnabled());
    }

    private static final class InternalLogger implements ILogger
    {
        @NonNull
        private final Log    logs;

        @NonNull
        private final String name;

        private InternalLogger(@NonNull final Log logs, @NonNull final String name)
        {
            this.logs = logs;

            this.name = name;
        }

        @NonNull
        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public boolean isInfoEnabled()
        {
            return logs.isInfoEnabled();
        }

        @Override
        public void info(@NonNull final Supplier<?> message)
        {
            if (isInfoEnabled())
            {
                logs.info(message.get().toString());
            }
        }

        @Override
        public void info(@NonNull final Supplier<?> message, @Nullable final Throwable reason)
        {
            if (isInfoEnabled())
            {
                logs.info(message.get().toString(), reason);
            }
        }

        @Override
        public boolean isWarnEnabled()
        {
            return logs.isWarnEnabled();
        }

        @Override
        public void warn(@NonNull final Supplier<?> message)
        {
            if (isWarnEnabled())
            {
                logs.warn(message.get().toString());
            }
        }

        @Override
        public void warn(@NonNull final Supplier<?> message, @Nullable final Throwable reason)
        {
            if (isWarnEnabled())
            {
                logs.warn(message.get().toString(), reason);
            }
        }

        @Override
        public boolean isTraceEnabled()
        {
            return logs.isTraceEnabled();
        }

        @Override
        public void trace(@NonNull final Supplier<?> message)
        {
            if (isTraceEnabled())
            {
                logs.trace(message.get().toString());
            }
        }

        @Override
        public void trace(@NonNull final Supplier<?> message, @Nullable final Throwable reason)
        {
            if (isTraceEnabled())
            {
                logs.trace(message.get().toString(), reason);
            }
        }

        @Override
        public boolean isDebugEnabled()
        {
            return logs.isDebugEnabled();
        }

        @Override
        public void debug(@NonNull final Supplier<?> message)
        {
            if (isDebugEnabled())
            {
                logs.debug(message.get().toString());
            }
        }

        @Override
        public void debug(@NonNull final Supplier<?> message, @Nullable final Throwable reason)
        {
            if (isDebugEnabled())
            {
                logs.debug(message.get().toString(), reason);
            }
        }

        @Override
        public boolean isErrorEnabled()
        {
            return logs.isErrorEnabled();
        }

        @Override
        public void error(@NonNull final Supplier<?> message)
        {
            if (isErrorEnabled())
            {
                logs.error(message.get().toString());
            }
        }

        @Override
        public void error(@NonNull final Supplier<?> message, @Nullable final Throwable reason)
        {
            if (isErrorEnabled())
            {
                logs.error(message.get().toString(), reason);
            }
        }

        @Override
        public boolean isFatalEnabled()
        {
            return logs.isFatalEnabled();
        }

        @Override
        public void fatal(@NonNull final Supplier<?> message)
        {
            if (isFatalEnabled())
            {
                logs.fatal(message.get().toString());
            }
        }

        @Override
        public void fatal(@NonNull final Supplier<?> message, @Nullable final Throwable reason)
        {
            if (isFatalEnabled())
            {
                logs.fatal(message.get().toString(), reason);
            }
        }
    }
}
