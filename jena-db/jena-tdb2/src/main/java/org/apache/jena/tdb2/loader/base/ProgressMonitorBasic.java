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

package org.apache.jena.tdb2.loader.base;

import org.apache.jena.atlas.lib.Timer;
import org.apache.jena.tdb2.TDBException;

/** Simple {@link ProgressMonitor} that records time and ticks but does not print anything */
public class ProgressMonitorBasic implements ProgressMonitor {
    // Overall
    private Timer timer = new Timer();
    private long timeInMillis = -1;
    private long tickCounter = 0;
    //Section
    private boolean inSection = false;
    private int sectionCounter = 0;
    private Timer sectionTimer = null;
    private long sectionTimeInMillis = -1;
    private long sectionTickCounter = 0;

    public ProgressMonitorBasic() {}

    @Override
    public void startMessage(String message) {}

    @Override
    public void finishMessage(String message) {}

    @Override
    public void start() {
        timer.startTimer();
    }

    @Override
    public void finish() {
        timeInMillis = timer.endTimer();
    }

    @Override
    public void startSection() {
        if ( inSection )
            throw new TDBException("startSection: Already in section");
        inSection = true;
        sectionCounter++;
        sectionTimer = new Timer();
        sectionTimeInMillis = 0;
        sectionTickCounter = 0;
    }

    @Override
    public void finishSection() {
        if ( ! inSection )
            throw new TDBException("finishSection: Not in section");
        inSection = false;
        sectionTimeInMillis = sectionTimer.endTimer();
    }

    @Override
    public void tick() {
        tickCounter++;
        if ( inSection )
            sectionTickCounter++;
    }

    @Override
    public long getTicks() {
        return tickCounter;
    }

    @Override
    public long getTime() {
        return timeInMillis;
    }

    @Override
    public long getSectionTicks() {
        return sectionTickCounter;
    }
    @Override
    public long getSectionTime() {
        return sectionTimeInMillis;
    }

    @Override
    public void setLabel(String label) {}
}
