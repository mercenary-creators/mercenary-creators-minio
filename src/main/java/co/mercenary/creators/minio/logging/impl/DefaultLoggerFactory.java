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

package co.mercenary.creators.minio.logging.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.lang.NonNull;

import co.mercenary.creators.minio.logging.ILogger;
import co.mercenary.creators.minio.logging.ILoggerFactory;
import co.mercenary.creators.minio.logging.WithLoggerOf;
import co.mercenary.creators.minio.util.MinioUtils;

public class DefaultLoggerFactory implements ILoggerFactory
{
    @NonNull
    private final ConcurrentHashMap<String, ILogger> loggers = new ConcurrentHashMap<>();

    @NonNull
    @Override
    public ILogger getLogger(@NonNull final CharSequence name)
    {
        return loggers.computeIfAbsent(MinioUtils.requireToString(name), DefaultLogger::new);
    }

    protected static class DefaultLogger implements ILogger, WithLoggerOf<Logger>
    {
        @NonNull
        private final Logger logs;

        protected DefaultLogger(@NonNull final String name)
        {
            this.logs = Logger.getLogger(MinioUtils.requireToString(name));
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
            return logger().isLoggable(Level.INFO);
        }

        @Override
        public void info(@NonNull final Supplier<?> message)
        {
            if (isInfoEnabled())
            {
                logger().info(message.get().toString());
            }
        }

        @Override
        public void info(@NonNull final Supplier<?> message, @NonNull final Throwable cause)
        {
            if (isInfoEnabled())
            {
                logger().log(Level.INFO, message.get().toString(), cause);
            }
        }

        @Override
        public boolean isWarnEnabled()
        {
            return logger().isLoggable(Level.WARNING);
        }

        @Override
        public void warn(final Supplier<?> message)
        {
            if (isWarnEnabled())
            {
                logger().warning(message.get().toString());
            }
        }

        @Override
        public void warn(final Supplier<?> message, final Throwable cause)
        {
            if (isWarnEnabled())
            {
                logger().log(Level.WARNING, message.get().toString(), cause);
            }
        }

        @Override
        public boolean isTraceEnabled()
        {
            return logger().isLoggable(Level.FINEST);
        }

        @Override
        public void trace(final Supplier<?> message)
        {
            if (isTraceEnabled())
            {
                logger().finest(message.get().toString());
            }
        }

        @Override
        public void trace(final Supplier<?> message, final Throwable cause)
        {
            if (isTraceEnabled())
            {
                logger().log(Level.FINEST, message.get().toString(), cause);
            }
        }

        @Override
        public boolean isDebugEnabled()
        {
            return logger().isLoggable(Level.FINE);
        }

        @Override
        public void debug(final Supplier<?> message)
        {
            if (isDebugEnabled())
            {
                logger().fine(message.get().toString());
            }
        }

        @Override
        public void debug(final Supplier<?> message, final Throwable cause)
        {
            if (isDebugEnabled())
            {
                logger().log(Level.FINE, message.get().toString(), cause);
            }
        }

        @Override
        public boolean isErrorEnabled()
        {
            return logger().isLoggable(Level.SEVERE);
        }

        @Override
        public void error(final Supplier<?> message)
        {
            if (isErrorEnabled())
            {
                logger().severe(message.get().toString());
            }
        }

        @Override
        public void error(final Supplier<?> message, final Throwable cause)
        {
            if (isErrorEnabled())
            {
                logger().log(Level.SEVERE, message.get().toString(), cause);
            }
        }

        @Override
        public boolean isFatalEnabled()
        {
            return isErrorEnabled();
        }

        @Override
        public void fatal(final Supplier<?> message)
        {
            if (isFatalEnabled())
            {
                logger().severe(message.get().toString());
            }
        }

        @Override
        public void fatal(final Supplier<?> message, final Throwable cause)
        {
            if (isFatalEnabled())
            {
                logger().log(Level.SEVERE, message.get().toString(), cause);
            }
        }
    }
}
