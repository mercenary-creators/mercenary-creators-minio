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

import co.mercenary.creators.minio.util.WithName;

public interface ILogger extends WithName
{
    boolean isInfoEnabled();

    void info(@NonNull Supplier<?> message);

    void info(@NonNull Supplier<?> message, @Nullable Throwable reason);

    boolean isWarnEnabled();

    void warn(@NonNull Supplier<?> message);

    void warn(@NonNull Supplier<?> message, @Nullable Throwable reason);

    boolean isTraceEnabled();

    void trace(@NonNull Supplier<?> message);

    void trace(@NonNull Supplier<?> message, @Nullable Throwable reason);

    boolean isDebugEnabled();

    void debug(@NonNull Supplier<?> message);

    void debug(@NonNull Supplier<?> message, @Nullable Throwable reason);

    boolean isErrorEnabled();

    void error(@NonNull Supplier<?> message);

    void error(@NonNull Supplier<?> message, @Nullable Throwable reason);

    boolean isFatalEnabled();

    void fatal(@NonNull Supplier<?> message);

    void fatal(@NonNull Supplier<?> message, @Nullable Throwable reason);
}
