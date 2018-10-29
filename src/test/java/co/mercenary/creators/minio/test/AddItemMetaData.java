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
import co.mercenary.creators.minio.util.AbstractMinioTests;
import co.mercenary.creators.minio.util.Ticker;

public class AddItemMetaData extends AbstractMinioTests
{
    @Test
    void test() throws Exception
    {
        final Ticker tick = getTicker();

        getOperations().setUserMetaData("root", "file.json", new MinioUserMetaData("test-meta", uuid()));

        info(() -> tick);

        tick.reset();

        getOperations().addUserMetaData("root", "file.json", new MinioUserMetaData("test-uuid", uuid()));

        info(() -> tick);

        tick.reset();

        final MinioObjectStatus stat = getOperations().getObjectStatus("root", "file.json");

        info(() -> tick);

        tick.reset();

        info(() -> stat);

        assertEquals(stat.getContentType(), "application/json", () -> "not application/json");
    }
}
