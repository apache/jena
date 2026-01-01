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

package org.apache.jena.fuseki.main.sys;

import java.util.function.Function;

import org.apache.jena.atlas.logging.FmtLog;
import org.slf4j.Logger;

/** Platform information - OS and JVM */
public class PlatformInfo {

    /** Output offset in logging messages. */
    public static final String prefix = "  ";
    public static final int nameFieldSize = 8;
    private static Function<Long, String> strMem = PlatformInfo::strMemNum2;

    /** Information logging */
    public static void logInfo(Logger log, String name, String valuesFmt, Object...values) {
        logInfo(log, nameFieldSize, name, valuesFmt, values);
    }

    /** Information logging */
    public static void logInfo(Logger log, int fieldNameWidth, String name, String valuesFmt, Object...values) {
        String fieldFmtStr = "%-"+fieldNameWidth+"s";
        String fieldStr = String.format(fieldFmtStr, name);
        String valuesStr = String.format(valuesFmt, values);
        FmtLog.info(log, "%s%s%s", prefix, fieldStr, valuesStr);
    }

    /** System details */
    public static void logSystemDetailsLong(Logger log) {
        long maxMem = Runtime.getRuntime().maxMemory();
        long totalMem = Runtime.getRuntime().totalMemory();
        long freeMem = Runtime.getRuntime().freeMemory();
        long usedMem = totalMem - freeMem;

        logInfo(log, "Memory:", "%s", strMem.apply(maxMem));
        //logInfo(log, "Memory:", "max=%s  total=%s  used=%s  free=%s", f.apply(maxMem), f.apply(totalMem), f.apply(usedMem), f.apply(freeMem));
        logInfo(log, "Java:", "%s", System.getProperty("java.version"));
        logInfo(log, "OS:", "%s %s %s", System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"));
        logProcessOS(log);
        //logDetailsUserJVM(log);
    }

    /** System details */
    public static void logSystemDetailsShort(Logger log) {
        long maxMem = Runtime.getRuntime().maxMemory();
//        long totalMem = Runtime.getRuntime().totalMemory();
//        long freeMem = Runtime.getRuntime().freeMemory();
//        long usedMem = totalMem - freeMem;
        logInfo(log, "Memory:", strMem.apply(maxMem));
        logProcessOS(log);
    }

    private static void logProcessOS(Logger log) {
        long pid = getProcessId();
        if ( pid != -1)
            logInfo(log, "PID:", "%d", pid);
    }

    /** JVM details, "java.*" section. */
    public static void logDetailsJavaJVM(Logger log) {
        String prefix = "    ";
        logOneJVM(log, prefix, "java.vendor");
        logOneJVM(log, prefix, "java.home");
        logOneJVM(log, prefix, "java.runtime.version");
        logOneJVM(log, prefix, "java.runtime.name");
    }


    /** JVM details, "user.*" section. */
    public static void logDetailsUserJVM(Logger log) {
        logOneJVM(log, prefix, "user.language");
        logOneJVM(log, prefix, "user.timezone");
        logOneJVM(log, prefix, "user.country");
        logOneJVM(log, prefix, "user.dir");
    }

    private static void logOneJVM(Logger log, String prefix, String property) {
        if ( prefix == null )
            prefix = "";
        logInfo(log, 22, property, " = %s", System.getProperty(property));
    }

    private static long getProcessId() {
        return ProcessHandle.current().pid();
    }

    /** Create a human-friendly string for a number based on Kilo/Mega/Giga/Tera (powers of 10) */
    private static String strMemNum10(long x) {
        if ( x < 1_000 )
            return Long.toString(x);
        if ( x < 1_000_000 )
            return String.format("%.1fKB", x/1000.0);
        if ( x < 1_000_000_000 )
            return String.format("%.1fMB", x/(1000.0*1000));
        if ( x < 1_000_000_000_000L )
            return String.format("%.1fGB", x/(1000.0*1000*1000));
        return String.format("%.1fTB", x/(1000.0*1000*1000*1000));
    }

    /** Create a human-friendly string for a number based on Kibi/Mebi/Gibi/Tebi (powers of 2) */
    private static String strMemNum2(long x) {
        // https://en.wikipedia.org/wiki/Byte#Multiple-byte_units
        if ( x < 1024 )
            return Long.toString(x);
        if ( x < 1024*1024 )
            return String.format("%.1f KiB", x/1024.0);
        if ( x < 1024*1024*1024 )
            return String.format("%.1f MiB", x/(1024.0*1024));
        if ( x < 1024L*1024*1024*1024 )
            return String.format("%.1f GiB", x/(1024.0*1024*1024));
        return String.format("%.1fTiB", x/(1024.0*1024*1024*1024));
    }}
