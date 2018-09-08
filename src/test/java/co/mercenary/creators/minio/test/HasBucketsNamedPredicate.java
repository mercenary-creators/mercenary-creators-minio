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

package co.mercenary.creators.minio.test;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import co.mercenary.creators.minio.data.MinioBucket;
import co.mercenary.creators.minio.errors.MinioOperationException;
import co.mercenary.creators.minio.util.AbstractMinioTests;

@SpringJUnitConfig(locations = "/test-config.xml")
public class HasBucketsNamedPredicate extends AbstractMinioTests
{
    @Test
    public void test() throws MinioOperationException
    {
        final List<MinioBucket> list = toList(getMinioTemplate().getBucketsNamed(value -> value.equals("root")));

        list.forEach(value -> info(() -> toJSONString(value)));

        assertFalse(list.isEmpty(), isEmptyMessage("buckets"));
    }
}