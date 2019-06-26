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
package am.plugin.gradle.compress.action

import am.plugin.gradle.compress.CommonAction
import am.plugin.gradle.compress.Util
import org.apache.commons.compress.compressors.CompressorInputStream
import org.apache.commons.compress.compressors.CompressorOutputStream
import org.apache.commons.compress.compressors.CompressorStreamFactory

/**
 * 压缩型
 */
class CompressorAction extends CommonAction {

    static final String NAME_BZIP2 = CompressorStreamFactory.BZIP2
    static final String NAME_GZIP = CompressorStreamFactory.GZIP
    static final String NAME_PACK200 = CompressorStreamFactory.PACK200
    static final String NAME_XZ = CompressorStreamFactory.XZ
    static final String NAME_LZMA = CompressorStreamFactory.LZMA
    static final String NAME_DEFLATE = CompressorStreamFactory.DEFLATE
    static final String NAME_SNAPPY_FRAMED = CompressorStreamFactory.SNAPPY_FRAMED
    static final String NAME_LZ4_BLOCK = CompressorStreamFactory.LZ4_BLOCK
    static final String NAME_LZ4_FRAMED = CompressorStreamFactory.LZ4_FRAMED
    static final String NAME_ZSTANDARD = CompressorStreamFactory.ZSTANDARD

    private final String mName
    private final String mFileName

    CompressorAction(String name, File source, File output, String fileName,
                     boolean clear, boolean override) {
        super(source, output, null, clear, override)
        mName = name
        mFileName = fileName
    }

    CompressorAction(File source, File output, String fileName, boolean clear, boolean override) {
        this(null, source, output, fileName, clear, override)
    }

    CompressorAction(String name, File output, File source) {
        super(output, source)
        mName = name
        mFileName = null
    }

    @Override
    void compress(byte[] buffer) throws Exception {
        checkCompress()
        if (mName == null || mName.length() <= 0)
            throw new IllegalArgumentException("Archive name cannot be empty.")
        final File[] sources = getSources()
        if (sources == null || sources.length != 1)
            throw new IllegalArgumentException("Only support one file.")
        final File source = sources[0]
        final File dir = getOutput().getParentFile()
        if (dir != null && !checkDirectory(dir))
            throw new IOException("Cannot create directory:" + dir.getPath())
        OutputStream output
        try {
            output = new BufferedOutputStream(new FileOutputStream(getOutput()))
        } catch (Exception e) {
            throw new IOException("Cannot compress:" + e.getMessage())
        }
        CompressorOutputStream compressor
        try {
            compressor =
                    new CompressorStreamFactory().createCompressorOutputStream(mName, output)
        } catch (Exception e) {
            try {
                output.close()
            } finally {
                throw new IOException("Cannot compress:" + e.getMessage())
            }
        }
        boolean error = false
        String message = null
        try {
            final InputStream input = new FileInputStream(source)
            Util.copy(input, compressor, buffer)
            input.close()
            compressor.flush()
        } catch (Exception e) {
            throw new IOException("Cannot compress:" + e.getMessage())
        } finally {
            try {
                compressor.close()
            } catch (Exception e) {
                error = true
                message = e.getMessage()
            }
        }
        if (error)
            throw new IOException("Cannot compress:" + message)
    }

    @Override
    void extract(byte[] buffer) throws Exception {
        checkExtract()
        if (mFileName == null || mFileName.length() <= 0)
            throw new IllegalArgumentException("File name cannot be empty.")
        InputStream input
        try {
            input = new BufferedInputStream(new FileInputStream(getSource()))
        } catch (Exception e) {
            throw new IOException("Cannot extract file:" + e.getMessage())
        }
        CompressorInputStream compressor
        try {
            compressor = mName == null ?
                    new CompressorStreamFactory().createCompressorInputStream(input) :
                    new CompressorStreamFactory().createCompressorInputStream(mName, input)
        } catch (Exception e) {
            try {
                input.close()
            } finally {
                throw new IOException("Cannot compress:" + e.getMessage())
            }
        }
        boolean error = false
        String message = null
        try {
            final File output = new File(getOutput(), mFileName)
            final boolean override = isOverride()
            if (output.exists()) {
                if (!override)
                    throw new IOException("File already exists:" + output.getPath())
                if (!output.delete())
                    throw new IOException("File cannot be deleted:" + output.getPath())
            }
            final File parent = output.getParentFile()
            if (parent != null && !checkDirectory(parent))
                throw new IOException("Failed to create directory:" + parent.getPath())
            final OutputStream stream = new FileOutputStream(output)
            Util.copy(compressor, stream, buffer)
            stream.close()
        } catch (Exception e) {
            throw new IOException("Cannot extract file:" + e.getMessage())
        } finally {
            try {
                compressor.close()
            } catch (Exception e) {
                error = true
                message = e.getMessage()
            }
        }
        if (error)
            throw new IOException("Cannot extract file:" + message)
    }
}
