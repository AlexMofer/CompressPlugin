Gradle Compress Plugin
=========

介绍
---

Gradle Compress Plugin为基于Apache Commons Compress的Gradle解压插件，用于解压大多数类型的归档型压缩文件及压缩型压缩文件

先决条件
----

无

入门
---

**引用:**
```
dependencies {
    ...
    classpath 'am.plugin.gradle:compress:1.0.1'
    ...
}
```


**实现:**

开启FTP服务器：

```
apply plugin: 'am.plugin.gradle.compress'
    
task compressTest(type: Compress) {
    sevenZ(file("../temp/test-7z.7z"), file1, file2, file3)
}
    
task extractTest(type: Extract) {
    unSevenZ(file("../test/Archives/Test.7z"), file("../temp/7z"))
    unSevenZ(file("../test/Archives/Test-Encrypted.7z"), file("../temp/7z-encrypted"), "123")
}
```


注意
---

- 压缩型压缩方式仅支持单个文件，不支持文件夹。
- 归档型压缩方式支持多个文件及文件夹。
- 默认已经添加xz支持库，因主打功能为压缩与解压7z文件。
- 归档型压缩文件的压缩方法分别为：ar、zip、tar、jar、cpio、sevenZ。
- 归档型压缩文件的解压通用方法为：unArchive；通用方法无法解压7z文档，7z格式专用的解压方法为：unSevenZ。
- 压缩型压缩文件的压缩方法分别为：bzip2、gzip、pack200、xz、lzma、deflate、snappy、lz4Block、lz4Framed、zStandard。
- zStandard需要[zstd](https://github.com/luben/zstd-jni)支持，默认没有加入该支持库。
- 虽然开放了pack200压缩型压缩文件的压缩方法，但是压缩后的文件存在问题，问题出在Apache Commons Compress包。
- 压缩型压缩文件的解压通用方法为：unCompress；通用方法无法解压lz4-block压缩文件，lz4-block压缩文件专用的解压方法为：unLz4Block，因为Apache Commons Compress无法通过流的数据头获取压缩包类型（不清楚是否为Bug）。
- 无法进行加密压缩，因为Apache Commons Compress不支持。
- 仅7z格式支持解压加密文档。
- 7z格式的加密文档需要设备有JDK环境，否则无法解压，并提示：Decryption error (do you have the JCE Unlimited Strength Jurisdiction Policy Files installed?)

支持
---

- Gmail: <mailto:moferalex@gmail.com>

如果发现错误，请在此处提出:
<https://github.com/AlexMofer/CompressPlugin/issues>

许可
---

Copyright (C) 2015 AlexMofer

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.