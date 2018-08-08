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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.xmlpull.v1.XmlPullParserException;

import co.mercenary.creators.minio.data.MinioBucket;
import co.mercenary.creators.minio.data.MinioCopyConditions;
import co.mercenary.creators.minio.data.MinioItem;
import co.mercenary.creators.minio.data.MinioObjectStatus;
import co.mercenary.creators.minio.data.MinioUpload;
import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.errors.BucketPolicyTooLargeException;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidArgumentException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidEncryptionMetadataException;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidExpiresRangeException;
import io.minio.errors.InvalidObjectPrefixException;
import io.minio.errors.InvalidPortException;
import io.minio.errors.NoResponseException;
import io.minio.errors.RegionConflictException;

public class MinioTemplate implements MinioOperations
{
    @NonNull
    private final MinioClient m_client;

    public MinioTemplate(@NonNull final CharSequence server, @Nullable final CharSequence access, @Nullable final CharSequence secret, @Nullable final CharSequence region) throws MinioOperationException
    {
        try
        {
            m_client = new MinioClient(MinioUtils.requireToString(server), MinioUtils.getCharSequence(access), MinioUtils.getCharSequence(secret), MinioUtils.getCharSequence(region));
        }
        catch (InvalidEndpointException | InvalidPortException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    protected MinioClient getMinioClient() throws MinioOperationException
    {
        return m_client;
    }

    @Override
    public boolean isBucket(@NonNull final CharSequence bucket) throws MinioOperationException
    {
        try
        {
            return getMinioClient().bucketExists(MinioUtils.requireToString(bucket));
        }
        catch (InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | NoResponseException | ErrorResponseException | InternalException | IOException | XmlPullParserException e)
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
            catch (InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | NoResponseException | ErrorResponseException | InternalException | IOException | XmlPullParserException e)
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
            catch (InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | NoResponseException | ErrorResponseException | InternalException | IOException | XmlPullParserException | InvalidArgumentException e)
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
            catch (InvalidKeyException | InvalidBucketNameException | RegionConflictException | NoSuchAlgorithmException | InsufficientDataException | NoResponseException | ErrorResponseException | InternalException | IOException | XmlPullParserException e)
            {
                throw new MinioOperationException(e);
            }
        }
        return getBucket(bucket).orElseThrow(() -> new MinioOperationException(String.format("no bucket %s", bucket)));
    }

    @NonNull
    @Override
    public Stream<MinioBucket> getBuckets() throws MinioOperationException
    {
        try
        {
            return getMinioClient().listBuckets().stream().map(bucket -> new MinioBucket(bucket.name(), () -> bucket.creationDate(), this));
        }
        catch (InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | NoResponseException | ErrorResponseException | InternalException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public Stream<MinioBucket> getBucketsNamed(@NonNull final Predicate<String> filter) throws MinioOperationException
    {
        try
        {
            return getMinioClient().listBuckets().stream().filter(bucket -> filter.test(bucket.name())).map(bucket -> new MinioBucket(bucket.name(), () -> bucket.creationDate(), this));
        }
        catch (InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | NoResponseException | ErrorResponseException | InternalException | IOException | XmlPullParserException e)
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
        catch (InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | NoResponseException | ErrorResponseException | InternalException | InvalidArgumentException | IOException | XmlPullParserException e)
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
        catch (InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | NoResponseException | ErrorResponseException | InternalException | InvalidArgumentException | IOException | XmlPullParserException e)
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
        catch (InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | NoResponseException | ErrorResponseException | InternalException | InvalidArgumentException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public InputStream getObjectInputStream(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final KeyPair keys) throws MinioOperationException
    {
        try
        {
            return getMinioClient().getObject(MinioUtils.requireToString(bucket), MinioUtils.requireToString(name), MinioUtils.requireNonNull(keys));
        }
        catch (InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | NoResponseException | ErrorResponseException | InternalException | InvalidArgumentException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException | InvalidEncryptionMetadataException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public InputStream getObjectInputStream(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final SecretKey keys) throws MinioOperationException
    {
        try
        {
            return getMinioClient().getObject(MinioUtils.requireToString(bucket), MinioUtils.requireToString(name), MinioUtils.requireNonNull(keys));
        }
        catch (InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | NoResponseException | ErrorResponseException | InternalException | InvalidArgumentException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException | InvalidEncryptionMetadataException | IOException | XmlPullParserException e)
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
        catch (InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | NoResponseException | ErrorResponseException | InternalException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final InputStream input, @Nullable final CharSequence type) throws MinioOperationException
    {
        MinioUtils.requireToString(name);

        MinioUtils.requireNonNull(input);

        final MinioBucket create = createOrGetBucket(bucket);

        try
        {
            getMinioClient().putObject(create.getName(), MinioUtils.getCharSequence(name), input, MinioUtils.requireToStringOrElse(type, MinioUtils.getDefaultContentType()));
        }
        catch (InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | NoResponseException | ErrorResponseException | InternalException | InvalidArgumentException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final InputStream input, final long size, @Nullable final CharSequence type) throws MinioOperationException
    {
        MinioUtils.requireToString(name);

        MinioUtils.requireNonNull(input);

        final MinioBucket create = createOrGetBucket(bucket);

        try
        {
            getMinioClient().putObject(create.getName(), MinioUtils.getCharSequence(name), input, size, MinioUtils.requireToStringOrElse(type, MinioUtils.getDefaultContentType()));
        }
        catch (InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | NoResponseException | ErrorResponseException | InternalException | InvalidArgumentException | IOException | XmlPullParserException e)
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

    @NonNull
    @Override
    public String getBucketPolicy(@NonNull final CharSequence bucket) throws MinioOperationException
    {
        try
        {
            return getMinioClient().getBucketPolicy(MinioUtils.requireToString(bucket));
        }
        catch (InvalidKeyException | InvalidBucketNameException | InvalidObjectPrefixException | NoSuchAlgorithmException | InsufficientDataException | NoResponseException | ErrorResponseException | InternalException | BucketPolicyTooLargeException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public void setBucketPolicy(@NonNull final CharSequence bucket, @NonNull final CharSequence policy) throws MinioOperationException
    {
        try
        {
            getMinioClient().setBucketPolicy(MinioUtils.requireToString(bucket), MinioUtils.requireToString(policy));
        }
        catch (InvalidKeyException | InvalidBucketNameException | InvalidObjectPrefixException | NoSuchAlgorithmException | InsufficientDataException | NoResponseException | ErrorResponseException | InternalException | IOException | XmlPullParserException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final Resource resource, @Nullable final CharSequence type) throws MinioOperationException
    {
        if (resource.isFile())
        {
            try
            {
                putObject(bucket, name, resource.getFile(), type);
            }
            catch (final IOException e)
            {
                throw new MinioOperationException(e);
            }
        }
        else
        {
            try (final InputStream is = resource.getInputStream())
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
    public void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final File file, @Nullable final CharSequence type) throws MinioOperationException
    {
        try (final InputStream is = MinioUtils.getInputStream(file))
        {
            putObject(bucket, name, is, MinioUtils.getSize(file), type);
        }
        catch (final IOException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final Path path, @Nullable final CharSequence type) throws MinioOperationException
    {
        try (final InputStream is = MinioUtils.getInputStream(path))
        {
            putObject(bucket, name, is, MinioUtils.getSize(path), type);
        }
        catch (final IOException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final File file, final long size, @Nullable final CharSequence type) throws MinioOperationException
    {
        try (final InputStream is = MinioUtils.getInputStream(file))
        {
            putObject(bucket, name, is, size, type);
        }
        catch (final IOException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final Path path, final long size, @Nullable final CharSequence type) throws MinioOperationException
    {
        try (final InputStream is = MinioUtils.getInputStream(path))
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
    public String getSignedObjectUrl(@NonNull final MinioMethod method, @NonNull final CharSequence bucket, @NonNull final CharSequence name) throws MinioOperationException
    {
        try
        {
            if ((method == MinioMethod.GET) || (method == MinioMethod.HEAD))
            {
                return getMinioClient().presignedGetObject(MinioUtils.requireToString(bucket), MinioUtils.requireToString(name));
            }
            if ((method == MinioMethod.PUT) || (method == MinioMethod.POST))
            {
                return getMinioClient().presignedPutObject(MinioUtils.requireToString(bucket), MinioUtils.requireToString(name));
            }
            throw new MinioOperationException(String.format("invalid method %s", method));
        }
        catch (InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException | NoResponseException | XmlPullParserException | ErrorResponseException | InternalException | InvalidExpiresRangeException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public String getSignedObjectUrl(@NonNull final MinioMethod method, @NonNull final CharSequence bucket, @NonNull final CharSequence name, final long seconds) throws MinioOperationException
    {
        try
        {
            if ((method == MinioMethod.GET) || (method == MinioMethod.HEAD))
            {
                return getMinioClient().presignedGetObject(MinioUtils.requireToString(bucket), MinioUtils.requireToString(name), MinioUtils.getDuration(seconds));
            }
            if ((method == MinioMethod.PUT) || (method == MinioMethod.POST))
            {
                return getMinioClient().presignedPutObject(MinioUtils.requireToString(bucket), MinioUtils.requireToString(name), MinioUtils.getDuration(seconds));
            }
            throw new MinioOperationException(String.format("invalid method %s", method));
        }
        catch (InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException | NoResponseException | XmlPullParserException | ErrorResponseException | InternalException | InvalidExpiresRangeException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public String getSignedObjectUrl(@NonNull final MinioMethod method, @NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final Duration seconds) throws MinioOperationException
    {
        try
        {
            if ((method == MinioMethod.GET) || (method == MinioMethod.HEAD))
            {
                return getMinioClient().presignedGetObject(MinioUtils.requireToString(bucket), MinioUtils.requireToString(name), MinioUtils.getDuration(seconds));
            }
            if ((method == MinioMethod.PUT) || (method == MinioMethod.POST))
            {
                return getMinioClient().presignedPutObject(MinioUtils.requireToString(bucket), MinioUtils.requireToString(name), MinioUtils.getDuration(seconds));
            }
            throw new MinioOperationException(String.format("invalid method %s", method));
        }
        catch (InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException | NoResponseException | XmlPullParserException | ErrorResponseException | InternalException | InvalidExpiresRangeException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @NonNull
    @Override
    public String getSignedObjectUrl(@NonNull final MinioMethod method, @NonNull final CharSequence bucket, @NonNull final CharSequence name, final long time, @NonNull final TimeUnit unit) throws MinioOperationException
    {
        try
        {
            if ((method == MinioMethod.GET) || (method == MinioMethod.HEAD))
            {
                return getMinioClient().presignedGetObject(MinioUtils.requireToString(bucket), MinioUtils.requireToString(name), MinioUtils.getDuration(time, unit));
            }
            if ((method == MinioMethod.PUT) || (method == MinioMethod.POST))
            {
                return getMinioClient().presignedPutObject(MinioUtils.requireToString(bucket), MinioUtils.requireToString(name), MinioUtils.getDuration(time, unit));
            }
            throw new MinioOperationException(String.format("invalid method %s", method));
        }
        catch (InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException | NoResponseException | XmlPullParserException | ErrorResponseException | InternalException | InvalidExpiresRangeException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public boolean copyObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final CharSequence destbucket, @Nullable final MinioCopyConditions conditions) throws MinioOperationException
    {
        MinioUtils.requireToString(destbucket);

        if (isObject(bucket, name))
        {
            try
            {
                if ((null != conditions) && (false == conditions.isEmpty()))
                {
                    getMinioClient().copyObject(MinioUtils.getCharSequence(bucket), MinioUtils.getCharSequence(name), createOrGetBucket(destbucket).getName(), MinioUtils.getCharSequence(null), conditions.getCopyConditions());
                }
                else
                {
                    getMinioClient().copyObject(MinioUtils.getCharSequence(bucket), MinioUtils.getCharSequence(name), createOrGetBucket(destbucket).getName(), MinioUtils.getCharSequence(null));
                }
                return true;
            }
            catch (InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException | NoResponseException | XmlPullParserException | ErrorResponseException | InternalException | InvalidArgumentException e)
            {
                throw new MinioOperationException(e);
            }
        }
        return false;
    }

    @Override
    public boolean copyObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final CharSequence destbucket, @Nullable final CharSequence destobject, @Nullable final MinioCopyConditions conditions) throws MinioOperationException
    {
        MinioUtils.requireToString(destbucket);

        if (isObject(bucket, name))
        {
            try
            {
                if ((null != conditions) && (false == conditions.isEmpty()))
                {
                    getMinioClient().copyObject(MinioUtils.getCharSequence(bucket), MinioUtils.getCharSequence(name), createOrGetBucket(destbucket).getName(), MinioUtils.getCharSequence(destobject), conditions.getCopyConditions());
                }
                else
                {
                    getMinioClient().copyObject(MinioUtils.getCharSequence(bucket), MinioUtils.getCharSequence(name), createOrGetBucket(destbucket).getName(), MinioUtils.getCharSequence(destobject));
                }
                return true;
            }
            catch (InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | IOException | InvalidKeyException | NoResponseException | XmlPullParserException | ErrorResponseException | InternalException | InvalidArgumentException e)
            {
                throw new MinioOperationException(e);
            }
        }
        return false;
    }

    @Override
    public void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final InputStream input, final long size, @Nullable final CharSequence type, @NonNull final KeyPair skeys) throws MinioOperationException
    {
        MinioUtils.requireToString(name);

        MinioUtils.requireNonNull(input);

        MinioUtils.requireNonNull(skeys);

        final MinioBucket create = createOrGetBucket(bucket);

        try
        {
            getMinioClient().putObject(create.getName(), MinioUtils.getCharSequence(name), input, size, MinioUtils.requireToStringOrElse(type, MinioUtils.getDefaultContentType()), skeys);
        }
        catch (InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | NoResponseException | ErrorResponseException | InternalException | InvalidArgumentException | IOException | XmlPullParserException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e)
        {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public void putObject(@NonNull final CharSequence bucket, @NonNull final CharSequence name, @NonNull final InputStream input, final long size, @Nullable final CharSequence type, @NonNull final SecretKey ckeys) throws MinioOperationException
    {
        MinioUtils.requireToString(name);

        MinioUtils.requireNonNull(input);

        MinioUtils.requireNonNull(ckeys);

        final MinioBucket create = createOrGetBucket(bucket);

        try
        {
            getMinioClient().putObject(create.getName(), MinioUtils.getCharSequence(name), input, size, MinioUtils.requireToStringOrElse(type, MinioUtils.getDefaultContentType()), ckeys);
        }
        catch (InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException | NoResponseException | ErrorResponseException | InternalException | InvalidArgumentException | IOException | XmlPullParserException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e)
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
}
