Mercenary Creators Minio
======

![<MERC>](http://docs.themodernway.com/merc5.png)

RATIONALE:


__Although I am grateful for the work done on the standard minio java client library, it has several problems:__

__It is not well suited for Spring. I have attempted to correct some of the issues by breaking up the API to the client through a set of interfaces for operations, and then a MinioTemplate bean.__

__The MinioClient class throws too many exceptions out of each method, some are as high as 9+ exception types! I am re-working the exception hierarchy to make it more meaningful.__

__The classes in minio data are badly suited for JSON/Jackson serialization. I have created wrapper classes for each ( Bucket, Item, ObjectStat, etc ). Furthermore, in some cases, String values of dates
in these objects can be null, resulting in a NPE when asking for a Date ( lastModified(), etc. ).__

__The wrapper classes ( MinioBucket, MinioItem ) have a method called withOperations(), where you now
get an API suited for that class. e.g. MinioBucket bucket.withOperations().getItems() gives you a
Java 8 Stream of MinioItem's.__

__The library uses Spring nullability annotations everywhere.__

__The library may consume and create Spring Resource classes.__

__I have updated all transitive dependencies to the newest versions.__

__I have also created a Spring Boot Autoconfiguration module available at:__

https://github.com/mercenary-creators/mercenary-creators-minio-autoconfigure

Downloading artifacts

RELEASE:

Maven:
```xml
<dependency>
  <groupId>co.mercenary-creators</groupId>
  <artifactId>mercenary-creators-minio</artifactId>
  <version>1.0.5-SNAPSHOT</version>
</dependency>
```
Gradle:
```
dependencies {
    compile(group: 'co.mercenary-creators', name: 'mercenary-creators-minio', version: '1.0.5-SNAPSHOT')
}
```
Javadoc URL:

http://docs.themodernway.com/docs/javadoc/mercenary-creators-minio/

Tests URL:

http://docs.themodernway.com/docs/reports/mercenary-creators-minio/tests/test/

Code Coverage URL:

http://docs.themodernway.com/docs/reports/mercenary-creators-minio/coverage/

Check Style URL:

http://docs.themodernway.com/docs/reports/mercenary-creators-minio/checkstyle/main.html

PMD URL:

http://docs.themodernway.com/docs/reports/mercenary-creators-minio/pmd/main.html

FindBugs URL:

http://docs.themodernway.com/docs/reports/mercenary-creators-minio/findbugs/main.html

CVE Dependency Check URL:

http://docs.themodernway.com/docs/reports/mercenary-creators-minio/cve/dependency-check-report.html

SonarQube URL:

https://sonarcloud.io/dashboard?id=co.mercenary-creators%3Amercenary-creators-minio

![alt SonarQube](https://sonarcloud.io/api/project_badges/measure?project=co.mercenary-creators%3Amercenary-creators-minio&metric=alert_status "SonarQube")

License:

Copyright (c) 2018, Mercenary Creators Company. All rights reserved.

Mercenary Creators Minio is released under version 2.0 of the Apache License.

http://www.apache.org/licenses/LICENSE-2.0.html

Author(s):

Dean S. Jones
deansjones@gmail.com
