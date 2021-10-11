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

/** Interface {@code ProgressMonitor} - monitor progress.*/
public interface ProgressMonitor {
    /** Output the starting message.
     * The format is implementation dependent.
     */
    public void startMessage(String message);

    /**
     * Output the finishing message.
     * The format is implementation dependent.
     */
    public void finishMessage(String message);

    public String getLabel();

    public void setLabel(String label);

    /** Start and start timing. This should be paired with a call to {@link #finish()}. */
    public void start();

    /** Start a section within the overall start-finish. */
    public void startSection();

    /** Finish a section within the overall start-finish. */
    public void finishSection();

    /**
     * Finish and stop timing. The total time is available with {@link #getTime} and the
     * number of items processes with {@link #getTicks()}.
     */
    public void finish();

    /** Something happened */
    public void tick();

    /** Return the number of ticks. Valid after {@link #start()} has been called. */
    public long getTicks();

    /** Return the elapsed time taken - this is only valid after {@link #finish()} has been called. */
    public long getTime();

    /** Return the number of ticks. Valid after {@link #startSection()} has been called. */
    public long getSectionTicks();

    /** Return the elapsed section time taken. */
    public long getSectionTime();
}
