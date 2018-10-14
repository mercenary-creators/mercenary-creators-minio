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

package co.mercenary.creators.minio.json;

import org.springframework.lang.NonNull;

import co.mercenary.creators.minio.util.MinioUtils;
import co.mercenary.creators.minio.util.WithStringValue;

public enum JSONType implements WithStringValue
{
    ARRAY("array"), OBJECT("object"), STRING("string"), NUMBER("number"), BOOLEAN("boolean"), DATE("date"), FUNCTION("function"), UNDEFINED("undefined"), NULL("null");

    @NonNull
    public static final String XMLNS_STRING = "jsonx";

    @NonNull
    public static final String ARRAY_STRING = "array";

    @NonNull
    public static final String VALUE_STRING = "value";

    @NonNull
    private final String       value;

    private JSONType(@NonNull final String value)
    {
        this.value = MinioUtils.requireNonNull(value);
    }

    @NonNull
    @Override
    public final String toStringValue()
    {
        return value;
    }
}
