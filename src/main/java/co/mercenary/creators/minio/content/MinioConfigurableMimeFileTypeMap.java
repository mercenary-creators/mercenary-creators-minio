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

import java.io.IOException;

import javax.activation.FileTypeMap;
import javax.annotation.concurrent.NotThreadSafe;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.mail.javamail.ConfigurableMimeFileTypeMap;

@NotThreadSafe
public class MinioConfigurableMimeFileTypeMap extends ConfigurableMimeFileTypeMap implements InitializingBean
{
    public MinioConfigurableMimeFileTypeMap()
    {
        super();
    }

    public MinioConfigurableMimeFileTypeMap(@Nullable final Resource location)
    {
        if (null != location)
        {
            super.setMappingLocation(location);
        }
    }

    @NonNull
    public FileTypeMap toFileTypeMap()
    {
        return getFileTypeMap();
    }

    @NonNull
    @Override
    protected synchronized FileTypeMap createFileTypeMap(@Nullable final Resource location, @Nullable final String[] mappings) throws IOException
    {
        return super.createFileTypeMap(location, mappings);
    }
}
