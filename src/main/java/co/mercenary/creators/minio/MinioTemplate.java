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
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.xmlpull.v1.XmlPullParserException;

import co.mercenary.creators.minio.data.MinioBucket;
import co.mercenary.creators.minio.data.MinioCopyConditions;
import co.mercenary.creators.minio.data.MinioItem;
import co.mercenary.creators.minio.data.MinioObjectStatus;
import co.mercenary.creators.minio.data.MinioUpload;
import co.mercenary.creators.minio.errors.MinioOperationException;
import co.mercenary.creators.minio.util.AbstractNamed;
import co.mercenary.creators.minio.util.MinioUtils;
import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.ServerSideEncryption;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import io.minio.errors.MinioException;
import io.minio.http.Method;

public class MinioTemplate extends AbstractNamed implements MinioOperations, BeanNameAware
{
    @NonNull
    private final String                       server_url;

    @Nullable
    private final String                       access_key;

    @Nullable
    private final String                       secret_key;

    @NonNull
    private final AtomicBoolean                was_opened = new AtomicBoolean(false);

    @NonNull
    private final AtomicBoolean                was_closed = new AtomicBoolean(false);

    @NonNull
    private final AtomicReference<MinioClient> atomic_ref = new AtomicReference<>(MinioUtils.NULL());

    public MinioTemplate(@NonNull final CharSequence server)
    {
        this(server, MinioUtils.NULL(), MinioUtils.NULL());
    }

    public MinioTemplate(@NonNull final CharSequence server, @Nullable final CharSequence access, @Nullable final CharSequence secret)
    {
        super(MinioUtils.uuid());

        this.server_url = MinioUtils.requireToString(server);

        this.access_key = MinioUtils.getCharSequence(access);

        this.secret_key = MinioUtils.getCharSequence(secret);
    }

    @Override
    public boolean isOpen()
    {
        return was_opened.get();
    }

    @Override
    public boolean isClosed()
    {
        return was_closed.get();
    }

    @Override
    public void close() throws IOException
    {
        was_closed.compareAndSet(false, true);

        was_opened.compareAndSet(true, false);
    }

    @NonNull
    @Override
    public String getServerUrl()
    {
        return server_url;
    }

    @Nullable
    protected String getAccessKey()
    {
        return access_key;
    }

    @Nullable
    protected String getSecretKey()
    {
        return secret_key;
    }

    @NonNull
    protected MinioClient getMinioClient() throws MinioOperationException
    {
        if (isClosed())
        {
            was_opened.set(false);

            throw new MinioOperationException(MinioUtils.format("%s is closed.", getName()));
        }
        MinioClient client = atomic_ref.get();

        if (null == client)
        {
            synchronized (this)
            {
                client = atomic_ref.get();

                if (null == client)
                {
                    try
                    {
                        atomic_ref.set(new MinioClient(getServerUrl(), getAccessKey(), getSecretKey()));

                        was_opened.set(true);

                        client = atomic_ref.get();
                    }
                    catch (InvalidEndpointException | InvalidPortException e)
                    {
                        throw new MinioOperationException(e);
                    }
                }
            }
        }
        if (isClosed())
        {
            was_opened.set(false);

            throw new MinioOperationException(MinioUtils.format("%s is closed.", getName()));
        }
        return client;
    }

    @Override
    public void setBeanName(final String name)
    {
        setName(MinioUtils.requireToStringOrElse(name, getName()));
    }

