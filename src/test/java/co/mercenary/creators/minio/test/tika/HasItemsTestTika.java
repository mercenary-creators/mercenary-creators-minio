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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import co.mercenary.creators.minio.data.MinioItem;
import co.mercenary.creators.minio.util.AbstractMinioTests;

@TestPropertySource(properties = "co.mercenary.creators.minio.content-type-probe.name=tika")
public class HasItemsTestTika extends AbstractMinioTests
{
    @Test
    void test() throws Exception
    {
        final List<MinioItem> list = forInfo(getOperations().findItems("root"));

        assertFalse(list.isEmpty(), isEmptyMessage("items"));
    }
}
