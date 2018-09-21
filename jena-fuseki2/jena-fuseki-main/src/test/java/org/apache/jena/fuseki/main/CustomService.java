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

package org.apache.jena.fuseki.main;

import java.io.IOException;

import org.apache.jena.fuseki.servlets.ActionREST;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.riot.WebContent;
import org.apache.jena.web.HttpSC;

public class CustomService extends ActionREST {

    // do* -- the operations to accept
    
    @Override
    protected void doGet(HttpAction action) {
        action.response.setStatus(HttpSC.OK_200);
        try {
            action.response.setContentType(WebContent.contentTypeTextPlain);
            action.response.getOutputStream().println("    ** Hello world (GET) **");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doHead(HttpAction action) {
        action.response.setStatus(HttpSC.OK_200);
        action.response.setContentType(WebContent.contentTypeTextPlain);
    }

    @Override
    protected void doPost(HttpAction action) {
        action.response.setStatus(HttpSC.OK_200);
        try {
            action.response.setContentType(WebContent.contentTypeTextPlain);
            action.response.getOutputStream().println("    ** Hello world (POST) **");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPatch(HttpAction action) { notSupported(action); }

    @Override
    protected void doDelete(HttpAction action) { notSupported(action); }

    @Override
    protected void doPut(HttpAction action) { notSupported(action); }

    @Override
    protected void doOptions(HttpAction action) { notSupported(action); }

    @Override
    protected void validate(HttpAction action) { }
    
    private void notSupported(HttpAction action) {
        ServletOps.errorMethodNotAllowed(action.getMethod()+" "+action.getDatasetName());
    }
}