    @Override
    public boolean isBucket(@NonNull final CharSequence bucket) throws MinioOperationException
    {
        try
        {
            return getMinioClient().bucketExists(MinioUtils.requireToString(bucket));
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public boolean deleteBucket(@NonNull final CharSequence bucket) throws MinioOperationException
    {
        if (isBucket(bucket))
        {
            try
            {
                getMinioClient().removeBucket(MinioUtils.getCharSequence(bucket));

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
        try
        {
            return getObjectStatus(bucket, name).getBucket().contentEquals(bucket);
        }
        catch (final MinioOperationException e)
        {
            return false;
        }
    }

    @Override
    public boolean deleteObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name) throws MinioOperationException
    {
        if (isBucket(bucket))
        {
            try
            {
                getMinioClient().removeObject(MinioUtils.getCharSequence(bucket), MinioUtils.requireToString(name));

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
    public MinioBucket createOrGetBucket(@NonNull final CharSequence bucket) throws MinioOperationException
    {
        if (false == isBucket(bucket))
        {
            try
            {
                getMinioClient().makeBucket(MinioUtils.getCharSequence(bucket));
            }
            catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
            {
                throw new MinioOperationException(e);
            }
        }
        return getBucket(bucket).orElseThrow(() -> new MinioOperationException(MinioUtils.format("no bucket %s", bucket)));
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
        MinioUtils.testAllNonNull(filter);

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
        try
        {
            return getMinioClient().getObject(MinioUtils.requireToString(bucket), MinioUtils.requireToString(name));
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
        try
        {
            return getMinioClient().getObject(MinioUtils.requireToString(bucket), MinioUtils.requireToString(name), skip);
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
        try
        {
            return getMinioClient().getObject(MinioUtils.requireToString(bucket), MinioUtils.requireToString(name), skip, leng);
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
        try
        {
            return getMinioClient().getObject(MinioUtils.requireToString(bucket), MinioUtils.requireToString(name), MinioUtils.requireNonNull(keys));
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
        try
        {
            final ObjectStat status = getMinioClient().statObject(MinioUtils.requireToString(bucket), MinioUtils.requireToString(name));

            return new MinioObjectStatus(name, bucket, status.length(), status.contentType(), status.etag(), () -> status.createdTime());
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
        try
        {
            final ObjectStat status = getMinioClient().statObject(MinioUtils.requireToString(bucket), MinioUtils.requireToString(name), MinioUtils.requireNonNull(keys));

            return new MinioObjectStatus(name, bucket, status.length(), status.contentType(), status.etag(), () -> status.createdTime());
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final InputStream input, @Nullable final CharSequence type) throws MinioOperationException
    {
        MinioUtils.testAllNonNull(bucket, name, input);

        final MinioBucket create = createOrGetBucket(bucket);

        try
        {
            getMinioClient().putObject(create.getName(), MinioUtils.getCharSequence(name), input, MinioUtils.fixContentType(type));
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final InputStream input, final long size, @Nullable final CharSequence type) throws MinioOperationException
    {
        MinioUtils.testAllNonNull(bucket, name, input);

        final MinioBucket create = createOrGetBucket(bucket);

        try
        {
            getMinioClient().putObject(create.getName(), MinioUtils.getCharSequence(name), input, size, MinioUtils.fixContentType(type));
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final byte[] input, @Nullable final CharSequence type) throws MinioOperationException
    {
        MinioUtils.testAllNonNull(bucket, name, input);

        try (final ByteArrayInputStream baos = new ByteArrayInputStream(input))
        {
            putObject(bucket, name, baos, baos.available(), type);
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
        return MinioUtils.getResultAsStream(getMinioClient().listObjects(MinioUtils.requireToString(bucket), MinioUtils.getCharSequence(prefix), recursive)).map(item -> new MinioItem(item.objectName(), bucket, item.size(), !item.isDir(), item.etag(), () -> item.lastModified(), this));
    }

    @Override
    public void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final Resource input, @Nullable final CharSequence type) throws MinioOperationException
    {
        if (input.isFile())
        {
            try
            {
                putObject(bucket, name, input.getFile(), type);
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
                putObject(bucket, name, is, type);
            }
            catch (final IOException e)
            {
                throw new MinioOperationException(e);
            }
        }
    }

    @Override
    public void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final File input, @Nullable final CharSequence type) throws MinioOperationException
    {
        try (final InputStream is = MinioUtils.getInputStream(input))
        {
            putObject(bucket, name, is, MinioUtils.getSize(input), type);
        }
        catch (final IOException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final Path input, @Nullable final CharSequence type) throws MinioOperationException
    {
        try (final InputStream is = MinioUtils.getInputStream(input))
        {
            putObject(bucket, name, is, MinioUtils.getSize(input), type);
        }
        catch (final IOException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final File input, final long size, @Nullable final CharSequence type) throws MinioOperationException
    {
        try (final InputStream is = MinioUtils.getInputStream(input))
        {
            putObject(bucket, name, is, size, type);
        }
        catch (final IOException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final Path input, final long size, @Nullable final CharSequence type) throws MinioOperationException
    {
        try (final InputStream is = MinioUtils.getInputStream(input))
        {
            putObject(bucket, name, is, size, type);
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
        MinioUtils.testAllNonNull(method, bucket, name);

        try
        {
            if ((method == Method.GET) || (method == Method.HEAD))
            {
                return getMinioClient().presignedGetObject(MinioUtils.requireToString(bucket), MinioUtils.requireToString(name));
            }
            if ((method == Method.PUT) || (method == Method.POST))
            {
                return getMinioClient().presignedPutObject(MinioUtils.requireToString(bucket), MinioUtils.requireToString(name));
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
        MinioUtils.testAllNonNull(method, bucket, name, seconds);

        try
        {
            if ((method == Method.GET) || (method == Method.HEAD))
            {
                return getMinioClient().presignedGetObject(MinioUtils.requireToString(bucket), MinioUtils.requireToString(name), MinioUtils.getDuration(seconds));
            }
            if ((method == Method.PUT) || (method == Method.POST))
            {
                return getMinioClient().presignedPutObject(MinioUtils.requireToString(bucket), MinioUtils.requireToString(name), MinioUtils.getDuration(seconds));
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
        MinioUtils.testAllNonNull(method, bucket, name, seconds);

        try
        {
            if ((method == Method.GET) || (method == Method.HEAD))
            {
                return getMinioClient().presignedGetObject(MinioUtils.requireToString(bucket), MinioUtils.requireToString(name), MinioUtils.getDuration(seconds));
            }
            if ((method == Method.PUT) || (method == Method.POST))
            {
                return getMinioClient().presignedPutObject(MinioUtils.requireToString(bucket), MinioUtils.requireToString(name), MinioUtils.getDuration(seconds));
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
        MinioUtils.testAllNonNull(method, bucket, name, time, unit);

        try
        {
            if ((method == Method.GET) || (method == Method.HEAD))
            {
                return getMinioClient().presignedGetObject(MinioUtils.requireToString(bucket), MinioUtils.requireToString(name), MinioUtils.getDuration(time, unit));
            }
            if ((method == Method.PUT) || (method == Method.POST))
            {
                return getMinioClient().presignedPutObject(MinioUtils.requireToString(bucket), MinioUtils.requireToString(name), MinioUtils.getDuration(time, unit));
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
        MinioUtils.testAllNonNull(bucket, name, target);

        if (isObject(bucket, name))
        {
            try
            {
                if ((null != conditions) && (false == conditions.isEmpty()))
                {
                    getMinioClient().copyObject(MinioUtils.getCharSequence(bucket), MinioUtils.getCharSequence(name), createOrGetBucket(target).getName(), MinioUtils.getCharSequence(MinioUtils.NULL()), conditions.getCopyConditions());
                }
                else
                {
                    getMinioClient().copyObject(MinioUtils.getCharSequence(bucket), MinioUtils.getCharSequence(name), createOrGetBucket(target).getName(), MinioUtils.getCharSequence(MinioUtils.NULL()));
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

    @Override
    public boolean copyObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final CharSequence target, @Nullable final CharSequence object, @Nullable final MinioCopyConditions conditions) throws MinioOperationException
    {
        MinioUtils.testAllNonNull(bucket, name, target);

        if (isObject(bucket, name))
        {
            try
            {
                if ((null != conditions) && (false == conditions.isEmpty()))
                {
                    getMinioClient().copyObject(MinioUtils.getCharSequence(bucket), MinioUtils.getCharSequence(name), createOrGetBucket(target).getName(), MinioUtils.getCharSequence(object), conditions.getCopyConditions());
                }
                else
                {
                    getMinioClient().copyObject(MinioUtils.getCharSequence(bucket), MinioUtils.getCharSequence(name), createOrGetBucket(target).getName(), MinioUtils.getCharSequence(object));
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

    @Override
    public void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final InputStream input, final long size, @NonNull final ServerSideEncryption keys) throws MinioOperationException
    {
        MinioUtils.testAllNonNull(bucket, name, input, keys);

        final MinioBucket create = createOrGetBucket(bucket);

        try
        {
            getMinioClient().putObject(create.getName(), MinioUtils.getCharSequence(name), input, size, keys);
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public Stream<MinioUpload> getIncompleteUploads(@NonNull final CharSequence bucket, @Nullable final CharSequence prefix, final boolean recursive) throws MinioOperationException
    {
        return MinioUtils.getResultAsStream(getMinioClient().listIncompleteUploads(MinioUtils.requireToString(bucket), MinioUtils.getCharSequence(prefix), recursive)).map(item -> new MinioUpload(item.objectName(), bucket));
    }

    @Override
    public void setBucketPolicy(@NonNull final CharSequence bucket, @NonNull final Object policy) throws MinioOperationException
    {
        MinioUtils.testAllNonNull(bucket, policy);

        try
        {
            if (policy instanceof CharSequence)
            {
                getMinioClient().setBucketPolicy(MinioUtils.requireToString(bucket), policy.toString());
            }
            else
            {
                getMinioClient().setBucketPolicy(MinioUtils.requireToString(bucket), MinioUtils.toJSONString(policy));
            }
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public <T> T getBucketPolicy(@NonNull final CharSequence bucket, @NonNull final Class<T> type) throws MinioOperationException
    {
        MinioUtils.requireNonNull(type);

        final String policy = getBucketPolicy(bucket);

        if ((String.class == type) || (CharSequence.class == type))
        {
            final T value = MinioUtils.CAST(policy, type);

            return MinioUtils.requireNonNull(value);
        }
        return MinioUtils.toJSONObject(policy, type);
    }

    @NonNull
    @Override
    public String getBucketPolicy(@NonNull final CharSequence bucket) throws MinioOperationException
    {
        try
        {
            return getMinioClient().getBucketPolicy(MinioUtils.requireToString(bucket));
        }
        catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }
}
