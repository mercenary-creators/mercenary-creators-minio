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

import org.springframework.lang.NonNull;

public abstract class AbstractWithLogging implements WithLogging
{
    @NonNull
    private final ILogger logger;

    protected AbstractWithLogging()
    {
        logger = Logging.getLogger(getClass());
    }

    protected AbstractWithLogging(@NonNull final Class<?> type)
    {
        logger = Logging.getLogger(type);
    }

    protected AbstractWithLogging(@NonNull final ILogger logs)
    {
        logger = Logging.getLogger(logs.getName());
    }

    protected AbstractWithLogging(@NonNull final CharSequence name)
    {
        logger = Logging.getLogger(name.toString());
    }

    @NonNull
    @Override
    public ILogger getLogger()
    {
        return logger;
    }
}
