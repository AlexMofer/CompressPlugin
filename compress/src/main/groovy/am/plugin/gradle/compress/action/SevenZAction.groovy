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

/**
 * 7z
 */
class SevenZAction extends CommonAction {

    SevenZAction(File source, File output, String password, boolean clear, boolean override) {
        super(source, output, password, clear, override)
    }

    SevenZAction(File output, String password, File... sources) {
        super(output, password, sources)
    }

    @Override
    void compress(byte[] buffer) throws Exception {
        checkCompress()
        // not support yet
    }

    @Override
    void extract(byte[] buffer) throws Exception {
        checkExtract()
        SevenZFile archive
        try {
            final String password = getPassword()
            if (password == null)
                archive = new SevenZFile(getSource())
            else
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
            archive.close()
        }
    }
}
