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
public class PatternPathMatcher implements WithPatternPathMatcher
{
    @NonNull
    private final String      pattern;

    @NonNull
    private final PathMatcher matcher;

    public PatternPathMatcher(@NonNull final String pattern)
    {
        this(pattern, MinioUtils.GLOBAL_PATH_MATCHER);
    }

    public PatternPathMatcher(@NonNull final String pattern, @NonNull final PathMatcher matcher)
    {
        this.pattern = MinioUtils.requireNonNull(pattern);

        this.matcher = MinioUtils.requireNonNull(matcher);
    }

    @NonNull
    @Override
    public String getPattern()
    {
        return pattern;
    }

    @NonNull
    @Override
    public PathMatcher getPathMatcher()
    {
        return matcher;
    }
}
