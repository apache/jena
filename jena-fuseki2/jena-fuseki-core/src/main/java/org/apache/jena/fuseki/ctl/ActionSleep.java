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

import static java.lang.String.format;

import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.fuseki.async.AsyncPool;
import org.apache.jena.fuseki.async.AsyncTask;
import org.apache.jena.fuseki.servlets.ActionLib;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.slf4j.Logger;

/** A task that kicks off a asynchronous operation that simply waits and exits.  For testing. */
public class ActionSleep extends ActionCtl /* Not ActionAsyncTask - that is a container-item based. */
{
    public static final int MaxSleepMillis = 20*1000;

    public ActionSleep() { super(); }

    @Override
    public void execOptions(HttpAction action) {
        ActionLib.doOptionsPost(action);
        ServletOps.success(action);
    }

    @Override
    public void execPost(HttpAction action) {
        super.executeLifecycle(action);
    }

    @Override
    public void validate(HttpAction action) {}

    @Override
    public void execute(HttpAction action) {
        SleepTask task = createRunnable(action);
        AsyncTask aTask = Async.execASyncTask(action, AsyncPool.get(), "sleep", task);
        action.log.info(format("[%d] Sleep %d ms.", action.id, task.sleepMilli));
        JsonValue v = Async.asJson(aTask);
        ServletOps.sendJsonReponse(action, v);
    }

    protected SleepTask createRunnable(HttpAction action) {
        String interval = action.request.getParameter("interval");
        int sleepMilli = 5000;
        if ( interval != null ) {
            try {
                sleepMilli = Integer.parseInt(interval);
            } catch (NumberFormatException ex) {
                ServletOps.errorBadRequest("Bad value for 'interval': integer required");
                return null;
            }
        }
        if ( sleepMilli < 0 ) {
            ServletOps.errorBadRequest("Bad value for 'interval': negative sleep interval");
            return null;
        }
        if ( sleepMilli > MaxSleepMillis ) {
            ServletOps.errorBadRequest("Bad value for 'interval': sleep internal greater than maximum allowed");
            return null;
        }
        return new SleepTask(action, sleepMilli, AsyncPool.get());
    }

    static class SleepTask implements Runnable {
        private final Logger log;
        private final long actionId;
        public  final int sleepMilli;
        private final AsyncPool asyncPool;

        public SleepTask(HttpAction action, int sleepMilli, AsyncPool asyncPool ) {
            this.log = action.log;
            this.actionId = action.id;
            this.sleepMilli = sleepMilli;
            this.asyncPool = asyncPool;
        }

        @Override
        public void run() {
            try {
                log.info(format("[Task %d] >> Sleep start", actionId));
                Lib.sleep(sleepMilli);
                log.info(format("[Task %d] << Sleep finish", actionId));
            } catch (Exception ex) {
                log.info(format("[Task %d] **** Exception", actionId), ex);
                // Must also throw the error upwards so that the async task tracking infrastucture can set the
                // success flag correctly
                throw ex;
            }
        }
    }
}

