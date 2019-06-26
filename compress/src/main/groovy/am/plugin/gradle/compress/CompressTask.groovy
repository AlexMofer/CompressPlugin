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

import am.plugin.gradle.compress.action.ArchiveAction
import am.plugin.gradle.compress.action.CompressorAction
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
     * 压缩为压缩类型的压缩包（.bz2）
     *
     * @param output 输出文件
     * @param source 资源文件
     */
    @Input
    void bzip2(File output, File source) {
        mParams.add(new CompressorAction(CompressorAction.NAME_BZIP2, output, source))
    }

    /**
     * 压缩为压缩类型的压缩包（.gz）
     *
     * @param output 输出文件
     * @param source 资源文件
     */
    @Input
    void gzip(File output, File source) {
        mParams.add(new CompressorAction(CompressorAction.NAME_GZIP, output, source))
    }

    /**
     * 压缩为压缩类型的压缩包（.pack）
     *
     * @param output 输出文件
     * @param source 资源文件
     */
    @Input
    void pack200(File output, File source) {
        mParams.add(new CompressorAction(CompressorAction.NAME_PACK200, output, source))
    }

    /**
     * 压缩为压缩类型的压缩包（.xz）
     *
     * @param output 输出文件
     * @param source 资源文件
     */
    @Input
    void xz(File output, File source) {
        mParams.add(new CompressorAction(CompressorAction.NAME_XZ, output, source))
    }

    /**
     * 压缩为压缩类型的压缩包（.lzma）
     *
     * @param output 输出文件
     * @param source 资源文件
     */
    @Input
    void lzma(File output, File source) {
        mParams.add(new CompressorAction(CompressorAction.NAME_LZMA, output, source))
    }

    /**
     * 压缩为压缩类型的压缩包（.dfl）
     *
     * @param output 输出文件
     * @param source 资源文件
     */
    @Input
    void deflate(File output, File source) {
        mParams.add(new CompressorAction(CompressorAction.NAME_DEFLATE, output, source))
    }

    /**
     * 压缩为压缩类型的压缩包（.snappy）
     *
     * @param output 输出文件
     * @param source 资源文件
     */
    @Input
    void snappy(File output, File source) {
        mParams.add(new CompressorAction(CompressorAction.NAME_SNAPPY_FRAMED, output, source))
    }

    /**
     * 压缩为压缩类型的压缩包（.lz4）
     *
     * @param output 输出文件
     * @param source 资源文件
     */
    @Input
    void lz4Block(File output, File source) {
        mParams.add(new CompressorAction(CompressorAction.NAME_LZ4_BLOCK, output, source))
    }

    /**
     * 压缩为压缩类型的压缩包（.lz4）
     *
     * @param output 输出文件
     * @param source 资源文件
     */
    @Input
    void lz4Framed(File output, File source) {
        mParams.add(new CompressorAction(CompressorAction.NAME_LZ4_FRAMED, output, source))
    }

    /**
     * 压缩为压缩类型的压缩包（.zstd）
     *
     * @param output 输出文件
     * @param source 资源文件
     */
    @Input
    void zStandard(File output, File source) {
        mParams.add(new CompressorAction(CompressorAction.NAME_ZSTANDARD, output, source))
    }

    /**
     * 压缩为归档类型的压缩包（.ar）
     *
     * @param output 输出文件
     * @param sources 资源文件
     */
    @Input
    void ar(File output, File... sources) {
        mParams.add(new ArchiveAction(ArchiveAction.NAME_AR, output, sources))
    }

    /**
     * 压缩为归档类型的压缩包（.zip）
     *
     * @param output 输出文件
     * @param sources 资源文件
     */
    @Input
    void zip(File output, File... sources) {
        mParams.add(new ArchiveAction(ArchiveAction.NAME_ZIP, output, sources))
    }

    /**
     * 压缩为归档类型的压缩包（.tar）
     *
     * @param output 输出文件
     * @param sources 资源文件
     */
    @Input
    void tar(File output, File... sources) {
        mParams.add(new ArchiveAction(ArchiveAction.NAME_TAR, output, sources))
    }

    /**
     * 压缩为归档类型的压缩包（.jar）
     *
     * @param output 输出文件
     * @param sources 资源文件
     */
    @Input
    void jar(File output, File... sources) {
        mParams.add(new ArchiveAction(ArchiveAction.NAME_JAR, output, sources))
    }

    /**
     * 压缩为归档类型的压缩包（.cpio）
     *
     * @param output 输出文件
     * @param sources 资源文件
     */
    @Input
    void cpio(File output, File... sources) {
        mParams.add(new ArchiveAction(ArchiveAction.NAME_CPIO, output, sources))
    }

    /**
     * 压缩7z文件
     *
     * @param output 输出文件
     * @param sources 资源文件
     */
    @Input
    void sevenZ(File output, File... sources) {
        mParams.add(new SevenZAction(output, sources))
    }

    @TaskAction
    def compress() {
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