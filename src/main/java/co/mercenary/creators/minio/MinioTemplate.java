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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.xmlpull.v1.XmlPullParserException;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import co.mercenary.creators.minio.content.MinioContentTypeProbe;
import co.mercenary.creators.minio.content.MinioContentTypeProbeFileTypeMapAdapter;
import co.mercenary.creators.minio.data.MinioBucket;
import co.mercenary.creators.minio.data.MinioCopyConditions;
import co.mercenary.creators.minio.data.MinioItem;
import co.mercenary.creators.minio.data.MinioObjectStatus;
import co.mercenary.creators.minio.data.MinioUpload;
import co.mercenary.creators.minio.data.MinioUserMetaData;
import co.mercenary.creators.minio.errors.MinioDataException;
import co.mercenary.creators.minio.errors.MinioOperationException;
import co.mercenary.creators.minio.errors.MinioRuntimeException;
import co.mercenary.creators.minio.util.JSONUtils;
import co.mercenary.creators.minio.util.MinioUtils;
import io.minio.CopyConditions;
import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.ServerSideEncryption;
import io.minio.errors.MinioException;
import io.minio.http.Method;

@JsonIgnoreType
public class MinioTemplate implements MinioOperations
{
    @NonNull
    private static final CopyConditions        COPY_CONDS = new MinioCopyConditions().setReplaceMetadataDirective().getCopyConditions();

    @NonNull
    private final String                       server_url;

    @Nullable
    private final String                       access_key;

    @Nullable
    private final String                       secret_key;

    @Nullable
    private final String                       aws_region;

    @Nullable
    @Autowired(required = false)
    private MinioContentTypeProbe              type_probe;

    @NonNull
    private final AtomicReference<MinioClient> atomic_ref = new AtomicReference<>();

    public MinioTemplate(@NonNull final CharSequence server, @Nullable final CharSequence access, @Nullable final CharSequence secret, @Nullable final CharSequence region)
    {
        this.server_url = MinioUtils.requireToString(server);

        this.access_key = MinioUtils.getCharSequence(access);

        this.secret_key = MinioUtils.getCharSequence(secret);

        this.aws_region = MinioUtils.fixRegionString(region, MinioUtils.DEFAULT_REGION_EAST);
    }

    @NonNull
    protected MinioClient getMinioClient()
    {
        MinioClient client = atomic_ref.get();

        if (null == client)
        {
            synchronized (this)
            {
                client = atomic_ref.get();

                if (null == client)
                {
                    client = atomic_ref.updateAndGet(update -> {

                        try
                        {
                            return new MinioClient(server_url, access_key, secret_key, MinioUtils.fixRegionString(aws_region, MinioUtils.isAmazonEndpoint(server_url)));
                        }
                        catch (final MinioException e)
                        {
                            throw new MinioRuntimeException(e);
                        }
                    });
                }
            }
        }
        return MinioUtils.requireNonNull(client, () -> MinioUtils.format("could not create MinioClient server=(%s), region=(%s).", getServer(), getRegion()));
    }

    public void setContentTypeProbe(@Nullable final MinioContentTypeProbe type_probe)
    {
        this.type_probe = MinioUtils.requireNonNullOrElse(type_probe, MinioContentTypeProbeFileTypeMapAdapter::instance);
    }

    @NonNull
    @Override
    public MinioContentTypeProbe getContentTypeProbe()
    {
        return MinioUtils.requireNonNullOrElse(type_probe, MinioContentTypeProbeFileTypeMapAdapter::instance);
    }

    @NonNull
    @Override
    public String getServer()
    {
        return server_url;
    }

    @NonNull
    @Override
    public String getRegion()
    {
        return MinioUtils.fixRegionString(aws_region, MinioUtils.DEFAULT_REGION_EAST);
    }

    @NonNull
    @Override
    public String toDescription()
    {
        return MinioUtils.format("server=(%s), region=(%s).", getServer(), getRegion());
    }

    @NonNull
    @Override
    public String toString()
    {
        return toDescription();
    }

    @Override
    public int hashCode()
    {
        return toDescription().hashCode();
    }

