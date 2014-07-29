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

package com.hp.hpl.jena.tdb.sys;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

public class ProcessUtils {

    private static int myPid = -1;

    /**
     * Tries to get the PID of the current process caching it for future calls
     * since it won't change throughout the life of the process
     * 
     * @param fallback
     *            Fallback PID to return if unable to determine the PID i.e. an
     *            error code to return
     * 
     * @return PID of current process or provided {@code fallback} if unable to
     *         determine PID
     */
    public static int getPid(int fallback) {
        if (myPid != -1)
            return myPid;

        String runtimeBeanName = ManagementFactory.getRuntimeMXBean().getName();
        if (runtimeBeanName == null) {
            return useFallbackPid(fallback);
        }

        // Bean name will have format PID@hostname so we try to parse the PID
        // portion
        int index = runtimeBeanName.indexOf("@");
        if (index < 0)
            return useFallbackPid(fallback);
        try {
            // Parse and cache for future reuse
            String pidData = runtimeBeanName.substring(0, index);
            myPid = Integer.parseInt(pidData);
            return myPid;
        } catch (NumberFormatException e) {
            // Invalid PID
            return useFallbackPid(fallback);
        }
    }

    private static int useFallbackPid(int fallback) {
        // In the case where we can't determine our PID then treat ourselves as
        // no owner and cache for future use
        myPid = fallback;
        return myPid;
    }

    /**
     * Determines whether a given PID is alive
     * 
     * @param pid
     *            PID
     * @return True if the given PID is alive or unknown, false if it is dead
     */
    public static boolean isAlive(int pid) {
        String pidStr = Integer.toString(pid);
        try {
            List<String> data = getProcessInfo(pidStr);

            // Expect a line to contain the PID to indicate the process is
            // alive
            for (String lineData : data) {
                if (lineData.contains(pidStr))
                    return true;
            }

            // Did not find any lines mentioning the PID so we can safely
            // assume that process is dead
            return false;
        } catch (IOException e) {
            // If any error running the process to check for the live process
            // then our check failed and for safety we assume the process is
            // alive

            SystemTDB.errlog
                    .warn("Your platform does not support checking process liveness so TDB disk locations cannot be reliably locked to prevent possible corruption due to unsafe multi-JVM usage");
            return true;
        }
    }

    /**
     * Gets whether the platform we are running on will cause us to treat
     * negative (i.e. invalid) PIDs as alive because of the format in which the
     * command line process monitor application for the system returns errors
     * for invalid PIDs
     * 
     * @return True if a negative PID is treated as alive on this platform or we
     *         cannot determine liveness for PIDs on this platform, false
     *         otherwise
     */
    public static boolean negativePidsTreatedAsAlive() {
        return isAlive(-1);
    }

    /**
     * Gets process information based on the given PID string
     * 
     * @param pidStr
     *            PID string
     * @return Output of running the OSes appropriate command line task info
     *         application
     * @throws IOException
     *             Thrown if there is a problem running the command line
     *             application or reading the data returned
     */
    private static List<String> getProcessInfo(String pidStr) throws IOException {
        Process p;
        if (SystemTDB.isWindows) {
            // Use the Windows tasklist utility
            ProcessBuilder builder = new ProcessBuilder("tasklist", "/FI", "PID eq " + pidStr);
            builder.redirectErrorStream(true);
            p = builder.start();
        } else {
            // Use the ps utility
            ProcessBuilder builder = new ProcessBuilder("ps", "-p", pidStr);
            builder.redirectErrorStream(true);
            p = builder.start();
        }

        // Run and read data from the process
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            List<String> data = new ArrayList<>();
            String line = null;
            while ((line = reader.readLine()) != null) {
                data.add(line);
            }
            return data;
        }
    }
}
