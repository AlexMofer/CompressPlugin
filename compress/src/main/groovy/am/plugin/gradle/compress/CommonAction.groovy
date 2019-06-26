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
 * 通用任务
 */
@SuppressWarnings("unused")
abstract class CommonAction implements CompressAction, ExtractAction {

    private final File mSource
    private final File mOutput
    private final String mPassword
    private final boolean mClear
    private final boolean mOverride
    private final File[] mSources

    CommonAction(File source, File output, String password, boolean clear, boolean override) {
        mSource = source
        mOutput = output
        mPassword = password
        mClear = clear
        mOverride = override
        mSources = null
    }

    CommonAction(File output, File[] sources, String password) {
        mSource = null
        mOutput = output
        mPassword = password
        mClear = false
        mOverride = false
        mSources = sources
    }

    CommonAction(File output, File[] sources) {
        this(output, sources, null)
    }

    CommonAction(File output, File source, String password) {
        mSource = null
        mOutput = output
        mPassword = password
        mClear = false
        mOverride = false
        mSources = new File[1]
        mSources[0] = source
    }

    CommonAction(File output, File source) {
        this(output, source, null)
    }

    /**
     * 获取压缩源文件
     *
     * @return 源文件
     */
    protected File[] getSources() {
        return mSources
    }

    /**
     * 获取压缩输出文件或解压输出文件夹
     *
     * @return 压缩输出文件或解压输出文件夹
     */
    protected File getOutput() {
        return mOutput
    }

    /**
     * 获取压缩或解压密码
     *
     * @return 压缩或解压密码
     */
    protected String getPassword() {
        return mPassword
    }

    /**
     * 获取解压源文件
     *
     * @return 解压源文件
     */
    protected File getSource() {
        return mSource
    }

    /**
     * 判断是否清空解压输出文件夹
     *
     * @return 是否清空
     */
    protected boolean isClear() {
        return mClear
    }

    /**
     * 判断是否覆盖解压输出文件夹中已存在的文件
     *
     * @return 是否覆盖
     */
    protected boolean isOverride() {
        return mOverride
    }

    /**
     * 检查压缩参数
     *
     * @throws Exception 错误
     */
    protected void checkCompress() throws Exception {
        if (mSources == null || mSources.length <= 0)
            throw new IllegalArgumentException("Source files can not be empty.")
        boolean empty = true
        for (File source : mSources) {
            if (source != null && source.exists()) {
                empty = false
                break
            }
        }
        if (empty)
            throw new IllegalArgumentException("Source files can not be exist.")
        if (mOutput == null)
            throw new IllegalArgumentException("Output file can not be null.")
        if (mOutput.exists() && !mOverride)
            throw new IllegalArgumentException("Output file with existing file duplicate name.")
    }

    /**
     * 检查解压参数
     *
     * @throws Exception 错误
     */
    protected void checkExtract() throws Exception {
        if (mSource == null)
            throw new IllegalArgumentException("Source file can not be null.")
        if (!mSource.exists() || !mSource.isFile())
            throw new IllegalArgumentException("Source file not exists or a file.")
        if (!mSource.canRead())
            throw new IllegalArgumentException("Source file can not read.")
        if (mOutput == null)
            throw new IllegalArgumentException("Output directory can not be null.")
        if (mOutput.exists()) {
            if (mOutput.isDirectory()) {
                if (mClear && !Util.clearDirectory(mOutput))
                    throw new IllegalArgumentException("Output directory can not be clear.")
            } else
                throw new IllegalArgumentException("Output path should be a directory.")
        } else {
            if (!mOutput.mkdirs())
                throw new IllegalArgumentException("Output directory can not create.")
        }
    }

    /**
     * 检查文件夹
     *
     * @param dir 文件夹
     * @return 是否检查通过* @throws Exception 错误
     */
    protected static boolean checkDirectory(File dir) throws Exception {
        if (dir.exists()) {
            if (!dir.isDirectory() && dir.delete() && !dir.mkdirs())
                return false
        } else {
            if (!dir.mkdirs())
                return false
        }
        return true
    }
}
