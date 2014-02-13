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

import org.apache.jena.atlas.json.JsonValue ;
import org.apache.jena.fuseki.servlets.HttpAction ;
import org.apache.jena.fuseki.servlets.ServletOps ;
import org.apache.jena.web.HttpSC ;

/** Base for actions that are container and also have action on items */ 
public abstract class ActionContainerItem extends ActionCtl {
    
    public ActionContainerItem() { super() ; }
    
    @Override
    final
    protected void perform(HttpAction action) {
        String method = action.request.getMethod() ;
        if ( method.equals(METHOD_GET) )
            execGet(action) ;
        else if ( method.equals(METHOD_POST) )
            execPost(action) ;
        else if ( method.equals(METHOD_DELETE) )
            execDelete(action) ;
        else
            ServletOps.error(HttpSC.METHOD_NOT_ALLOWED_405) ;
    }

    protected void execGet(HttpAction action) {
        JsonValue v ;
        if ( isContainerAction(action)  )
            v = execGetContainer(action) ;
        else
            v = execGetItem(action) ;
        
        ServletOps.sendJsonReponse(action, v);
    }
    
    /** GET request on the container - respond with JSON, or null for plain 200 */  
    protected abstract JsonValue execGetContainer(HttpAction action) ;
    /** GET request on an item in the container - repond with JSON, or null for plain 200 */  
    protected abstract JsonValue execGetItem(HttpAction action) ;

    private void execPost(HttpAction action) {
        JsonValue v ;
        if ( isContainerAction(action) )
            v = execPostContainer(action) ;
        else
            v = execPostItem(action) ;
        
        ServletOps.sendJsonReponse(action, v);
    }
    
    /** POST request on an item in the container - respond with JSON, or null for plain 200 */  
    protected abstract JsonValue execPostContainer(HttpAction action) ;
    /** POST request on an item in the container - respond with JSON, or null for plain 200 */  
    protected abstract JsonValue execPostItem(HttpAction action) ;

    // Must be an item
    protected abstract void execDelete(HttpAction action) ;

}
