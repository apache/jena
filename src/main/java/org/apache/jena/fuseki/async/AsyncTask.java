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

package org.apache.jena.fuseki.async;

import java.util.concurrent.Callable ;

import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.server.DataService ;
import org.slf4j.Logger ;

/** An asynchronous task */ 
public class AsyncTask implements Callable<Object>  
{
    private static Logger log = Fuseki.serverLog ; 
    
    private Callable<Object> callable ;
    private AsyncPool pool ;

    private final String displayName ;
    private final DataService dataService ;

    public AsyncTask(Callable<Object> callable, 
                     AsyncPool pool,
                     String displayName,
                     DataService dataService ) {
        this.callable = callable ;
        this.pool = pool ;
        this.displayName = displayName ;
        this.dataService = dataService ;
    }

    /** Display name - no newlines */
    public String displayName() { return displayName ; }
    
    public DataService getDataService() { return dataService ; }

    @Override
    public Object call() {
        try { return  callable.call() ; } 
        catch (Exception ex) {
            log.error("Async task threw an expection", ex) ;
            return null ; 
        }
        finally { pool.finished(this) ; } 
    }
}

