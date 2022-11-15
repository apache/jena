/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.rdfpatch.filelog.rotate;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Roller where the files are "0001", "0002", "0003", The files are not moved ; the next
 * index in sequence is the next filename. See {@link RollerShifter} for shifting all the
 * file names up and having a fixed current filename.
 */
class RollerIndex implements Roller {
    // Explicit rollover.

    private final Path directory;
    private final String baseFilename;
    private final String indexFormat;
    private Path lastFilename;

    public static Comparator<Filename> cmpNumericModifier = FileMgr.cmpNumericModifier;

    private final Pattern patternFilenameIndex = Pattern.compile("(.*)("+Pattern.quote(FileMgr.INC_SEP)+")(\\d+)");
    private final String  fmtModifer = "%04d";
    private static final String INC_SEP = FileMgr.INC_SEP;

    private Long currentId = null;
    // Are we in a section?
    // If not, the file needs to rotate on next access.
    private boolean inSection = false;

    RollerIndex(Path directory, String baseFilename, String indexFormat) {
        this.directory = directory;
        this.baseFilename = baseFilename;
        this.indexFormat = indexFormat;
        init(directory,baseFilename);
    }

    private void init(Path directory, String baseFilename) {
        List<Filename> filenames = FileMgr.scan(directory, baseFilename, patternFilenameIndex);
        if ( ! filenames.isEmpty() ) {
            Filename max = Collections.max(filenames, cmpNumericModifier);
            currentId = Long.parseLong(max.modifier);
            lastFilename = filename(currentId);
        }
        else {
            // Before the start.
            currentId = 0L;
            lastFilename = null;
        }
    }

    @Override
    public Stream<Filename> files() {
        List<Filename> filenames = FileMgr.scan(directory, baseFilename, patternFilenameIndex);
        return filenames.stream().sorted(cmpNumericModifier);
    }

    @Override
    public Path directory() {
        return directory;
    }

    @Override
    public void startSection() {
        inSection = true;
    }

    @Override
    public void finishSection() {
        // Each section is in its own file
        inSection = false;
    }

    @Override
    public Path latestFilename() {
        return lastFilename;
    }

    @Override
    public void rotate() {
        // Always rotates on nextFilename.
    }

    @Override
    public boolean hasExpired() {
     // Always rotates on nextFilename.
        return true;
    }

    private long nextIndex() {
        return currentId+1;
    }

    @Override
    public Path nextFilename() {
        long idx = nextIndex();
        currentId = idx;
        // XXX FileMgr.freshFilename(directory, baseFilename, (int)idx, INC_SEP, fmtModifer);
        lastFilename = filename(currentId);
        return lastFilename;
    }

    private Path filename(Long idx) {
        Objects.requireNonNull(idx);
        String fn = FileMgr.basename(baseFilename, idx, INC_SEP, fmtModifer);
        return directory.resolve(fn);
    }
}