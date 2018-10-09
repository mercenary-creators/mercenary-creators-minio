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

package co.mercenary.creators.minio.content;

import javax.activation.FileTypeMap;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import co.mercenary.creators.minio.util.MinioUtils;

@JsonIgnoreType
public class MinioContentTypeProbeFileTypeMapAdapter2 implements MinioContentTypeProbe, InitializingBean
{
    @NonNull
    private final FileTypeMap fmap;

    public MinioContentTypeProbeFileTypeMapAdapter2()
    {
        this(new MinioConfigurableMimeFileTypeMap());
    }

    public MinioContentTypeProbeFileTypeMapAdapter2(@NonNull final FileTypeMap fmap)
    {
        this.fmap = MinioUtils.requireNonNull(fmap);
    }

    @Nullable
    @Override
    public String getContentType(@Nullable final CharSequence name)
    {
        if ((null == name) || (name.length() < 1))
        {
            return MinioUtils.NULL();
        }
        final String type = MinioUtils.getContentTypeCommon(name);

        if (null != type)
        {
            return type;
        }
        final String valu = fmap.getContentType(name.toString());

        if ((null == valu) || (valu.trim().isEmpty()) || (valu.equals(MinioUtils.getDefaultContentType())))
        {
            return getContentType(name, () -> MinioUtils.NULL());
        }
        return valu;
    }

    @Override
    public synchronized void afterPropertiesSet() throws Exception
    {
        if (fmap instanceof InitializingBean)
        {
            MinioUtils.CAST(fmap, InitializingBean.class).afterPropertiesSet();
        }
    }

    @NonNull
    public static MinioContentTypeProbe instance()
    {
        return Factory.INSTANCE;
    }

    private static final class Factory
    {
        @NonNull
        private static final MinioContentTypeProbeFileTypeMapAdapter2 INSTANCE = new MinioContentTypeProbeFileTypeMapAdapter2();
    }
}
