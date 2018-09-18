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

package co.mercenary.creators.minio.content.tika;

import java.io.IOException;
import java.io.InputStream;

import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.language.translate.Translator;
import org.apache.tika.parser.Parser;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.xml.sax.SAXException;

import co.mercenary.creators.minio.content.MinioContentTypeProbe;
import co.mercenary.creators.minio.errors.MinioRuntimeException;
import co.mercenary.creators.minio.util.MinioUtils;

public class MinioContentTypeProbeTikaAdapter implements MinioContentTypeProbe, InitializingBean
{
    @NonNull
    private final Tika tika;

    public MinioContentTypeProbeTikaAdapter()
    {
        this.tika = new Tika();
    }

    public MinioContentTypeProbeTikaAdapter(@NonNull final Tika tika)
    {
        this.tika = MinioUtils.requireNonNull(tika);
    }

    public MinioContentTypeProbeTikaAdapter(@NonNull final TikaConfig config)
    {
        this.tika = new Tika(MinioUtils.requireNonNull(config));
    }

    public MinioContentTypeProbeTikaAdapter(@NonNull final Detector detector)
    {
        this.tika = new Tika(MinioUtils.requireNonNull(detector));
    }

    public MinioContentTypeProbeTikaAdapter(@NonNull final Detector detector, @NonNull final Parser parser)
    {
        this.tika = new Tika(MinioUtils.requireNonNull(detector), MinioUtils.requireNonNull(parser));
    }

    public MinioContentTypeProbeTikaAdapter(@NonNull final Detector detector, @NonNull final Parser parser, @NonNull final Translator translator)
    {
        this.tika = new Tika(MinioUtils.requireNonNull(detector), MinioUtils.requireNonNull(parser), MinioUtils.requireNonNull(translator));
    }

    public MinioContentTypeProbeTikaAdapter(@NonNull final Resource resource)
    {
        try (InputStream input = resource.getInputStream())
        {
            this.tika = new Tika(new TikaConfig(input));
        }
        catch (IOException | TikaException | SAXException e)
        {
            throw new MinioRuntimeException(e);
        }
    }

    public MinioContentTypeProbeTikaAdapter(@NonNull final InputStream input)
    {
        try
        {
            this.tika = new Tika(new TikaConfig(MinioUtils.requireNonNull(input)));
        }
        catch (TikaException | IOException | SAXException e)
        {
            throw new MinioRuntimeException(e);
        }
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
        final String valu = getTika().detect(name.toString());

        if ((null == valu) || (valu.trim().isEmpty()) || (valu.equals(MinioUtils.getDefaultContentType())))
        {
            return getContentType(name, () -> MinioUtils.NULL());
        }
        return valu;
    }

    @Nullable
    @Override
    public String getContentType(@Nullable final InputStream input)
    {
        if (null == input)
        {
            return MinioUtils.NULL();
        }
        try
        {
            final String valu = getTika().detect(input);

            if ((null == valu) || (valu.trim().isEmpty()))
            {
                return MinioUtils.NULL();
            }
            return valu;
        }
        catch (final IOException e)
        {
            return MinioUtils.NULL();
        }
    }

    @NonNull
    protected Tika getTika()
    {
        return tika;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
    }
}