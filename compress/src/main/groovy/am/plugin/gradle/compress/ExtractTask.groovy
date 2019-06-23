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
 * 解压
 */
@SuppressWarnings(["GrMethodMayBeStatic", "unused"])
class ExtractTask extends DefaultTask {

    private final ArrayList<ExtractAction> mParams = new ArrayList<>()

    @Input
    @Optional
    int bufferSize = 1024// 缓存大小

    @Input
    @Optional
    boolean interruptWhenError = false// 出现错误时终止

    /**
     * 解压7z文件
     *
     * @param source 资源文件
     * @param output 输出文件夹
     * @param password 密码
     * @param clearOutput 清空输出文件夹
     * @param override 覆盖已存在的文件
     */
    @Input
    void unSevenZ(File source, File output, String password, boolean clearOutput,
                  boolean override) {
        mParams.add(new SevenZAction(source, output, password, clearOutput, override))
    }

    /**
     * 解压7z文件
     *
     * @param source 资源文件
     * @param output 输出文件夹
     * @param password 密码
     */
    @Input
    void unSevenZ(File source, File output, String password) {
        unSevenZ(source, output, password, false, false)
    }

    /**
     * 解压7z文件
     *
     * @param source 资源文件
     * @param output 输出文件夹
     */
    @Input
    void unSevenZ(File source, File output) {
        unSevenZ(source, output, null)
    }

    @TaskAction
    def extract() {
        final byte[] buffer = bufferSize > 0 ? new byte[bufferSize] : null
        for (ExtractAction action : mParams) {
            try {
                action.extract(buffer)
            } catch (Exception e) {
                logger.warn(e.getMessage())
                if (interruptWhenError) {
                    logger.warn("Extract actions interrupt by error.")
                    return
                }
            }
        }
    }
}
