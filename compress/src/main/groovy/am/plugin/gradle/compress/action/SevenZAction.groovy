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
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile

/**
 * 7z
 */
class SevenZAction extends CommonAction {

    SevenZAction(File source, File output, String password, boolean clear, boolean override) {
        super(source, output, password, clear, override)
    }

    SevenZAction(File output, File[] sources) {
        super(output, sources)
    }

    @Override
    void compress(byte[] buffer) throws Exception {
        checkCompress()
        final File dir = getOutput().getParentFile()
        if (dir != null && !checkDirectory(dir))
            throw new IOException("Cannot create directory:" + dir.getPath())
        SevenZOutputFile output
        try {
            output = new SevenZOutputFile(getOutput())
        } catch (Exception e) {
            throw new IOException("Cannot compress:" + e.getMessage())
        }
        boolean error = false
        String message = null
        try {
            final File[] sources = getSources()
            for (File source : sources) {
                final String parent = source.getParent()
                final String root = parent == null ? null : parent + File.pathSeparator
                writeFile(source, output, root, buffer)
            }
        } catch (Exception e) {
            throw new IOException("Cannot compress:" + e.getMessage())
        } finally {
            try {
                output.close()
            } catch (Exception e) {
                error = true
                message = e.getMessage()
            }
        }
        if (error)
            throw new IOException("Cannot compress:" + message)
    }

    private void writeFile(File input, SevenZOutputFile output, String root, byte[] buffer)
            throws Exception {
        if (input.isDirectory()) {
            final File[] children = input.listFiles()
            if (children != null && children.length > 0)
                for (File child : children) {
                    writeFile(child, output, root, buffer)
                }
            return
        }
        final String entryName =
                root == null ? input.getPath() : input.getPath().substring(root.length())
        final SevenZArchiveEntry entry = output.createArchiveEntry(input, entryName)
        output.putArchiveEntry(entry)
        final InputStream stream = new FileInputStream(input)
        copy(stream, output, buffer)
        stream.close()
        output.closeArchiveEntry()
    }

    @Override
    void extract(byte[] buffer) throws Exception {
        checkExtract()
        SevenZFile archive
        try {
            final String password = getPassword()
            archive = password == null ? new SevenZFile(getSource()) :
                    new SevenZFile(getSource(), password.toCharArray())
        } catch (Exception e) {
            throw new IOException("Cannot extract file:" + e.getMessage())
        }
        boolean error = false
        String message = null
        try {
            final File output = getOutput()
            final boolean override = isOverride()
            SevenZArchiveEntry entry
            while ((entry = archive.getNextEntry()) != null) {
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
                copy(archive, stream, buffer)
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

    private static void copy(final InputStream input, final SevenZOutputFile output, byte[] buffer)
            throws Exception {
        if (buffer == null) {
            int data
            while ((data = input.read()) != -1) {
                output.write(data)
            }
        } else {
            int count
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count)
            }
        }
    }

    private static void copy(final SevenZFile input, final OutputStream output, byte[] buffer)
            throws Exception {
        if (buffer == null) {
            int data
            while ((data = input.read()) != -1) {
                output.write(data)
            }
        } else {
            int count
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count)
            }
        }
    }
}