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

package org.apache.jena.tdb2.xloader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.apache.jena.tdb2.TDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BulkLoaderX {
    public static int DataTick = 1_000_000;
    public static int DataSuperTick = 10;

    /**
     * Marker at end of each step with the log message about the statistics.
     * Grep for this in the logs.
     */
    public static final String StepMarker = "==-==-==";

    /**
     * Marker at the start of a stage within a step.
     */
    public static final String StageMarker = "==";

    /**
     * Whether to compress the triple.tmp and quads.tmp files.
     * These are read multiple times.
     */
    public static boolean CompressDataFiles = true;

    /**
     * Whether to compress intermediate sort files for the node table.
     * We'll need this amount of space for the final indexes so this isn't helpful.
     */
    public static boolean CompressSortNodeTableFiles = false;

    /**
     * Whether to compress intermediate sort files for the indexes.
     */
    public static boolean CompressSortIndexFiles = true;

    // Ubuntu: it now (21.04) is at /usr/bin/gzip.
    //   /bin has become a symbolic link to /usr/bin.
    //   New installs of 20.04 have it at /usr/bin, upgrades have it at /bin.
    /*package*/ static String gzipProgram() {
        if ( programInstalledAt("/usr/bin/gzip") )
            return "/usr/bin/gzip";
        if ( programInstalledAt("/bin/gzip") )
            return "/bin/gzip";
        throw new TDBException("Can't find gzip program");
    }

    private static boolean programInstalledAt(String pathname) {
        Path path = Path.of(pathname);
        if ( ! Files.exists(path) )
            return false;
        if ( ! Files.isExecutable(path) )
            throw new TDBException(pathname+" is not executable by this process");
        return true;
    }

    public static Thread async(Runnable action, String threadName) {
        Objects.requireNonNull(action);
        Objects.requireNonNull(threadName);
        Thread thread = new Thread(action, threadName);
        thread.start();
        return thread;
    }

    public static void waitFor(Thread thread) {
        try { thread.join(); }
        catch (InterruptedException e) { e.printStackTrace(); }
    }

    public static String rateStr(long items, long elapsedMillis) {
        double xSec = elapsedMillis/1000.0;
        double rate = items/xSec;
        return String.format("%,.0f", rate);
    }

    public static String milliToHMS(long milliSeconds) {
        long seconds = milliSeconds/1000;
        // Seconds to allocate
        long z = seconds;

        long h = z / 3600;
        z = z - (3600 * h);

        long m = z / 60;
        z = z - 60 * m;
        long s = z;
        //long check = 3600 * h + 60 * m + s;
        return String.format("%dh %02dm %02ds", h, m, s);
    }

    // Loggers

    public static Logger LOG_Data  = LoggerFactory.getLogger("Data");
    public static Logger LOG_Nodes = LoggerFactory.getLogger("Nodes");
    public static Logger LOG_Terms = LoggerFactory.getLogger("Terms");
    public static Logger LOG_Index = LoggerFactory.getLogger("Index");
}
