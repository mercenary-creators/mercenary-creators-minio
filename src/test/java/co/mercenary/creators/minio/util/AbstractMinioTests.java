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

package co.mercenary.creators.minio.util;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import co.mercenary.creators.minio.MinioOperations;
import co.mercenary.creators.minio.MinioTemplate;
import co.mercenary.creators.minio.MinioTestConfig;
import co.mercenary.creators.minio.errors.MinioDataException;
import co.mercenary.creators.minio.logging.impl.AbstractWithLogger;

@SpringJUnitConfig(MinioTestConfig.class)
@TestPropertySource("file:/opt/development/properties/mercenary-creators-minio/minio-test.properties")
public abstract class AbstractMinioTests extends AbstractWithLogger
{
    @Nullable
    @Autowired
    private MinioTemplate minioTemplate;

    @Nullable
    protected MinioTemplate getMinioTemplate()
    {
        return minioTemplate;
    }

    @Nullable
    protected MinioOperations getMinioOperations()
    {
        return getMinioTemplate();
    }

    @NonNull
    protected Resource getResource()
    {
        return new ClassPathResource("management.json", AbstractMinioTests.class);
    }

    @NonNull
    protected String uuid()
    {
        return UUID.randomUUID().toString();
    }

    @BeforeEach
    protected void doBeforeEachTest()
    {
        info(() -> getMinioOperations().getContentTypeProbe().getClass().getName());

        info(() -> logger());
    }

    @NonNull
    protected Supplier<String> isEmptyMessage(@NonNull final String message)
    {
        return isEmptyMessage(() -> message);
    }

    @NonNull
    protected Supplier<String> isEmptyMessage(@NonNull final Supplier<String> message)
    {
        return () -> message.get() + " is empty.";
    }

    @NonNull
    @SuppressWarnings("unchecked")
    protected <T> List<T> toList(@NonNull final T... source)
    {
        return MinioUtils.toList(source);
    }

    @NonNull
    protected <T> List<T> toList(@NonNull final Stream<T> source)
    {
        return MinioUtils.toList(source);
    }

    @NonNull
    protected String toJSONString(@NonNull final Object value)
    {
        return toJSONString(value, true);
    }

    @NonNull
    protected String toJSONString(@NonNull final Object value, final boolean pretty)
    {
        try
        {
            return MinioUtils.toJSONString(value, pretty);
        }
        catch (final MinioDataException e)
        {
            throw new AssertionError("toJSONString", e);
        }
    }

    @NonNull
    protected <T> T toJSONObject(@NonNull final CharSequence value, @NonNull final Class<T> type)
    {
        try
        {
            return MinioUtils.toJSONObject(value, type);
        }
        catch (final MinioDataException e)
        {
            throw new AssertionError("toJSONObject", e);
        }
    }

    protected void assertEquals(@Nullable final Object expected, @Nullable final Object actual, @NonNull final Supplier<?> message)
    {
        Assertions.assertEquals(expected, actual, () -> message.get().toString());
    }

    protected void assertTrue(final boolean condition, @NonNull final Supplier<?> message)
    {
        Assertions.assertTrue(condition, () -> message.get().toString());
    }

    protected void assertFalse(final boolean condition, @NonNull final Supplier<?> message)
    {
        Assertions.assertFalse(condition, () -> message.get().toString());
    }
}
