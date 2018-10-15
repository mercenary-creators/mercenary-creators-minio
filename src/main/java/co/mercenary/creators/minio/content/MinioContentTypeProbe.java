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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import co.mercenary.creators.minio.util.MinioUtils;

@JsonIgnoreType
public interface MinioContentTypeProbe
{
    @Nullable
    default String getContentType(@Nullable final URL link)
    {
        if (null == link)
        {
            return MinioUtils.NULL();
        }
        if ("file".equalsIgnoreCase(link.getProtocol()))
        {
            try
            {
                final Path path = Paths.get(link.toURI());

                if (path.toFile().isFile())
                {
                    return getContentType(path);
                }
            }
            catch (final URISyntaxException e)
            {
                MinioUtils.requireNonNull(e);
            }
        }
        try
        {
            final URLConnection conn = link.openConnection();

            String type = conn.getContentType();

            final int size = conn.getContentLength();

            try (BufferedInputStream buff = new BufferedInputStream(conn.getInputStream(), size))
            {
                type = getContentType(buff, type);
            }
            if (conn instanceof HttpURLConnection)
            {
                MinioUtils.CAST(conn, HttpURLConnection.class).disconnect();
            }
            return type;
        }
        catch (final IOException e)
        {
            return MinioUtils.NULL();
        }
    }

    @Nullable
    default String getContentType(@Nullable final Path path)
    {
        if (null == path)
        {
            return MinioUtils.NULL();
        }
        return getContentType(path.toFile());
    }

    @Nullable
    default String getContentType(@Nullable final File file)
    {
        if (null == file)
        {
            return MinioUtils.NULL();
        }
        return getContentType(file.getName());
    }

    @Nullable
    default String getContentType(@Nullable final CharSequence name)
    {
        return getContentType(name, () -> MinioUtils.NULL());
    }

    @NonNull
    default String getContentType(@Nullable final CharSequence type, @Nullable final CharSequence name)
    {
        final String valu = MinioUtils.toStringOrElse(type, MinioUtils.EMPTY_STRING_VALUED).trim();

        final String path = MinioUtils.fixPathString(MinioUtils.toStringOrElse(name, MinioUtils.EMPTY_STRING_VALUED).trim());

        if (((valu.length() < 1) || (valu.equals(MinioUtils.getDefaultContentType()))) && (path.length() < 1))
        {
            return MinioUtils.getDefaultContentType();
        }
        final String find = getContentType(path);

        if ((find == null) || (find.length() < 1))
        {
            return MinioUtils.getDefaultContentType();
        }
        if ((find.equals(MinioUtils.getDefaultContentType())) && (valu.length() > 0))
        {
            return valu;
        }
        return find;
    }

    @Nullable
    default String getContentType(@Nullable final InputStream input)
    {
        return getContentType(input, MinioUtils.NULL());
    }

    @Nullable
    default String getContentType(@Nullable final InputStream input, @Nullable final String type)
    {
        return type;
    }

    @Nullable
    default String getContentType(@Nullable final byte[] data)
    {
        return MinioUtils.NULL();
    }

    @Nullable
    default String getContentType(@Nullable final Resource value)
    {
        String name = MinioUtils.NULL();

        if (null == value)
        {
            return name;
        }
        if (value.isFile())
        {
            name = getContentType(value.getFilename());

            if (null == name)
            {
                try
                {
                    name = getContentType(value.getFile());
                }
                catch (final IOException e)
                {
                    name = MinioUtils.NULL();
                }
            }
        }
        if (null == name)
        {
            try (final InputStream input = value.getInputStream())
            {
                return getContentType(input, name);
            }
            catch (final IOException e)
            {
                return MinioUtils.NULL();

            }
        }
        return name;
    }

    @Nullable
    default String getContentType(@Nullable final CharSequence name, @NonNull final Supplier<String> otherwise)
    {
        final String path = MinioUtils.toStringOrElse(name, MinioUtils.EMPTY_STRING_VALUED);

        if (path.length() > 4)
        {
            if (path.endsWith(".json"))
            {
                return MinioUtils.getJSONContentType();
            }
            if (path.endsWith(".yml") || path.endsWith(".yaml"))
            {
                return MinioUtils.getYAMLContentType();
            }
            if (path.endsWith(".java"))
            {
                return MinioUtils.getJAVAContentType();
            }
            if (path.endsWith(".properties"))
            {
                return MinioUtils.getPROPContentType();
            }
        }
        return otherwise.get();
    }
}
