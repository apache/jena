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
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.async.AsyncPool ;
import org.apache.jena.fuseki.async.AsyncTask ;
import org.apache.jena.fuseki.servlets.ActionBase ;
import org.apache.jena.fuseki.servlets.HttpAction ;
import org.apache.jena.fuseki.servlets.ServletOps ;
import org.apache.jena.web.HttpSC ;

public class ActionTasks extends ActionBase //ActionContainerItem
{
    private static AsyncPool[] pools = { AsyncPool.get() } ; 
    
    public ActionTasks() { super(Fuseki.serverLog) ; }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        doCommon(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        doCommon(request, response);
    }

    private static String prefix = "/" ;
    
    @Override
    protected void execCommonWorker(HttpAction action) {
        String name = extractItemName(action) ;
        if ( name != null ) {
            if ( name.startsWith(prefix))
                name = name.substring(prefix.length()) ; 
            else
                log.warn("Unexpected task name : "+name) ;
        }
        
        String method = action.request.getMethod() ;
        if ( method.equals(METHOD_GET) )
            execGet(action, name) ;
        else if ( method.equals(METHOD_POST) )
            execPost(action, name) ;
        else
            ServletOps.error(HttpSC.METHOD_NOT_ALLOWED_405) ;
    }

    private void execGet(HttpAction action, String name) {
        if ( name == null )
            log.info(format("[%d] Tasks", action.id));
        else
            log.info(format("[%d] Task %s", action.id, name));

        JsonValue responseBody = null ;
        
        if ( name == null ) {
            JsonBuilder builder = new JsonBuilder() ;
            builder.startArray() ;
            
            for ( AsyncPool pool : pools ) {
                for ( AsyncTask aTask : pool.tasks() ) {
                    //builder.value(aTask.getTaskId()) ;
                    descOneTask(builder, aTask) ;
                }
            }
            builder.finishArray() ;
            responseBody = builder.build(); 
        } else {
            for ( AsyncPool pool : pools ) {
                // Assumes first is only.
                AsyncTask aTask = pool.getTask(name) ;
                if ( aTask != null ) {
                    JsonBuilder builder = new JsonBuilder() ;
                    descOneTask(builder, aTask);
                    responseBody = builder.build() ;
                }
            }
        }
        
        if ( responseBody == null )
            ServletOps.errorNotFound("Task '"+name+"' not found") ;
        ServletOps.setNoCache(action) ; 
        ServletOps.sendJsonReponse(action, responseBody); 
    }

    private void execPost(HttpAction action, String name) {
        
    }
    
    private static void descOneTask(JsonBuilder builder, AsyncTask aTask) {
        builder.startObject("SingleTask") ;
        builder.key(JsonConst.task).value(aTask.displayName()) ;
        builder.key(JsonConst.taskId).value(aTask.getTaskId()) ;
        if ( aTask.getStartPoint() != null )
            builder.key(JsonConst.started).value(aTask.getStartPoint()) ;
        if ( aTask.getFinishPoint() != null )
            builder.key(JsonConst.finished).value(aTask.getFinishPoint()) ;
        builder.finishObject("SingleTask") ;
    }
}

