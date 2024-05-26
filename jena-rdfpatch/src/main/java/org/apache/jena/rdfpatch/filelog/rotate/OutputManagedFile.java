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

import java.io.*;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Semaphore;

import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.rdfpatch.filelog.FilePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** File-based {@link ManagedOutput} with various {@link FilePolicy} for file rotation. */
public class OutputManagedFile implements ManagedOutput {
    private static Logger LOG = LoggerFactory.getLogger(OutputManagedFile.class);

    // The file area
    private final Path directory;
    private final String filebase;
    // Current active file, full path name.
    private Path currentFilename = null;

    // One writer at a time.
    private final Semaphore sema = new Semaphore(1);
    // The output
    private FileOutputStream fileOutput = null;
    // Buffered output stream used by the caller.
    private OutputStream output = null;
    private OutputStreamManaged currentOutput = null;

    static DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

    // File Policy
    private final Roller roller;

    // Number of writes this process-lifetime.
    private long counter = 0;

    /* package*/ public OutputManagedFile(Path directory, String baseFilename, FilePolicy strategy) {
        this.directory = directory;
        this.filebase = baseFilename;
        this.roller = roller(directory, baseFilename, strategy);
    }

    private static Roller roller(Path directory, String baseFilename, FilePolicy strategy) {
        switch ( strategy ) {
            case DATE :         return new RollerDate(directory, baseFilename);
            case INDEX :        return new RollerIndex(directory, baseFilename, "%04d");
            case SHIFT :        return new RollerShifter(directory, baseFilename, "%03d");
            case TIMESTAMP :    return new RollerTimestamp(directory, baseFilename);
            case FIXED :        return new RollerFixed(directory, baseFilename);
        }
        return null;
    }

    /** Get rotation engine */
    @Override
    public Roller roller() {
        return roller;
    }


    @Override
    public OutputStream currentOutput() {
        return currentOutput;
    }


    @Override
    public Path currentFilename() {
        return currentOutput != null ? currentFilename : null;
    }

    @Override
    public Path latestFilename() {
        return roller.latestFilename();
    }

    @Override
    public OutputStream output() {
        try {
            sema.acquire();
        }
        catch (InterruptedException e) {
            throw new RuntimeIOException(e);
        }
        roller.startSection();
        advanceIfNecessary();
        currentOutput = new OutputStreamManaged(output, (x)->finish());
        return currentOutput;
    }

    /** Force a rotation of the output file. */
    @Override
    public void rotate() {
        finish();
        roller.rotate();
    }

    private void finish() {
        roller.finishSection();
        try {
            currentOutput = null;
            // Flush the BufferedOutputStream to the FileOutputStream
            if ( output != null ) {
                output.flush();
                // fsync the FileOutputStream to storage
                fileOutput.getFD().sync();
            }
            // output still valid.
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        if ( sema.availablePermits() == 0 )
            sema.release();
    }

    private boolean hasActiveFile() {
        return output != null;
    }

    private void advanceIfNecessary() {
        // Inside ownership of the semaphore.
        // Other rules
        if ( roller.hasExpired() )
            closeOutput();
        if ( ! hasActiveFile() )
            nextFile();
    }

    private void flushOutput() {
        try {
            output.flush();
            fileOutput.getFD().sync();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeOutput() {
        if ( output == null )
            return;
        flushOutput();
        IO.close(output);
        //[gz] Compress currentFilename.
        output = null;
        fileOutput = null;
        // keep: currentFilename
        currentOutput = null;
    }

    private void nextFile() {
        try {
            currentFilename = roller.nextFilename();
            FmtLog.debug(LOG, "Setup: %s", currentFilename);
            // Must be a FileOutputStream so that getFD().sync is available.
            fileOutput = new FileOutputStream(currentFilename.toString(), true);
            output = new BufferedOutputStream(fileOutput);
            //[gz]
        } catch (FileNotFoundException ex) {
            IO.exception(ex);
            return;
//        } catch (IOException ex) {
//            IO.exception(ex);
//            return;
        }
    }
}
