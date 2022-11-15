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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.concurrent.Semaphore;

import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.atlas.io.IO;

/** Fixed OutputStream; one writer via {@code output()} at a time.  */
public class OutputFixed implements ManagedOutput {
    private boolean valid = false;
    private final OutputStream outputStream;
    private final Semaphore sema = new Semaphore(1);
    private OutputStreamManaged currentOutput = null;

    public OutputFixed(OutputStream output) {
        this.outputStream = output;
    }

    /** Get rotation engine */
    @Override
    public Roller roller() {
        return null;
    }

    @Override
    public Path currentFilename() {
        return null;
    }

    @Override
    public Path latestFilename() {
        return null;
    }

    @Override
    public OutputStream currentOutput() {
        return null;
    }

    @Override
    public OutputStream output() {
        try {
            sema.acquire();
        }
        catch (InterruptedException e) {
            throw new RuntimeIOException(e);
        }
        currentOutput = new OutputStreamManaged(outputStream, (x)->this.finish());
        return currentOutput;
    }

    private void finish() {
        try {
            outputStream.flush();
            currentOutput = null;
        }
        catch (IOException e) {
            IO.exception(e); return;
        }
        if ( sema.availablePermits() == 0 )
            sema.release();
    }

    @Override
    public void rotate() {}
}
