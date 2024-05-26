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

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.logging.FmtLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filename policy where files are "filebase-yyyy-mm-dd_hh-mm-ss"
 * and do not rollover automatically, only when prompted via {@link #rotate}.
 */
class RollerTimestamp implements Roller {
    private final static Logger LOG = LoggerFactory.getLogger(RollerTimestamp.class);
    private final Path directory;
    private final String baseFilename;
    private Path currentFilename = null;
    private boolean valid = false;
    private LocalDateTime lastTimestamp = null;
    private Path lastAllocatedPath = null;

    /** Match a datetime-appended filename, with non-capturing optional fractional seconds. */
    private static final Pattern patternFilenameDateTime = Pattern.compile("(.*)(-)(\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}(?:\\.\\d+)?)");
    private static final String DATETIME_SEP = "-";
    private static final DateTimeFormatter fmtDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final Comparator<Filename> cmpDateTime = (x,y)->{
        LocalDateTime xdt = filenameToDateTime(x);
        LocalDateTime ydt = filenameToDateTime(y);
        return xdt.compareTo(ydt);
    };
    private static int RETRIES = 5;

    private static LocalDateTime filenameToDateTime(Filename filename) {
        return LocalDateTime.parse(filename.modifier, fmtDateTime);
    }

    RollerTimestamp(Path directory, String baseFilename) {
        this.directory = directory;
        this.baseFilename = baseFilename;
        init(directory,baseFilename);
    }

    private void init(Path directory, String baseFilename) {
        LocalDateTime current = LocalDateTime.now();
        List<Filename> filenames = FileMgr.scan(directory, baseFilename, patternFilenameDateTime);
        if ( filenames.isEmpty() ) {
            currentFilename = null;
        } else {
            LocalDateTime dtLast = filenameToDateTime(Collections.max(filenames, cmpDateTime));
            LocalDateTime dtFirst = filenameToDateTime(Collections.min(filenames, cmpDateTime));
            int problems = 0;
            if ( dtLast.isAfter(current)) {
                problems++;
                FmtLog.warn(LOG, "Latest output file is timestamped after now: %s > %s", dtLast, current);
            }
            if ( dtFirst.isAfter(current)) {
                problems++;
                FmtLog.warn(LOG, "First output file is timestamped after now: %s > %s", dtFirst, current);
            }
            if ( problems > 0 )
                throw new FileRotateException("Existing files timestamped into the future");
            currentFilename = filename(dtLast);
        }
    }

    @Override
    public Stream<Filename> files() {
        List<Filename> filenames = FileMgr.scan(directory, baseFilename, patternFilenameDateTime);
        return filenames.stream().sorted(cmpDateTime);
    }

    @Override
    public Path directory() {
        return directory;
    }

    @Override
    public void startSection() {}

    @Override
    public void finishSection() {}

    @Override
    public Path latestFilename() {
        return lastAllocatedPath;
    }

    @Override
    public boolean hasExpired() {
        // Manual rollover only.
        return ! valid;
    }

    @Override
    public void rotate() {
        valid = false;
    }

    @Override
    public Path nextFilename() {
        for ( int i = 1 ; ; i++ ) {
            // This "must" be unique unless it is the same time as last time
            // because time moves forward and we checked for future files in init().
            LocalDateTime timestamp = LocalDateTime.now();
            String fn = baseFilename + DATETIME_SEP + timestamp.format(fmtDateTime);
            Path path = directory.resolve(fn);
            if ( ! Files.exists(path) ) {
                valid = true;
                lastTimestamp = timestamp;
                lastAllocatedPath = path;
                return lastAllocatedPath;
            }
            // Try again.
            if ( i == RETRIES)
                throw new FileRotateException("Failed to find a new, fresh filename: "+timestamp);
            Lib.sleep(1000);
        }
    }

    private Path filename(LocalDateTime dateTime) {
        String fn = baseFilename + DATETIME_SEP + dateTime.format(fmtDateTime);
        return directory.resolve(fn);
    }
}