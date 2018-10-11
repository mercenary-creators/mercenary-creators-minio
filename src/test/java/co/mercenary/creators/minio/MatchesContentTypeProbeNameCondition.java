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

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.MultiValueMap;

import co.mercenary.creators.minio.content.MinioContentTypeProbe;
import co.mercenary.creators.minio.util.MinioUtils;

public class MatchesContentTypeProbeNameCondition implements Condition
{
    @Override
    public boolean matches(@NonNull final ConditionContext ctxt, @NonNull final AnnotatedTypeMetadata meta)
    {
        final MultiValueMap<String, Object> attr = meta.getAllAnnotationAttributes(MatchesContentTypeProbeName.class.getName());

        if (attr != null)
        {
            if (getAttributeString(attr.getFirst("value"), "file").equalsIgnoreCase(ctxt.getEnvironment().getProperty("minio.content-type-probe.name", "file")))
            {
                return true;
            }
            if (getAttributeString(attr.getFirst("matchIfMissing"), MinioUtils.FALSE_STRING_VALUED).equalsIgnoreCase(MinioUtils.FALSE_STRING_VALUED))
            {
                return false;
            }
            final ConfigurableListableBeanFactory fact = ctxt.getBeanFactory();

            if ((null != fact) && (fact.getBeanNamesForType(MinioContentTypeProbe.class).length < 1))
            {
                return true;
            }
            return false;
        }
        return true;
    }

    @NonNull
    private String getAttributeString(@Nullable final Object valu, @NonNull final String attr)
    {
        return MinioUtils.requireNonNullOrElse(valu, attr).toString();
    }
}
