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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.lang.NonNull;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.classic.turbo.TurboFilter;

public final class LoggingOps
{
    @NonNull
    public static final Marker MERCENARY_MARKER;

    static
    {
        start();

        MERCENARY_MARKER = getMarker(LoggingOps.class.getPackage());
    }

    private LoggingOps()
    {
    }

    public static Marker getMarker(final String name)
    {
        return MarkerFactory.getMarker(MinioUtils.requireNonNull(name));
    }

    public static Marker getMarker(final Package pack)
    {
        return MarkerFactory.getMarker(pack.getName() + ".MARKER");
    }

    public static Logger getLogger(final String name)
    {
        return LoggerFactory.getLogger(MinioUtils.requireNonNull(name));
    }

    public static Logger getLogger(final Class<?> type)
    {
        return LoggerFactory.getLogger(MinioUtils.requireNonNull(type));
    }

    public static boolean isStarted()
    {
        return context().isStarted();
    }

    public static void start()
    {
        if (false == isStarted())
        {
            if (false == SLF4JBridgeHandler.isInstalled())
            {
                SLF4JBridgeHandler.install();
            }
            context().start();
        }
    }

    public static void stop()
    {
        if (isStarted())
        {
            if (SLF4JBridgeHandler.isInstalled())
            {
                SLF4JBridgeHandler.uninstall();
            }
            context().stop();
        }
    }

    public static void reset()
    {
        context().reset();
    }

    public static void addFilters(final TurboFilter... filters)
    {
        final LoggerContext context = context();

        for (final TurboFilter filter : filters)
        {
            context.addTurboFilter(MinioUtils.requireNonNull(filter));
        }
    }

    public static void setFilters(final TurboFilter... filters)
    {
        final LoggerContext context = context();

        context.resetTurboFilterList();

        for (final TurboFilter filter : filters)
        {
            context.addTurboFilter(MinioUtils.requireNonNull(filter));
        }
    }

    public static void addListeners(final LoggerContextListener... listeners)
    {
        final LoggerContext context = context();

        for (final LoggerContextListener listener : listeners)
        {
            context.addListener(MinioUtils.requireNonNull(listener));
        }
    }

    public static void setListeners(final LoggerContextListener... listeners)
    {
        final LoggerContext context = context();

        context.getCopyOfListenerList().forEach(context::removeListener);

        for (final LoggerContextListener listener : listeners)
        {
            context.addListener(MinioUtils.requireNonNull(listener));
        }
    }

    public static Level getLevel(final String name)
    {
        return classic(name).getLevel();
    }

    public static Level getLevel(final Logger logger)
    {
        return getLevel(logger.getName());
    }

    public static Level getLevel()
    {
        return getLevel(Logger.ROOT_LOGGER_NAME);
    }

    public static void setLevel(final String level)
    {
        setLevel(Level.toLevel(level, Level.INFO));
    }

    public static void setLevel(final Level level)
    {
        classic(Logger.ROOT_LOGGER_NAME).setLevel(level);
    }

    public static Logger setLevel(final Logger logger, final Level level)
    {
        classic(logger.getName()).setLevel(level);

        return logger;
    }

    public static Logger setLevel(final Logger logger, final String level)
    {
        return setLevel(logger, Level.toLevel(level, Level.INFO));
    }

    public static LoggerContext context()
    {
        return ((LoggerContext) LoggerFactory.getILoggerFactory());
    }

    private static ch.qos.logback.classic.Logger classic(final String name)
    {
        if (name.equalsIgnoreCase(Logger.ROOT_LOGGER_NAME))
        {
            return context().getLogger(Logger.ROOT_LOGGER_NAME);
        }
        return context().exists(name);
    }
}
