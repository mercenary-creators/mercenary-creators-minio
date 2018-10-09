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

package co.mercenary.creators.minio;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.MultiValueMap;

import co.mercenary.creators.minio.logging.impl.AbstractWithLogger;
import co.mercenary.creators.minio.util.MinioUtils;

public class MatchesProbeCondition extends AbstractWithLogger implements Condition
{
    @Override
    public boolean matches(@NonNull final ConditionContext context, @NonNull final AnnotatedTypeMetadata metadata)
    {
        final MultiValueMap<String, Object> attrs = metadata.getAllAnnotationAttributes(MatchesProbe.class.getName());

        if (attrs != null)
        {
            if (getAttributeString(attrs.getFirst("value"), "file").equalsIgnoreCase(context.getEnvironment().getProperty("minio.type-probe", "file")))
            {
                return true;
            }
            return false;
        }
        return true;
    }

    @NonNull
    private String getAttributeString(@Nullable final Object value, @NonNull final String oherwise)
    {
        final String text = MinioUtils.requireNonNullOrElse(value, oherwise).toString();

        info(() -> text);

        return text;
    }
}
