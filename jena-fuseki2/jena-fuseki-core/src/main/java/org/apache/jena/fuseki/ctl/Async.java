/**
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

package org.apache.jena.fuseki.ctl;

import org.apache.http.HttpHeaders;
import org.apache.jena.atlas.json.JsonBuilder;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.fuseki.async.AsyncPool;
import org.apache.jena.fuseki.async.AsyncTask;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.servlets.HttpAction;

public class Async
{
    public static AsyncTask asyncTask(AsyncPool asyncPool, String displayName, DataService dataService, Runnable task, long requestId) {
        AsyncTask asyncTask = asyncPool.submit(task, displayName, dataService, requestId);
        return asyncTask;
    }

    public static JsonValue asJson(AsyncTask asyncTask) {
        JsonBuilder builder = new JsonBuilder();
        builder.startObject("outer");
        builder.key(JsonConstCtl.taskId).value(asyncTask.getTaskId());
        if ( asyncTask.getOriginatingRequestId() > 0 )
            builder.key(JsonConstCtl.taskRequestId).value(asyncTask.getOriginatingRequestId());
        builder.finishObject("outer");
        return builder.build();
    }

    private static void setLocationHeader(HttpAction action, AsyncTask asyncTask) {
        String x = action.getRequest().getRequestURI();
        if ( ! x.endsWith("/") )
            x += "/";
        x += asyncTask.getTaskId();
        action.getResponse().setHeader(HttpHeaders.LOCATION, x);
    }

    public static AsyncTask execASyncTask(HttpAction action, AsyncPool asyncPool, String displayName, Runnable runnable) {
        AsyncTask atask = Async.asyncTask(asyncPool, displayName, action.getDataService(), runnable, action.id);
        Async.setLocationHeader(action, atask);
        return atask;
    }
}

