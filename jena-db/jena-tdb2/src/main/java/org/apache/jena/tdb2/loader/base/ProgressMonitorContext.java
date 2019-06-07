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
import org.apache.jena.atlas.logging.Log;

/**
 * The counting state of a {@link ProgressMonitor}. This can be used to across different
 * {@code ProgressMonitor}s (sequentially) to give running totals
 */

public class ProgressMonitorContext {
    /*package*/ long ticks;
    /*package*/ Timer timer;
    private int depth = 0;

    public ProgressMonitorContext(long ticks, Timer timer) {
        super();
        this.ticks = ticks;
        this.timer = timer;
    }

    public void tick() { ticks++; }

    public void start() {
        if ( depth == 0 )
            timer.startTimer();
        depth++;
    }
    public void finish() {
        --depth;
        if ( depth < 0 ) {
            Log.error(this, "Misaligned start/finish");
            return;
        }
        if ( depth == 0 )
            timer.endTimer();
    }

    //public int getDepth() { return depth; }

    public long getElapsed() { return depth < 0 ? -1 : timer.readTimer(); }

}
