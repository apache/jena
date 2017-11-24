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

package org.apache.jena.fuseki.mgt;

import static java.lang.String.format ;

import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.atlas.json.JsonValue ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.fuseki.async.AsyncPool ;
import org.apache.jena.fuseki.async.AsyncTask ;
import org.apache.jena.fuseki.servlets.HttpAction ;
import org.apache.jena.fuseki.servlets.ServletOps ;
import org.slf4j.Logger ;

/** A task that kicks off a asynchronous operation that simply waits and exits.  For testing. */
public class ActionSleep extends ActionCtl /* Not ActionAsyncTask - that is a container-item based.c */
{
    public ActionSleep() { super() ; }
    
    // And only POST
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        doCommon(request, response);
    }

    @Override
    protected void perform(HttpAction action) {
        Runnable task = createRunnable(action) ;
        AsyncTask aTask = Async.execASyncTask(action, AsyncPool.get(), "sleep", task) ;
        JsonValue v = Async.asJson(aTask) ;
        Async.setLocationHeader(action, aTask);
        ServletOps.sendJsonReponse(action, v);
    }

    protected Runnable createRunnable(HttpAction action) {
        String name = action.getDatasetName() ;
        if ( name == null )
            name = "''" ;
        
        String interval = action.request.getParameter("interval") ;
        int sleepMilli = 5000 ;
        if ( interval != null )
            try {
                sleepMilli = Integer.parseInt(interval) ;
            } catch (NumberFormatException ex) {
                action.log.error(format("[%d] NumberFormatException: %s", action.id, interval)) ; 
            }
        action.log.info(format("[%d] Sleep %s %d ms", action.id, name, sleepMilli)) ;
        return new SleepTask(action, sleepMilli) ;
    }

    static class SleepTask implements Runnable {
        private final Logger log ;
        private final long actionId ;
        private final int sleepMilli ;
        
        public SleepTask(HttpAction action, int sleepMilli ) {
            this.log = action.log ;
            this.actionId = action.id ;
            this.sleepMilli = sleepMilli ;
        }

        @Override
        public void run() {
            try {
                log.info(format("[%d] >> Sleep start", actionId)) ;
                Lib.sleep(sleepMilli) ;
                log.info(format("[%d] << Sleep finish", actionId)) ;
            } catch (Exception ex) {
                log.info(format("[%d] **** Exception", actionId), ex) ;
            }
        }
    }
}

