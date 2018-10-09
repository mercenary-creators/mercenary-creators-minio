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

import java.util.LinkedHashMap;
import java.util.function.Supplier;

import org.springframework.lang.NonNull;

import co.mercenary.creators.minio.util.MinioUtils;

public final class Logging
{
    @NonNull
    private static final ILoggerFactory logs = init(kind());

    private Logging()
    {
    }

    @NonNull
    public static <T> ILogger getLogger(@NonNull final Class<T> type)
    {
        return logs.getLogger(MinioUtils.requireNonNull(type));
    }

    @NonNull
    public static ILogger getLogger(@NonNull final CharSequence name)
    {
        return logs.getLogger(MinioUtils.requireNonNull(name));
    }

    @NonNull
    private static ILoggerFactory init(@NonNull final LinkedHashMap<String, Supplier<ILoggerFactory>> kind)
    {
        for (final String name : kind.keySet())
        {
            if (MinioUtils.isPresent(name))
            {
                return kind.get(name).get();
            }
        }
        return new DefaultLoggerFactory();
    }

    @NonNull
    private static LinkedHashMap<String, Supplier<ILoggerFactory>> kind()
    {
        final LinkedHashMap<String, Supplier<ILoggerFactory>> make = new LinkedHashMap<>(4);

        make.put("ch.qos.logback.classic.LoggerContext", LogbackLoggerFactory::new);

        make.put("org.slf4j.LoggerFactory", SLF4JLoggerFactory::new);

        make.put("org.apache.commons.logging.LogFactory", CommonsLoggerFactory::new);

        make.put("java.util.logging.Logger", DefaultLoggerFactory::new);

        return make;
    }
}
