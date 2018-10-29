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
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.regex.Pattern;
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
import co.mercenary.creators.minio.json.JSONUtils;
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

    public MinioTemplate(@NonNull final String server, @Nullable final String access, @Nullable final String secret, @Nullable final String region)
    {
        this.server_url = MinioUtils.fixServerString(server);

        this.access_key = access;

        this.secret_key = secret;

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
        return MinioUtils.requireNonNull(client, () -> String.format("could not create MinioClient server=(%s), region=(%s).", getServer(), getRegion()));
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
        return String.format("server=(%s), region=(%s).", getServer(), getRegion());
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
    public boolean isBucket(@NonNull final String bucket) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket);

        try
        {
            return getMinioClient().bucketExists(bucket);
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public boolean deleteBucket(@NonNull final String bucket) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket);

        if (isBucket(bucket))
        {
            try
            {
                getMinioClient().removeBucket(bucket);

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
    public boolean isObject(@NonNull final String bucket, @NonNull final String name) throws MinioOperationException
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
    public boolean deleteObject(@NonNull final String bucket, @NonNull final String name) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name);

        if (isObject(bucket, name))
        {
            try
            {
                getMinioClient().removeObject(bucket, name);

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
    public boolean ensureBucket(@NonNull final String bucket) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket);

        if (false == isBucket(bucket))
        {
            try
            {
                getMinioClient().makeBucket(bucket);

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
    public Stream<MinioBucket> findBuckets() throws MinioOperationException
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
    public Stream<MinioBucket> findBuckets(@NonNull final Predicate<String> filter) throws MinioOperationException
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
    public Stream<MinioBucket> findBuckets(@NonNull final Collection<String> filter) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(filter);

        return findBuckets(filter::contains);
    }

    @NonNull
    @Override
    public InputStream getObjectInputStream(@NonNull final String bucket, @NonNull final String name) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name);

        try
        {
            return getMinioClient().getObject(bucket, name);
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public InputStream getObjectInputStream(@NonNull final String bucket, @NonNull final String name, final long skip) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name);

        try
        {
            return getMinioClient().getObject(bucket, name, skip);
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public InputStream getObjectInputStream(@NonNull final String bucket, @NonNull final String name, final long skip, final long leng) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name);

        try
        {
            return getMinioClient().getObject(bucket, name, skip, leng);
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public InputStream getObjectInputStream(@NonNull final String bucket, @NonNull final String name, @NonNull final ServerSideEncryption keys) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name, keys);

        try
        {
            return getMinioClient().getObject(bucket, name, keys);
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public MinioObjectStatus getObjectStatus(@NonNull final String bucket, @NonNull final String name) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name);

        try
        {
            final ObjectStat stat = getMinioClient().statObject(bucket, name);

            return new MinioObjectStatus(name, bucket, stat.length(), getContentTypeProbe().getContentType(stat.contentType(), name), stat.etag(), () -> stat.createdTime(), new MinioUserMetaData(stat.httpHeaders()));
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public MinioObjectStatus getObjectStatus(@NonNull final String bucket, @NonNull final String name, @NonNull final ServerSideEncryption keys) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name, keys);

        try
        {
            final ObjectStat stat = getMinioClient().statObject(bucket, name, keys);

            return new MinioObjectStatus(name, bucket, stat.length(), getContentTypeProbe().getContentType(stat.contentType(), name), stat.etag(), () -> stat.createdTime(), new MinioUserMetaData(stat.httpHeaders()));
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public void putObject(@NonNull final String bucket, @NonNull final String name, @NonNull final InputStream input, @Nullable final String type) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name, input);

        try
        {
            ensureBucket(bucket);

            getMinioClient().putObject(bucket, name, input, getContentTypeProbe().getContentType(type, name));
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public void putObject(@NonNull final String bucket, @NonNull final String name, @NonNull final byte[] input, @Nullable final String type) throws MinioOperationException
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
    public Stream<MinioItem> findItems(@NonNull final String bucket, @Nullable final String prefix, final boolean recursive) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket);

        final MinioContentTypeProbe probe = getContentTypeProbe();

        return MinioUtils.getResultAsStream(getMinioClient().listObjects(bucket, prefix, recursive)).map(item -> new MinioItem(item.objectName(), bucket, item.objectSize(), !item.isDir(), item.etag(), probe.getContentType(item.objectName()), () -> item.lastModified(), item.storageClass(), this));
    }

    @Override
    public void putObject(@NonNull final String bucket, @NonNull final String name, @NonNull final Resource input, @Nullable final String type) throws MinioOperationException
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
    public void putObject(@NonNull final String bucket, @NonNull final String name, @NonNull final File input, @Nullable final String type) throws MinioOperationException
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
    public void putObject(@NonNull final String bucket, @NonNull final String name, @NonNull final Path input, @Nullable final String type) throws MinioOperationException
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
    public String getSignedObjectUrl(@NonNull final Method method, @NonNull final String bucket, @NonNull final String name) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(method, bucket, name);

        try
        {
            if ((method == Method.GET) || (method == Method.HEAD))
            {
                return getMinioClient().presignedGetObject(bucket, name);
            }
            if ((method == Method.PUT) || (method == Method.POST))
            {
                return getMinioClient().presignedPutObject(bucket, name);
            }
            throw new MinioOperationException(String.format("invalid method %s", method));
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public String getSignedObjectUrl(@NonNull final Method method, @NonNull final String bucket, @NonNull final String name, @NonNull final Long seconds) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(method, bucket, name, seconds);

        try
        {
            if ((method == Method.GET) || (method == Method.HEAD))
            {
                return getMinioClient().presignedGetObject(bucket, name, MinioUtils.getDuration(seconds));
            }
            if ((method == Method.PUT) || (method == Method.POST))
            {
                return getMinioClient().presignedPutObject(bucket, name, MinioUtils.getDuration(seconds));
            }
            throw new MinioOperationException(String.format("invalid method %s", method));
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public String getSignedObjectUrl(@NonNull final Method method, @NonNull final String bucket, @NonNull final String name, @NonNull final Duration seconds) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(method, bucket, name, seconds);

        try
        {
            if ((method == Method.GET) || (method == Method.HEAD))
            {
                return getMinioClient().presignedGetObject(bucket, name, MinioUtils.getDuration(seconds));
            }
            if ((method == Method.PUT) || (method == Method.POST))
            {
                return getMinioClient().presignedPutObject(bucket, name, MinioUtils.getDuration(seconds));
            }
            throw new MinioOperationException(String.format("invalid method %s", method));
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public String getSignedObjectUrl(@NonNull final Method method, @NonNull final String bucket, @NonNull final String name, @NonNull final Long time, @NonNull final TimeUnit unit) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(method, bucket, name, time, unit);

        try
        {
            if ((method == Method.GET) || (method == Method.HEAD))
            {
                return getMinioClient().presignedGetObject(bucket, name, MinioUtils.getDuration(time, unit));
            }
            if ((method == Method.PUT) || (method == Method.POST))
            {
                return getMinioClient().presignedPutObject(bucket, name, MinioUtils.getDuration(time, unit));
            }
            throw new MinioOperationException(String.format("invalid method %s", method));
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public boolean copyObject(@NonNull final String bucket, @NonNull final String name, @NonNull final String target, @Nullable final MinioCopyConditions conditions) throws MinioOperationException
    {
        return copyObject(bucket, name, target, MinioUtils.NULL(), conditions);
    }

    @Override
    public boolean copyObject(@NonNull final String bucket, @NonNull final String name, @NonNull final String target, @Nullable final String object, @Nullable final MinioCopyConditions conditions) throws MinioOperationException
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
                    getMinioClient().copyObject(bucket, name, target, (null == object) ? MinioUtils.NULL() : object, conditions.getCopyConditions());
                }
                else
                {
                    getMinioClient().copyObject(bucket, name, target, (null == object) ? MinioUtils.NULL() : object);
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
    public Stream<MinioUpload> getIncompleteUploads(@NonNull final String bucket, @Nullable final String prefix, final boolean recursive) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket);

        return MinioUtils.getResultAsStream(getMinioClient().listIncompleteUploads(bucket, prefix, recursive)).map(item -> new MinioUpload(item.objectName(), bucket, this));
    }

    @Override
    public void setBucketPolicy(@NonNull final String bucket, @NonNull final Object policy) throws MinioOperationException, MinioDataException
    {
        MinioUtils.isEachNonNull(bucket, policy);

        try
        {
            if (policy instanceof CharSequence)
            {
                getMinioClient().setBucketPolicy(bucket, policy.toString());
            }
            else
            {
                getMinioClient().setBucketPolicy(bucket, JSONUtils.toJSONString(policy));
            }
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public <T> T getBucketPolicy(@NonNull final String bucket, @NonNull final Class<T> type) throws MinioOperationException, MinioDataException
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
    public String getBucketPolicy(@NonNull final String bucket) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket);

        try
        {
            return MinioUtils.requireNonNull(getMinioClient().getBucketPolicy(bucket));
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public boolean removeUpload(@NonNull final String bucket, @NonNull final String name) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name);

        try
        {
            getMinioClient().removeIncompleteUpload(bucket, name);

            return true;
        }
        catch (final MinioException | NoSuchAlgorithmException | IOException | XmlPullParserException | InvalidKeyException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public MinioUserMetaData getUserMetaData(@NonNull final String bucket, @NonNull final String name) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name);

        try
        {
            return new MinioUserMetaData(getMinioClient().statObject(bucket, name).httpHeaders());
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public void setUserMetaData(@NonNull final String bucket, @NonNull final String name, @Nullable final MinioUserMetaData meta) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name);

        try
        {
            getMinioClient().copyObject(bucket, name, bucket, MinioUtils.NULL(), COPY_CONDS, (null == meta) ? MinioUtils.NULL() : meta.getUserMetaData());
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public void addUserMetaData(@NonNull final String bucket, @NonNull final String name, @Nullable final MinioUserMetaData meta) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name);

        if ((null != meta) && (false == meta.isEmpty()))
        {
            setUserMetaData(bucket, name, getUserMetaData(bucket, name).add(meta));
        }
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

    @Override
    public void putObject(@NonNull final String bucket, @NonNull final String name, @NonNull final byte[] input, @Nullable final String type, @Nullable final MinioUserMetaData meta) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name, input);

        try (ByteArrayInputStream is = new ByteArrayInputStream(input))
        {
            putObjectInputStream(bucket, name, is, new Long(is.available()), type, meta);
        }
        catch (final IOException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public void putObject(@NonNull final String bucket, @NonNull final String name, @NonNull final byte[] input, @Nullable final MinioUserMetaData meta) throws MinioOperationException
    {
        putObject(bucket, name, input, MinioUtils.NULL(), meta);
    }

    protected void putObjectInputStream(@NonNull final String bucket, @NonNull final String name, @NonNull final InputStream input, @Nullable final Long size, @Nullable final String type, @Nullable final MinioUserMetaData meta) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name, input);

        try
        {
            ensureBucket(bucket);

            if (null != size)
            {
                final Map<String, String> head = MinioUtils.toHeaderMap(meta);

                head.put("Content-Type", getContentTypeProbe().getContentType(type, name));

                getMinioClient().putObject(bucket, name, input, size, head);
            }
            else
            {
                getMinioClient().putObject(bucket, name, input, getContentTypeProbe().getContentType(type, name));

                if ((null != meta) && (false == meta.isEmpty()))
                {
                    final Map<String, String> data = meta.getUserMetaData();

                    if (false == data.isEmpty())
                    {
                        getMinioClient().copyObject(bucket, name, bucket, MinioUtils.NULL(), COPY_CONDS, data);
                    }
                }
            }
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public Stream<MinioBucket> findBuckets(@NonNull final Pattern regex) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(regex);

        return findBuckets(named -> regex.matcher(named).matches());
    }

    @NonNull
    @Override
    public Stream<MinioBucket> findBuckets(@NonNull final String regex) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(regex);

        return findBuckets(Pattern.compile(regex));
    }

    @NonNull
    @Override
    public Optional<MinioBucket> findBucket(@NonNull final String bucket) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket);

        return findBuckets(named -> bucket.equals(named)).findFirst();
    }

    @NonNull
    @Override
    public String getSignedObjectUrl(@NonNull final String bucket, @NonNull final String name) throws MinioOperationException
    {
        return getSignedObjectUrl(Method.GET, bucket, name);
    }

    @NonNull
    @Override
    public String getSignedObjectUrl(@NonNull final String bucket, @NonNull final String name, @NonNull final Long seconds) throws MinioOperationException
    {
        return getSignedObjectUrl(Method.GET, bucket, name, seconds);
    }

    @NonNull
    @Override
    public String getSignedObjectUrl(@NonNull final String bucket, @NonNull final String name, @NonNull final Duration seconds) throws MinioOperationException
    {
        return getSignedObjectUrl(Method.GET, bucket, name, seconds);
    }

    @NonNull
    @Override
    public String getSignedObjectUrl(@NonNull final String bucket, @NonNull final String name, @NonNull final Long time, @NonNull final TimeUnit unit) throws MinioOperationException
    {
        return getSignedObjectUrl(Method.GET, bucket, name, time, unit);
    }

    @Override
    public void putObject(@NonNull final String bucket, @NonNull final String name, @NonNull final byte[] input) throws MinioOperationException
    {
        putObject(bucket, name, input, MinioUtils.NULL(), MinioUtils.NULL());
    }

    @Override
    public void putObject(@NonNull final String bucket, @NonNull final String name, @NonNull final InputStream input) throws MinioOperationException
    {
        putObject(bucket, name, input, MinioUtils.NULL(), MinioUtils.NULL());
    }

    @Override
    public void putObject(@NonNull final String bucket, @NonNull final String name, @NonNull final Resource input) throws MinioOperationException
    {
        putObject(bucket, name, input, MinioUtils.NULL(), MinioUtils.NULL());
    }

    @Override
    public void putObject(@NonNull final String bucket, @NonNull final String name, @NonNull final File input) throws MinioOperationException
    {
        putObject(bucket, name, input, MinioUtils.NULL(), MinioUtils.NULL());
    }

    @Override
    public boolean copyObject(@NonNull final String bucket, @NonNull final String name, @NonNull final String target) throws MinioOperationException
    {
        return copyObject(bucket, name, target, MinioUtils.NULL(), MinioUtils.NULL());
    }

    @Override
    public boolean copyObject(@NonNull final String bucket, @NonNull final String name, @NonNull final String target, @Nullable final String object) throws MinioOperationException
    {
        return copyObject(bucket, name, target, object, MinioUtils.NULL());
    }

    @NonNull
    @Override
    public Stream<MinioItem> findItems(@NonNull final String bucket) throws MinioOperationException
    {
        return findItems(bucket, MinioUtils.NULL());
    }

    @NonNull
    @Override
    public Stream<MinioItem> findItems(@NonNull final String bucket, final boolean recursive) throws MinioOperationException
    {
        return findItems(bucket, MinioUtils.NULL(), recursive);
    }

    @NonNull
    @Override
    public Optional<MinioItem> findItem(@NonNull final String bucket, @NonNull final String name) throws MinioOperationException
    {
        return findItems(bucket, name, false).findFirst();
    }

    @NonNull
    @Override
    public Stream<MinioItem> findItems(@NonNull final String bucket, @Nullable final String prefix) throws MinioOperationException
    {
        return findItems(bucket, prefix, true);
    }

    @NonNull
    @Override
    public Stream<MinioUpload> getIncompleteUploads(@NonNull final String bucket) throws MinioOperationException
    {
        return getIncompleteUploads(bucket, MinioUtils.NULL());
    }

    @NonNull
    @Override
    public Stream<MinioUpload> getIncompleteUploads(@NonNull final String bucket, final boolean recursive) throws MinioOperationException
    {
        return getIncompleteUploads(bucket, MinioUtils.NULL(), recursive);
    }

    @NonNull
    @Override
    public Stream<MinioUpload> getIncompleteUploads(@NonNull final String bucket, @Nullable final String prefix) throws MinioOperationException
    {
        return getIncompleteUploads(bucket, prefix, true);
    }

    @Override
    public void deleteUserMetaData(@NonNull final String bucket, @NonNull final String name) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name);

        try
        {
            getMinioClient().copyObject(bucket, name, bucket, MinioUtils.NULL(), COPY_CONDS, MinioUtils.NULL());
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public void putObject(@NonNull final String bucket, @NonNull final String name, @NonNull final InputStream input, @Nullable final MinioUserMetaData meta) throws MinioOperationException
    {
        putObject(bucket, name, input, MinioUtils.NULL(), meta);
    }

    @Override
    public void putObject(final String bucket, final String name, final InputStream input, final String type, final MinioUserMetaData meta) throws MinioOperationException
    {
        putObjectInputStream(bucket, name, input, MinioUtils.NULL(), type, meta);
    }

    @Override
    public void putObject(final String bucket, final String name, final Resource input, final MinioUserMetaData meta) throws MinioOperationException
    {
        putObject(bucket, name, input, MinioUtils.NULL(), meta);
    }

    @Override
    public void putObject(final String bucket, final String name, final Resource input, final String type, final MinioUserMetaData meta) throws MinioOperationException
    {
        if (input.isFile())
        {
            try
            {
                putObject(bucket, name, input.getFile(), type, meta);
            }
            catch (final IOException e)
            {
                throw new MinioOperationException(e);
            }
        }
        else
        {
            try (final InputStream is = input.getInputStream())
            {
                putObjectInputStream(bucket, name, is, MinioUtils.NULL(), type, meta);
            }
            catch (final IOException e)
            {
                throw new MinioOperationException(e);
            }
        }
    }

    @Override
    public void putObject(final String bucket, final String name, final File input, final MinioUserMetaData meta) throws MinioOperationException
    {
        putObject(bucket, name, input, MinioUtils.NULL(), meta);
    }

    @Override
    public void putObject(final String bucket, final String name, final File input, final String type, final MinioUserMetaData meta) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name, input);

        try (final InputStream is = MinioUtils.getInputStream(input))
        {
            putObjectInputStream(bucket, name, is, input.length(), type, meta);
        }
        catch (final IOException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public void putObject(final String bucket, final String name, final Path input) throws MinioOperationException
    {
        putObject(bucket, name, input, MinioUtils.NULL(), MinioUtils.NULL());
    }

    @Override
    public void putObject(final String bucket, final String name, final Path input, final MinioUserMetaData meta) throws MinioOperationException
    {
        putObject(bucket, name, input, MinioUtils.NULL(), meta);
    }

    @Override
    public void putObject(final String bucket, final String name, final Path input, final String type, final MinioUserMetaData meta) throws MinioOperationException
    {
        putObject(bucket, name, input.toFile(), type, meta);
    }

    @Override
    public void putObject(final String bucket, final String name, final URL input) throws MinioOperationException
    {
        putObject(bucket, name, input, MinioUtils.NULL(), MinioUtils.NULL());
    }

    @Override
    public void putObject(final String bucket, final String name, final URL input, final String type) throws MinioOperationException
    {
        putObject(bucket, name, input, type, MinioUtils.NULL());
    }

    @Override
    public void putObject(final String bucket, final String name, final URL input, final MinioUserMetaData meta) throws MinioOperationException
    {
        putObject(bucket, name, input, MinioUtils.NULL(), meta);
    }

    @Override
    public void putObject(final String bucket, final String name, final URL input, final String type, final MinioUserMetaData meta) throws MinioOperationException
    {
        MinioUtils.isEachNonNull(bucket, name, input);

        if ("file".equalsIgnoreCase(input.getProtocol()))
        {
            try
            {
                putObject(bucket, name, Paths.get(input.toURI()), type, meta);
            }
            catch (final URISyntaxException e)
            {
                throw new MinioOperationException(e);
            }
        }
        else
        {
            try
            {
                final URLConnection conn = input.openConnection();

                final int size = conn.getContentLength();

                try (InputStream is = conn.getInputStream())
                {
                    putObjectInputStream(bucket, name, is, new Long(size), type, meta);
                }
                if (conn instanceof HttpURLConnection)
                {
                    MinioUtils.CAST(conn, HttpURLConnection.class).disconnect();
                }
            }
            catch (final IOException e)
            {
                throw new MinioOperationException(e);
            }
        }
    }
}
