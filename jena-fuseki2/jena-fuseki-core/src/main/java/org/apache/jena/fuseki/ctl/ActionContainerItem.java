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

import static org.apache.jena.riot.web.HttpNames.METHOD_DELETE;
import static org.apache.jena.riot.web.HttpNames.METHOD_GET;
import static org.apache.jena.riot.web.HttpNames.METHOD_POST;

import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.fuseki.servlets.ActionLib;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.web.HttpSC;

/** Base for actions that are container and also have actions on items */
public abstract class ActionContainerItem extends ActionCtl {

    protected ActionContainerItem() { super(); }

    @Override
    final
    public void execute(HttpAction action) {
        String method = action.request.getMethod();
        if ( method.equals(METHOD_GET) )
            performGet(action);
        else if ( method.equals(METHOD_POST) )
            performPost(action);
        else if ( method.equals(METHOD_DELETE) )
            performDelete(action);
        else
            ServletOps.error(HttpSC.METHOD_NOT_ALLOWED_405);
    }


    @Override
    public void execOptions(HttpAction action) {
        ActionLib.doOptionsGetPostDeleteHead(action);
        ServletOps.success(action);
    }

    final
    // Container action if the name used to route to the servlet has more path.
    protected boolean isContainerAction(HttpAction action) {
        return (getItemName(action) == null );
    }

    protected void performGet(HttpAction action) {
        JsonValue v;
        if ( isContainerAction(action)  )
            v = execGetContainer(action);
        else
            v = execGetItem(action);

        ServletOps.sendJsonReponse(action, v);
    }

    /** GET request on the container - respond with JSON, or null for plain 200 */
    protected abstract JsonValue execGetContainer(HttpAction action);
    /** GET request on an item in the container - respond with JSON, or null for plain 200 */
    protected abstract JsonValue execGetItem(HttpAction action);

    protected void performPost(HttpAction action) {
        JsonValue v;
        if ( isContainerAction(action) )
            v = execPostContainer(action);
        else
            v = execPostItem(action);

        ServletOps.sendJsonReponse(action, v);
    }

    /** POST request on the container - respond with JSON, or null for plain 200 */
    protected abstract JsonValue execPostContainer(HttpAction action);
    /** POST request on an item in the container - respond with JSON, or null for plain 200 */
    protected abstract JsonValue execPostItem(HttpAction action);


    /** DELETE request */
    protected void performDelete(HttpAction action) {
        if ( isContainerAction(action)  )
            execDeleteContainer(action);
        else
            execDeleteItem(action);
        ServletOps.success(action);
    }

    /** DELETE request on an item in the container */
    protected void execDeleteContainer(HttpAction action) {
        ServletOps.errorMethodNotAllowed(METHOD_DELETE, "DELETE applied to a container");
    }

    /** DELETE request on an item in the container */
    protected void execDeleteItem(HttpAction action) {
        ServletOps.errorMethodNotAllowed(METHOD_DELETE);
    }
}