    @Override
    public boolean equals(final Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (other instanceof MinioTemplate)
        {
            return toString().equals(other.toString());
        }
        return false;
    }

    @Override
    public boolean isBucket(@NonNull final CharSequence bucket) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket);

        try
        {
            return getMinioClient().bucketExists(bucket.toString());
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public boolean deleteBucket(@NonNull final CharSequence bucket) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket);

        if (isBucket(bucket))
        {
            try
            {
                getMinioClient().removeBucket(bucket.toString());

                return true;
            }
            catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
            {
                throw new MinioOperationException(e);
            }
        }
        return false;
    }

    @Override
    public boolean isObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name);

        try
        {
            return getObjectStatus(bucket, name).getCreationTime().isPresent();
        }
        catch (final MinioOperationException e)
        {
            return false;
        }
    }

    @Override
    public boolean deleteObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name);

        if (isObject(bucket, name))
        {
            try
            {
                getMinioClient().removeObject(bucket.toString(), name.toString());

                return true;
            }
            catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
            {
                throw new MinioOperationException(e);
            }
        }
        return false;
    }

    @Override
    public boolean ensureBucket(@NonNull final CharSequence bucket) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket);

        if (false == isBucket(bucket))
        {
            try
            {
                getMinioClient().makeBucket(bucket.toString());

                return true;
            }
            catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
            {
                throw new MinioOperationException(e);
            }
        }
        return false;
    }

    @NonNull
    @Override
    public Stream<MinioBucket> getBuckets() throws MinioOperationException
    {
        try
        {
            return getMinioClient().listBuckets().stream().map(bucket -> new MinioBucket(bucket.name(), () -> bucket.creationDate(), this));
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public Stream<MinioBucket> getBucketsNamed(@NonNull final Predicate<String> filter) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(filter);

        try
        {
            return getMinioClient().listBuckets().stream().filter(bucket -> filter.test(bucket.name())).map(bucket -> new MinioBucket(bucket.name(), () -> bucket.creationDate(), this));
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public InputStream getObjectInputStream(@NonNull final CharSequence bucket, @NonNull final CharSequence name) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name);

        try
        {
            return getMinioClient().getObject(bucket.toString(), name.toString());
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public InputStream getObjectInputStream(@NonNull final CharSequence bucket, @NonNull final CharSequence name, final long skip) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name);

        try
        {
            return getMinioClient().getObject(bucket.toString(), name.toString(), skip);
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public InputStream getObjectInputStream(@NonNull final CharSequence bucket, @NonNull final CharSequence name, final long skip, final long leng) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name);

        try
        {
            return getMinioClient().getObject(bucket.toString(), name.toString(), skip, leng);
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public InputStream getObjectInputStream(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final ServerSideEncryption keys) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name, keys);

        try
        {
            return getMinioClient().getObject(bucket.toString(), name.toString(), keys);
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public MinioObjectStatus getObjectStatus(@NonNull final CharSequence bucket, @NonNull final CharSequence name) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name);

        try
        {
            final ObjectStat stat = getMinioClient().statObject(bucket.toString(), name.toString());

            return new MinioObjectStatus(name, bucket, stat.length(), getContentTypeProbe().getContentType(stat.contentType(), name), stat.etag(), () -> stat.createdTime(), new MinioUserMetaData(stat.httpHeaders()));
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public MinioObjectStatus getObjectStatus(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final ServerSideEncryption keys) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name, keys);

        try
        {
            final ObjectStat stat = getMinioClient().statObject(bucket.toString(), name.toString(), keys);

            return new MinioObjectStatus(name, bucket, stat.length(), getContentTypeProbe().getContentType(stat.contentType(), name), stat.etag(), () -> stat.createdTime(), new MinioUserMetaData(stat.httpHeaders()));
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final InputStream input, @Nullable final CharSequence type) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name, input);

        try
        {
            ensureBucket(bucket);

            getMinioClient().putObject(bucket.toString(), name.toString(), input, getContentTypeProbe().getContentType(type, name));
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final byte[] input, @Nullable final CharSequence type) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name, input);

        try (final ByteArrayInputStream baos = new ByteArrayInputStream(input))
        {
            putObject(bucket, name, baos, type);
        }
        catch (final IOException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public Stream<MinioItem> getItems(@NonNull final CharSequence bucket, @Nullable final CharSequence prefix, final boolean recursive) throws MinioOperationException
    {
        final MinioContentTypeProbe probe = getContentTypeProbe();

        return MinioUtils.getResultAsStream(getMinioClient().listObjects(MinioUtils.requireToString(bucket), MinioUtils.getCharSequence(prefix), recursive)).map(item -> new MinioItem(item.objectName(), bucket, item.objectSize(), !item.isDir(), item.etag(), probe.getContentType(item.objectName()), () -> item.lastModified(), item.storageClass(), this));
    }

    @Override
    public void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final Resource input, @Nullable final CharSequence type) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name, input);

        try (final InputStream is = input.getInputStream())
        {
            putObject(bucket, name, is, type);
        }
        catch (final IOException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final File input, @Nullable final CharSequence type) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name, input);

        try (final InputStream is = MinioUtils.getInputStream(input))
        {
            putObject(bucket, name, is, type);
        }
        catch (final IOException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final Path input, @Nullable final CharSequence type) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name, input);

        try (final InputStream is = MinioUtils.getInputStream(input))
        {
            putObject(bucket, name, is, type);
        }
        catch (final IOException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public String getSignedObjectUrl(@NonNull final Method method, @NonNull final CharSequence bucket, @NonNull final CharSequence name) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(method, bucket, name);

        try
        {
            if ((method == Method.GET) || (method == Method.HEAD))
            {
                return getMinioClient().presignedGetObject(bucket.toString(), name.toString());
            }
            if ((method == Method.PUT) || (method == Method.POST))
            {
                return getMinioClient().presignedPutObject(bucket.toString(), name.toString());
            }
            throw new MinioOperationException(MinioUtils.format("invalid method %s", method));
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public String getSignedObjectUrl(@NonNull final Method method, @NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final Long seconds) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(method, bucket, name, seconds);

        try
        {
            if ((method == Method.GET) || (method == Method.HEAD))
            {
                return getMinioClient().presignedGetObject(bucket.toString(), name.toString(), MinioUtils.getDuration(seconds));
            }
            if ((method == Method.PUT) || (method == Method.POST))
            {
                return getMinioClient().presignedPutObject(bucket.toString(), name.toString(), MinioUtils.getDuration(seconds));
            }
            throw new MinioOperationException(MinioUtils.format("invalid method %s", method));
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public String getSignedObjectUrl(@NonNull final Method method, @NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final Duration seconds) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(method, bucket, name, seconds);

        try
        {
            if ((method == Method.GET) || (method == Method.HEAD))
            {
                return getMinioClient().presignedGetObject(bucket.toString(), name.toString(), MinioUtils.getDuration(seconds));
            }
            if ((method == Method.PUT) || (method == Method.POST))
            {
                return getMinioClient().presignedPutObject(bucket.toString(), name.toString(), MinioUtils.getDuration(seconds));
            }
            throw new MinioOperationException(MinioUtils.format("invalid method %s", method));
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public String getSignedObjectUrl(@NonNull final Method method, @NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final Long time, @NonNull final TimeUnit unit) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(method, bucket, name, time, unit);

        try
        {
            if ((method == Method.GET) || (method == Method.HEAD))
            {
                return getMinioClient().presignedGetObject(bucket.toString(), name.toString(), MinioUtils.getDuration(time, unit));
            }
            if ((method == Method.PUT) || (method == Method.POST))
            {
                return getMinioClient().presignedPutObject(bucket.toString(), name.toString(), MinioUtils.getDuration(time, unit));
            }
            throw new MinioOperationException(MinioUtils.format("invalid method %s", method));
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public boolean copyObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final CharSequence target, @Nullable final MinioCopyConditions conditions) throws MinioOperationException
    {
        return copyObject(bucket, name, target, MinioUtils.NULL(), conditions);
    }

    @Override
    public boolean copyObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final CharSequence target, @Nullable final CharSequence object, @Nullable final MinioCopyConditions conditions) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name, target);

        if (isObject(bucket, name))
        {
            try
            {
                if (false == bucket.equals(target))
                {
                    ensureBucket(target);
                }
                if ((null != conditions) && (false == conditions.isEmpty()))
                {
                    getMinioClient().copyObject(bucket.toString(), name.toString(), target.toString(), (null == object) ? MinioUtils.NULL() : object.toString(), conditions.getCopyConditions());
                }
                else
                {
                    getMinioClient().copyObject(bucket.toString(), name.toString(), target.toString(), (null == object) ? MinioUtils.NULL() : object.toString());
                }
                return true;
            }
            catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
            {
                throw new MinioOperationException(e);
            }
        }
        return false;
    }

    @NonNull
    @Override
    public Stream<MinioUpload> getIncompleteUploads(@NonNull final CharSequence bucket, @Nullable final CharSequence prefix, final boolean recursive) throws MinioOperationException
    {
        return MinioUtils.getResultAsStream(getMinioClient().listIncompleteUploads(MinioUtils.requireToString(bucket), MinioUtils.getCharSequence(prefix), recursive)).map(item -> new MinioUpload(item.objectName(), bucket.toString(), this));
    }

    @Override
    public void setBucketPolicy(@NonNull final CharSequence bucket, @NonNull final Object policy) throws MinioOperationException, MinioDataException
    {
        MinioUtils.isEachNonNull(bucket, policy);

        try
        {
            if (policy instanceof CharSequence)
            {
                getMinioClient().setBucketPolicy(bucket.toString(), policy.toString());
            }
            else
            {
                getMinioClient().setBucketPolicy(bucket.toString(), JSONUtils.toJSONString(policy));
            }
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public <T> T getBucketPolicy(@NonNull final CharSequence bucket, @NonNull final Class<T> type) throws MinioOperationException, MinioDataException
    {
        MinioUtils.isEachNonNull(bucket, type);

        final String policy = getBucketPolicy(bucket);

        if ((String.class == type) || (CharSequence.class == type))
        {
            final T value = MinioUtils.CAST(policy, type);

            return MinioUtils.requireNonNull(value);
        }
        return JSONUtils.toJSONObject(policy, type);
    }

    @NonNull
    @Override
    public String getBucketPolicy(@NonNull final CharSequence bucket) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket);

        try
        {
            return MinioUtils.requireNonNull(getMinioClient().getBucketPolicy(bucket.toString()));
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public boolean removeUpload(@NonNull final CharSequence bucket, @NonNull final CharSequence name) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name);

        try
        {
            getMinioClient().removeIncompleteUpload(bucket.toString(), name.toString());

            return true;
        }
        catch (final MinioException | NoSuchAlgorithmException | IOException | XmlPullParserException | InvalidKeyException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public MinioUserMetaData getUserMetaData(@NonNull final CharSequence bucket, @NonNull final CharSequence name) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name);

        try
        {
            return new MinioUserMetaData(getMinioClient().statObject(bucket.toString(), name.toString()).httpHeaders());
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public boolean setUserMetaData(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @Nullable final MinioUserMetaData meta) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name);

        try
        {
            getMinioClient().copyObject(bucket.toString(), name.toString(), bucket.toString(), MinioUtils.NULL(), COPY_CONDS, (null == meta) ? MinioUtils.NULL() : meta.getUserMetaData());

            return true;
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public boolean addUserMetaData(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @Nullable final MinioUserMetaData meta) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name);

        if ((null == meta) || (meta.isEmpty()))
        {
            return false;
        }
        return setUserMetaData(bucket, name, getUserMetaData(bucket, name).plus(meta));
    }

    @Override
    public void traceStreamOff()
    {
        try
        {
            getMinioClient().traceOff();
        }
        catch (final IOException e)
        {
            throw new MinioRuntimeException(e);
        }
    }

    @Override
    public void setTraceStream(@Nullable final OutputStream stream)
    {
        if (null == stream)
        {
            traceStreamOff();
        }
        else
        {
            getMinioClient().traceOn(stream);
        }
    }
}
