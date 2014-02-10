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

import org.apache.jena.atlas.json.JsonBuilder ;
import org.apache.jena.atlas.json.JsonValue ;
import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.fuseki.async.AsyncPool ;
import org.apache.jena.fuseki.async.AsyncTask ;
import org.apache.jena.fuseki.servlets.HttpAction ;
import org.apache.jena.fuseki.servlets.ServletOps ;
import org.apache.jena.web.HttpSC ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class ActionBackup extends ActionItem
{
    private static AsyncPool asyncPool = AsyncPool.get() ;
    
    public ActionBackup() { super() ; }
    
//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
//        doCommon(request, response);
//    }

    // Only POST
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        doCommon(request, response);
    }

    @Override
    protected JsonValue execGetItem(HttpAction action) { 
        throw new InternalErrorException("GET for backup -- Should not be here!") ;
//        ServletOps.error(HttpSC.METHOD_NOT_ALLOWED_405);
//        return null ;
    }

    @Override
    protected JsonValue execPostItem(HttpAction action) {
        String name = action.getDatasetName() ;
        if ( name == null ) {
            action.log.error("Null for dataset name in item request") ;  
            ServletOps.errorOccurred("Null for dataset name in item request");
            return null ;
        }
        action.log.info(format("[%d] Backup dataset %s", action.id, name)) ;
        
        Task task = new Task(action) ;
        AsyncTask asyncTask = asyncPool.submit(task, "backup", action.getDataService()) ;
        
        JsonBuilder builder = new JsonBuilder() ;
        builder.startObject("outer") ;
        builder.key("task").value(asyncTask.getTaskId()) ;
        builder.finishObject("outer") ;
        return builder.build() ;
    }

    @Override
    protected void execDelete(HttpAction action) { ServletOps.error(HttpSC.METHOD_NOT_ALLOWED_405); }
    
    static class Task implements Runnable {
        static private Logger log = LoggerFactory.getLogger("Backup") ;
        
        private final long actionId ;
        
        public Task(HttpAction action) {
            actionId = action.id ;
        }

        @Override
        public void run() {
            try {
                log.info(format("[%d] >>>> Start", actionId)) ;
                Lib.sleep(5000) ;
                log.info(format("[%d] <<<< Finish", actionId)) ;
            } catch (Exception ex) {
                log.info(format("[%d] **** Exception", actionId), ex) ;
            }
        }
    }
}

