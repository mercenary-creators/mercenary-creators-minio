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

import org.junit.jupiter.api.Test;

import co.mercenary.creators.minio.data.MinioObjectStatus;
import co.mercenary.creators.minio.data.MinioUserMetaData;
import co.mercenary.creators.minio.json.JSONNode;
import co.mercenary.creators.minio.util.AbstractMinioTests;

public class PutItemMetaData extends AbstractMinioTests
{
    @Test
    void test() throws Exception
    {
        final JSONNode json = new JSONNode().add("name", "Dean Jones").add("year", 1963);

        getOperations().putObject("root", "zips.json", json.toByteArray(), new MinioUserMetaData("zips-meta", uuid()).add("zips-name", "Dean Jones  "));

        final MinioObjectStatus stat = getOperations().getObjectStatus("root", "zips.json");

        info(() -> stat);

        final MinioUserMetaData meta = getOperations().getUserMetaData("root", "zips.json");

        info(() -> meta);

        assertEquals(stat.getContentType(), "application/json", () -> "not application/json");
    }
}
