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

package co.mercenary.creators.minio.test.tika;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import co.mercenary.creators.minio.content.tika.MinioContentTypeProbeTikaAdapter;
import co.mercenary.creators.minio.util.AbstractMinioTests;

@TestPropertySource(properties = "minio.content-type-probe.name=tika")
public class HasBeanTestTika extends AbstractMinioTests
{
    @Test
    void test()
    {
        final String name = MinioContentTypeProbeTikaAdapter.class.getName();

        final String prop = getOperations().getContentTypeProbe().getClass().getName();

        info(() -> prop);

        assertEquals(name, prop, () -> name);
    }
}
