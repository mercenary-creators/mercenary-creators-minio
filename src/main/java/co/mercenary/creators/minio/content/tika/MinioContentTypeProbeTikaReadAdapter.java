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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.language.translate.Translator;
import org.apache.tika.parser.Parser;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import co.mercenary.creators.minio.util.MinioUtils;

public class MinioContentTypeProbeTikaReadAdapter extends MinioContentTypeProbeTikaAdapter
{
    public MinioContentTypeProbeTikaReadAdapter()
    {
        super();
    }

    public MinioContentTypeProbeTikaReadAdapter(@NonNull final Tika tika)
    {
        super(tika);
    }

    public MinioContentTypeProbeTikaReadAdapter(@NonNull final TikaConfig config)
    {
        super(config);
    }

    public MinioContentTypeProbeTikaReadAdapter(@NonNull final Detector detector)
    {
        super(detector);
    }

    public MinioContentTypeProbeTikaReadAdapter(@NonNull final Detector detector, @NonNull final Parser parser)
    {
        super(detector, parser);
    }

    public MinioContentTypeProbeTikaReadAdapter(@NonNull final Detector detector, @NonNull final Parser parser, @NonNull final Translator translator)
    {
        super(detector, parser, translator);
    }

    @Nullable
    @Override
    public String getContentType(@Nullable final Path path)
    {
        if (null == path)
        {
            return MinioUtils.NULL();
        }
        try
        {
            return getTika().detect(path);
        }
        catch (final IOException e)
        {
            return getContentType(path.toFile());
        }
    }

    @Nullable
    @Override
    public String getContentType(@Nullable final File file)
    {
        if (null == file)
        {
            return MinioUtils.NULL();
        }
        try
        {
            return getTika().detect(file);
        }
        catch (final IOException e)
        {
            return getContentType(file.getName());
        }
    }
}
