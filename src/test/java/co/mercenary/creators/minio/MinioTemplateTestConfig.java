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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

@Configuration
public class MinioTemplateTestConfig
{
    @Value("${co.mercenary.creators.minio.server-url}")
    private String server;

    @Value("${co.mercenary.creators.minio.access-key}")
    private String access;

    @Value("${co.mercenary.creators.minio.secret-key}")
    private String secret;

    @Value("${co.mercenary.creators.minio.aws-region:us-east-1}")
    private String region;

    @Bean
    @NonNull
    public MinioTemplate minioTemplate()
    {
        return new MinioTemplate(server, access, secret, region);
    }
}
