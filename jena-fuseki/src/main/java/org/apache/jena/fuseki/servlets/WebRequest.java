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

package org.apache.jena.fuseki.servlets;

import java.util.HashMap ;
import java.util.Map ;

import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.atlas.logging.Log ;

public class WebRequest
{
    // ActionLifescycle?
    
    // This is the object created for an action.
    Map <String, String> headers = new HashMap<String, String>() ;
    public final long id ;

    private boolean startTimeIsSet = false ;
    private boolean finishTimeIsSet = false ;

    private long startTime = -2 ;
    private long finishTime = -2 ;
    
    // Outcome.
    int statusCode = -1 ;
    String message = null ;
    int contentLength = -1 ;
    String contentType = null ;
    
    // In production, don't hold onto large objects like the request or response for too long.
    private HttpServletRequest request ;
    private HttpServletResponseTracker response ;
    
    public WebRequest(long id, HttpServletRequest request , HttpServletResponse response )
    {
        this.id = id ;
        this.request = request ;
        this.response = new HttpServletResponseTracker(this, response) ; 
    }
    
    /** Reduce to a size that can be kept around for sometime */  
    public void minimize()
    {
        this.request = null ;
        this.response = null ;
    }

    public void setStartTime() {
        if ( startTimeIsSet ) 
            Log.warn(this,  "Start time reset") ;
        startTimeIsSet = true ;
        this.startTime = System.nanoTime() ;
    }

    public void setFinishTime() {
        if ( finishTimeIsSet ) 
            Log.warn(this,  "Finish time reset") ;
        finishTimeIsSet = true ;
        this.finishTime = System.nanoTime() ;
    }

    /** Return the recorded time taken in milliseconds. 
     *  {@linkplain #setStartTime} and {@linkplain #setFinishTime}
     *  must have been called.
     */
    public long getTime()
    {
        if ( ! startTimeIsSet ) 
            Log.warn(this,  "Start time not set") ;
        if ( ! finishTimeIsSet ) 
            Log.warn(this,  "Finish time not set") ;
        return (finishTime-startTime)/(1000*1000) ;
    }

    public Map <String, String> getHeaders()    { return headers ; } 
    
    public HttpServletRequest getRequest()      { return request ; }

    public HttpServletResponseTracker getResponse()    { return response ; }
    
    
}

