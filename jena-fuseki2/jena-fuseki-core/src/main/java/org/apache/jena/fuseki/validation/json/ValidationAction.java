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

package org.apache.jena.fuseki.validation.json;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.jena.atlas.logging.Log;
import org.slf4j.Logger;

public class ValidationAction
{
    public final boolean verbose;
    public final long id;
    public final Logger log;
    private boolean startTimeIsSet = false;
    private boolean finishTimeIsSet = false;

    private long startTime = -2;
    private long finishTime = -2;

    // Outcome.
    public int statusCode = -1;
    public String message = null;
    public int contentLength = -1;
    public String contentType = null;

    public Map <String, String> headers = new HashMap<>();
    public HttpServletRequest request;
    public HttpServletResponse response;

    public ValidationAction(long id, Logger log, HttpServletRequest request, HttpServletResponse response, boolean verbose) {
        this.id = id;
        this.log = log;
        this.request = request;
        this.response = response;
        this.verbose = false;
    }

    /** Reduce to a size that can be kept around for sometime */
    public void minimize() {
        this.request = null;
        this.response = null;
    }

    public void setStartTime() {
        if ( startTimeIsSet )
            Log.warn(this,  "Start time reset");
        startTimeIsSet = true;
        this.startTime = System.nanoTime();
    }

    public void setFinishTime() {
        if ( finishTimeIsSet )
            Log.warn(this,  "Finish time reset");
        finishTimeIsSet = true;
        this.finishTime = System.nanoTime();
    }

    public HttpServletRequest getRequest()          { return request; }

    public HttpServletResponse getResponse()        { return response; }

    /** Return the recorded time taken in milliseconds.
     *  {@link #setStartTime} and {@link #setFinishTime}
     *  must have been called.
     */
    public long getTime() {
        if ( ! startTimeIsSet )
            Log.warn(this,  "Start time not set");
        if ( ! finishTimeIsSet )
            Log.warn(this,  "Finish time not set");
        return (finishTime-startTime)/(1000*1000);
    }
}
