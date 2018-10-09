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

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.lang.NonNull;

import co.mercenary.creators.minio.util.MinioUtils;

public class SLF4JLoggerFactory implements ILoggerFactory
{
    @NonNull
    public static final Marker MERCENARY_MARKER;

    static
    {
        start();

        MERCENARY_MARKER = MarkerFactory.getMarker(ILogger.class.getPackage().getName() + ".MARKER");
    }

    @NonNull
    private final ConcurrentHashMap<String, ILogger> loggers = new ConcurrentHashMap<>();

    @NonNull
    @Override
    public ILogger getLogger(@NonNull final CharSequence name)
    {
        return loggers.computeIfAbsent(MinioUtils.requireToString(name), SLF4JLogger::new);
    }

    private static void start()
    {
        if (false == SLF4JBridgeHandler.isInstalled())
        {
            SLF4JBridgeHandler.install();
        }
    }

    protected static class SLF4JLogger implements ILogger, WithLoggerOf<Logger>
    {
        @NonNull
        private final Logger logs;

        protected SLF4JLogger(@NonNull final String name)
        {
            this.logs = LoggerFactory.getLogger(MinioUtils.requireToString(name));
        }

        @NonNull
        @Override
        public Logger logger()
        {
            return logs;
        }

        @NonNull
        @Override
        public String toString()
        {
            return getClass().getName();
        }

        @Override
        public boolean isInfoEnabled()
        {
            return logger().isInfoEnabled();
        }

        @Override
        public void info(@NonNull final Supplier<?> message)
        {
            if (isInfoEnabled())
            {
                logger().info(MERCENARY_MARKER, message.get().toString());
            }
        }

        @Override
        public void info(@NonNull final Supplier<?> message, @NonNull final Throwable cause)
        {
            if (isInfoEnabled())
            {
                logger().info(MERCENARY_MARKER, message.get().toString(), cause);
            }
        }

        @Override
        public boolean isWarnEnabled()
        {
            return logger().isWarnEnabled();
        }

        @Override
        public void warn(final Supplier<?> message)
        {
            if (isWarnEnabled())
            {
                logger().warn(MERCENARY_MARKER, message.get().toString());
            }
        }

        @Override
        public void warn(final Supplier<?> message, final Throwable cause)
        {
            if (isWarnEnabled())
            {
                logger().warn(MERCENARY_MARKER, message.get().toString(), cause);
            }
        }

        @Override
        public boolean isTraceEnabled()
        {
            return logger().isTraceEnabled();
        }

        @Override
        public void trace(final Supplier<?> message)
        {
            if (isTraceEnabled())
            {
                logger().trace(MERCENARY_MARKER, message.get().toString());
            }
        }

        @Override
        public void trace(final Supplier<?> message, final Throwable cause)
        {
            if (isTraceEnabled())
            {
                logger().trace(MERCENARY_MARKER, message.get().toString(), cause);
            }
        }

        @Override
        public boolean isDebugEnabled()
        {
            return logger().isDebugEnabled();
        }

        @Override
        public void debug(final Supplier<?> message)
        {
            if (isDebugEnabled())
            {
                logger().debug(MERCENARY_MARKER, message.get().toString());
            }
        }

        @Override
        public void debug(final Supplier<?> message, final Throwable cause)
        {
            if (isDebugEnabled())
            {
                logger().debug(MERCENARY_MARKER, message.get().toString(), cause);
            }
        }

        @Override
        public boolean isErrorEnabled()
        {
            return logger().isErrorEnabled();
        }

        @Override
        public void error(final Supplier<?> message)
        {
            if (isErrorEnabled())
            {
                logger().error(MERCENARY_MARKER, message.get().toString());
            }
        }

        @Override
        public void error(final Supplier<?> message, final Throwable cause)
        {
            if (isErrorEnabled())
            {
                logger().error(MERCENARY_MARKER, message.get().toString(), cause);
            }
        }

        @Override
        public boolean isFatalEnabled()
        {
            return logger().isErrorEnabled();
        }

        @Override
        public void fatal(final Supplier<?> message)
        {
            if (isFatalEnabled())
            {
                logger().error(MERCENARY_MARKER, message.get().toString());
            }
        }

        @Override
        public void fatal(final Supplier<?> message, final Throwable cause)
        {
            if (isFatalEnabled())
            {
                logger().error(MERCENARY_MARKER, message.get().toString());
            }
        }
    }
}
