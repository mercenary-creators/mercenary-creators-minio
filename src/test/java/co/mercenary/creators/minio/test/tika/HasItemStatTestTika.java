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
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import co.mercenary.creators.minio.data.MinioObjectStatus;
import co.mercenary.creators.minio.errors.MinioOperationException;
import co.mercenary.creators.minio.util.AbstractMinioTests;

@SpringJUnitConfig(locations = "/tika-config.xml")
public class HasItemStatTestTika extends AbstractMinioTests
{
    @Test
    public void test() throws MinioOperationException
    {
        info(() -> getMinioTemplate().getContentTypeProbe().getClass().getName());

        final MinioObjectStatus stat = getMinioTemplate().getObjectStatus("root", "MinioProperties.java");

        info(() -> toJSONString(stat));

        assertEquals(stat.getContentType(), "text/x-java-source", () -> "not text/x-java-source");
    }
}
