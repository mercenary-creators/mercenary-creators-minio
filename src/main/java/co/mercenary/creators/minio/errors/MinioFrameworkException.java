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

package co.mercenary.creators.minio.errors;

import org.springframework.lang.Nullable;

public class MinioFrameworkException extends Exception
{
    private static final long serialVersionUID = 2165046650833683617L;

    public MinioFrameworkException()
    {
        super();
    }

    public MinioFrameworkException(@Nullable final String message)
    {
        super(message);
    }

    public MinioFrameworkException(@Nullable final Throwable cause)
    {
        super(cause);
    }

    public MinioFrameworkException(@Nullable final String message, @Nullable final Throwable cause)
    {
        super(message, cause);
    }

    public MinioFrameworkException(@Nullable final String message, @Nullable final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
