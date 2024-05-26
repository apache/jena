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

package org.apache.jena.atlas.lib;

import org.apache.jena.atlas.AtlasException;

/** A Timer of operations */
public class Timer {

    protected long timeFinish = -1;
    protected boolean inTimer = false;
    protected long timeStart = 0;

    public Timer() {}

    public Timer startTimer() {
        if ( inTimer )
            throw new AtlasException("Already in timer");
        timeStart = System.currentTimeMillis();
        timeFinish = -1;
        inTimer = true;
        return this;
    }

    /** Stop timing and return the elapsed time in milliseconds */
    public long endTimer() {
        if ( !inTimer )
            throw new AtlasException("Not in timer");
        timeFinish = System.currentTimeMillis();
        inTimer = false;
        return getTimeInterval();
    }

    /**
     * Read the timer - either the instantaneous value (if running) or elapsed time
     * (if finished).
     */
    public long read() {
        return inTimer ? System.currentTimeMillis() - timeStart : timeFinish - timeStart;
    }

    /** Read a running timer */
    public long readTimer() {
        if ( !inTimer )
            throw new AtlasException("Not in timer");
        return read();
    }

    /** Read an elapsed timer */
    public long getTimeInterval() {
        if ( inTimer )
            throw new AtlasException("Still timing");
        if ( timeFinish == -1 )
            throw new AtlasException("No valid interval");
        return read();
    }

    /** Helper function to format milliseconds as "%.3f" seconds */
    static public String timeStr(long timeInterval) {
        return String.format("%.3f", timeInterval / 1000.0);
    }

    protected String timeStr(long timePoint, long startTimePoint) {
        return timeStr(timePoint - startTimePoint);
    }

    /** Time an operation. Return the elapsed time in milliseconds. */
    public static long time(Runnable action) {
        Timer timer = new Timer();
        timer.startTimer();
        action.run();
        long x = timer.endTimer();
        return x;
    }
}
