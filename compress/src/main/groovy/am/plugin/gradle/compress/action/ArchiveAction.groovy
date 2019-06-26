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
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.ArchiveOutputStream
import org.apache.commons.compress.archivers.ArchiveStreamFactory

/**
 * 归档型
 */
class ArchiveAction extends CommonAction {

    static final String NAME_AR = ArchiveStreamFactory.AR
    static final String NAME_ZIP = ArchiveStreamFactory.ZIP
    static final String NAME_TAR = ArchiveStreamFactory.TAR
    static final String NAME_JAR = ArchiveStreamFactory.JAR
    static final String NAME_CPIO = ArchiveStreamFactory.CPIO

    private final String mName

    ArchiveAction(File source, File output, boolean clear, boolean override) {
        super(source, output, null, clear, override)
        mName = null
    }

    ArchiveAction(String name, File output, File[] sources) {
        super(output, sources)
        mName = name
    }

    @Override
    void compress(byte[] buffer) throws Exception {
        checkCompress()
        if (mName == null || mName.length() <= 0)
            throw new IllegalArgumentException("Archive name cannot be empty.")
        final File dir = getOutput().getParentFile()
        if (dir != null && !checkDirectory(dir))
            throw new IOException("Cannot create directory:" + dir.getPath())
        OutputStream output
        try {
            output = new FileOutputStream(getOutput())
        } catch (Exception e) {
            throw new IOException("Cannot compress:" + e.getMessage())
        }
        ArchiveOutputStream archive
        try {
            archive =
                    new ArchiveStreamFactory().createArchiveOutputStream(mName, output)
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
            final File[] sources = getSources()
            for (File source : sources) {
                final String parent = source.getParent()
                final String root = parent == null ? null : parent + File.pathSeparator
                writeFile(source, archive, root, buffer)
            }
            archive.finish()
        } catch (Exception e) {
            throw new IOException("Cannot compress:" + e.getMessage())
        } finally {
            try {
                archive.close()
            } catch (Exception e) {
                error = true
                message = e.getMessage()
            }
        }
        if (error)
            throw new IOException("Cannot compress:" + message)
    }

    private void writeFile(File input, ArchiveOutputStream output, String root, byte[] buffer)
            throws Exception {
        if (input.isDirectory()) {
            final File[] children = input.listFiles()
            if (children != null && children.length > 0)
                for (File child : children) {
                    writeFile(child, output, root, buffer)
                }
            return
        }
        final String entryName = root == null ?
                input.getPath() : input.getPath().substring(root.length())
        final ArchiveEntry entry = output.createArchiveEntry(input, entryName)
        output.putArchiveEntry(entry)
        final InputStream stream = new FileInputStream(input)
        Util.copy(stream, output, buffer)
        stream.close()
        output.closeArchiveEntry()
    }

    @Override
    void extract(byte[] buffer) throws Exception {
        checkExtract()
        InputStream input
        try {
            input = new BufferedInputStream(new FileInputStream(getSource()))
        } catch (Exception e) {
            throw new IOException("Cannot extract file:" + e.getMessage())
        }
        ArchiveInputStream archive
        try {
            archive = new ArchiveStreamFactory().createArchiveInputStream(input)
        } catch (Exception e) {
            //noinspection GroovyUnusedCatchParameter
            try {
                input.close()
            } finally {
                throw new IOException("Cannot compress:" + e.getMessage())
            }
        }
        boolean error = false
        String message = null
        try {
            final File output = getOutput()
            final boolean override = isOverride()
            ArchiveEntry entry
            while ((entry = archive.getNextEntry()) != null) {
                if (!archive.canReadEntryData(entry))
                    continue
                final File file = new File(output.getPath() + File.separator + entry.getName())
                if (file.exists() && !override)
                    continue
                if (entry.isDirectory()) {
                    if (!checkDirectory(file))
                        throw new IOException("Failed to create directory:" + file.getPath())
                    continue
                }
                final File parent = file.getParentFile()
                if (parent != null && !checkDirectory(parent))
                    throw new IOException("Failed to create directory:" + parent.getPath())
                final OutputStream stream = new FileOutputStream(file)
                Util.copy(archive, stream, buffer)
                stream.close()
            }
        } catch (Exception e) {
            throw new IOException("Cannot extract file:" + e.getMessage())
        } finally {
            try {
                archive.close()
            } catch (Exception e) {
                error = true
                message = e.getMessage()
            }
        }
        if (error)
            throw new IOException("Cannot extract file:" + message)
    }
}
