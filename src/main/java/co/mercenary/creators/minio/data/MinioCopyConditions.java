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

package co.mercenary.creators.minio.data;

import java.util.Date;

import org.joda.time.DateTime;
import org.springframework.lang.NonNull;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import co.mercenary.creators.minio.errors.MinioDataException;
import co.mercenary.creators.minio.util.MinioUtils;
import io.minio.CopyConditions;
import io.minio.errors.InvalidArgumentException;

@JsonIgnoreType
public class MinioCopyConditions
{
    @NonNull
    private final CopyConditions cond;

    public MinioCopyConditions()
    {
        this(new CopyConditions());
    }

    public MinioCopyConditions(@NonNull final CopyConditions cond)
    {
        this.cond = MinioUtils.requireNonNull(cond);
    }

    @NonNull
    public CopyConditions getCopyConditions()
    {
        return cond;
    }

    @NonNull
    public MinioCopyConditions setModified(@NonNull final DateTime date) throws MinioDataException
    {
        try
        {
            getCopyConditions().setModified(date);
        }
        catch (final InvalidArgumentException e)
        {
            throw new MinioDataException(e);
        }
        return this;
    }

    @NonNull
    public MinioCopyConditions setModified(@NonNull final Date date) throws MinioDataException
    {
        return setModified(new DateTime(date.getTime()));
    }

    @NonNull
    public MinioCopyConditions setModified(@NonNull final Long date) throws MinioDataException
    {
        return setModified(new DateTime(date.longValue()));
    }

    @NonNull
    public MinioCopyConditions setUnmodified(@NonNull final DateTime date) throws MinioDataException
    {
        try
        {
            getCopyConditions().setUnmodified(date);
        }
        catch (final InvalidArgumentException e)
        {
            throw new MinioDataException(e);
        }
        return this;
    }

    @NonNull
    public MinioCopyConditions setUnmodified(@NonNull final Date date) throws MinioDataException
    {
        return setUnmodified(new DateTime(date.getTime()));
    }

    @NonNull
    public MinioCopyConditions setUnmodified(@NonNull final Long date) throws MinioDataException
    {
        return setUnmodified(new DateTime(date.longValue()));
    }

    @NonNull
    public MinioCopyConditions setMatchETag(@NonNull final String etag) throws MinioDataException
    {
        try
        {
            getCopyConditions().setMatchETag(MinioUtils.getETagSequence(etag));
        }
        catch (final InvalidArgumentException e)
        {
            throw new MinioDataException(e);
        }
        return this;
    }

    @NonNull
    public MinioCopyConditions setMatchETagNone(@NonNull final String etag) throws MinioDataException
    {
        try
        {
            getCopyConditions().setMatchETagNone(MinioUtils.getETagSequence(etag));
        }
        catch (final InvalidArgumentException e)
        {
            throw new MinioDataException(e);
        }
        return this;
    }

    @NonNull
    public MinioCopyConditions setReplaceMetadataDirective()
    {
        getCopyConditions().setReplaceMetadataDirective();

        return this;
    }

    public boolean isEmpty()
    {
        return getCopyConditions().getConditions().isEmpty();
    }
}
