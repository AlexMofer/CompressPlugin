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

    SevenZAction(File output, File... sources) {
        super(output, null, sources)
    }

    @Override
    void compress(byte[] buffer) throws Exception {
        checkCompress()
        final SevenZOutputFile output
        try {
            output = new SevenZOutputFile(getOutput())
        } catch (Exception e) {
            throw new IOException("Cannot compress:" + e.getMessage())
        }
        try {
            final File[] sources = getSources()
            for (File source : sources) {
                final String parent = source.getParent()
                final String root = parent == null ? null : parent + File.pathSeparator
                writeFile(source, output, root, buffer)
            }
        } catch (Exception e) {
            //noinspection GroovyUnusedCatchParameter
            try {
                output.close()
            } catch (Exception e1) {
                // ignore
            }
            throw new IOException("Cannot compress:" + e.getMessage())
        }
        try {
            output.close()
        } catch (Exception e) {
            throw new IOException("Cannot compress:" + e.getMessage())
        }
    }

    private void writeFile(File input, SevenZOutputFile output, String root, byte[] buffer)
            throws Exception {
        final String entryName = root == null ?
                input.getPath() : input.getPath().substring(root.length())
        final SevenZArchiveEntry entry = output.createArchiveEntry(input, entryName)
        output.putArchiveEntry(entry)
        if (input.isDirectory()) {
            final File[] children = input.listFiles()
            if (children == null || children.length <= 0)
                return
            for (File child : children) {
                writeFile(child, output, root, buffer)
            }
        } else {
            final BufferedInputStream stream = new BufferedInputStream(new FileInputStream(input))
            if (buffer == null) {
                int data
                while ((data = stream.read()) != -1) {
                    output.write(data)
                }
            } else {
                int count
                while ((count = stream.read(buffer)) != -1) {
                    output.write(buffer, 0, count)
                }
            }
            stream.close()
        }
        output.closeArchiveEntry()
    }

    @Override
    void extract(byte[] buffer) throws Exception {
        checkExtract()
        SevenZFile archive
        try {
            final String password = getPassword()
            if (password == null)
            //noinspection GroovyUnusedAssignment
                archive = new SevenZFile(getSource())
            else
            //noinspection GroovyUnusedAssignment
                archive = new SevenZFile(getSource(), password.toCharArray())
        } catch (Exception e) {
            throw new IOException("Cannot extract file:" + e.getMessage())
        }
        try {
            final File output = getOutput()
            final boolean override = isOverride()
            SevenZArchiveEntry entry
            if (buffer == null) {
                int data
                while ((entry = archive.getNextEntry()) != null) {
                    final File file = new File(output, entry.getName())
                    if (file.exists() && !override)
                        continue
                    if (entry.isDirectory()) {
                        file.mkdirs()
                        continue
                    }
                    final BufferedOutputStream stream =
                            new BufferedOutputStream(new FileOutputStream(file))
                    while ((data = archive.read()) != -1) {
                        stream.write(data)
                    }
                    stream.close()
                }
            } else {
                int count
                while ((entry = archive.getNextEntry()) != null) {
                    final File file = new File(output, entry.getName())
                    if (file.exists() && !override)
                        continue
                    if (entry.isDirectory()) {
                        file.mkdirs()
                        continue
                    }
                    final BufferedOutputStream stream =
                            new BufferedOutputStream(new FileOutputStream(file))
                    while ((count = archive.read(buffer)) != -1) {
                        stream.write(buffer, 0, count)
                    }
                    stream.close()
                }
            }
        } catch (Exception e) {
            throw new IOException("Cannot extract file:" + e.getMessage())
        } finally {
            try {
                archive.close()
            } catch (Exception e) {
                throw new IOException("Cannot extract file:" + e.getMessage())
            }
        }
    }
}