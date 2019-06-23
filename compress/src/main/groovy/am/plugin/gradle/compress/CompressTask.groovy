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

import am.plugin.gradle.compress.action.SevenZAction
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

/**
 * 压缩
 */
@SuppressWarnings(["GrMethodMayBeStatic", "unused"])
class CompressTask extends DefaultTask {

    private final ArrayList<CompressAction> mParams = new ArrayList<>()

    @Input
    @Optional
    int bufferSize = 1024// 缓存大小

    @Input
    @Optional
    boolean interruptWhenError = false// 出现错误时终止

    /**
     * 压缩7z文件
     *
     * @param output 输出文件
     * @param password 密码
     * @param sources 资源文件
     */
    @Input
    void sevenZ(File output, String password, File... sources) {
        mParams.add(new SevenZAction(output, password, sources))
    }

    /**
     * 压缩7z文件
     *
     * @param output 输出文件
     * @param sources 资源文件
     */
    @Input
    void sevenZ(File output, File... sources) {
        sevenZ(output, null, sources)
    }

    @TaskAction
    def extract() {
        final byte[] buffer = bufferSize > 0 ? new byte[bufferSize] : null
        for (CompressAction action : mParams) {
            try {
                action.compress(buffer)
            } catch (Exception e) {
                logger.warn(e.getMessage())
                if (interruptWhenError) {
                    logger.warn("Compress actions interrupt by error.")
                    return
                }
            }
        }
    }
}
