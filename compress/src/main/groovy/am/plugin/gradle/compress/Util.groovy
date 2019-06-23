/*
 * Copyright (C) 2019 AlexMofer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package am.plugin.gradle.compress

/**
 * 文件工具
 */
class Util {

    /**
     * 删除文件
     *
     * @param source 源文件
     * @return 是否成功
     */
    private static boolean deleteFile(File source) {
        if (source == null || !source.exists())
            return true
        if (source.isFile())
            return source.delete()
        if (source.isDirectory()) {
            File[] children = source.listFiles()
            if (children == null || children.length == 0) {
                return source.delete()
            }
            for (File child : children) {
                if (!deleteFile(child))
                    return false
            }
            return source.delete()
        }
        return source.delete()
    }

    /**
     * 清空文件夹
     *
     * @param dir 文件夹
     * @return 是否成功
     */
    static boolean clearDirectory(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return false
        final File[] children = dir.listFiles()
        if (children == null || children.length == 0)
            return true
        for (File child : children) {
            if (!deleteFile(child))
                return false
        }
        return true
    }
}
