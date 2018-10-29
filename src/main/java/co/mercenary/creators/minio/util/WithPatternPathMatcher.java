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

import org.springframework.lang.NonNull;
import org.springframework.util.PathMatcher;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

@JsonIgnoreType
@FunctionalInterface
public interface WithPatternPathMatcher
{
    @NonNull
    String getPattern();

    @NonNull
    default PathMatcher getPathMatcher()
    {
        return MinioUtils.GLOBAL_PATH_MATCHER;
    }

    default boolean isPattern()
    {
        return isPattern(getPattern());
    }

    default boolean isPattern(@NonNull final String path)
    {
        return getPathMatcher().isPattern(MinioUtils.requireNonNull(path));
    }

    default boolean isMatching(@NonNull final String path)
    {
        return getPathMatcher().match(getPattern(), MinioUtils.requireNonNull(path));
    }

    default boolean isStarting(@NonNull final String path)
    {
        return getPathMatcher().matchStart(getPattern(), MinioUtils.requireNonNull(path));
    }
}