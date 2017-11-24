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

package org.apache.jena.fuseki.servlets ;


/**
 * Servlet for operations directly on a dataset - REST(ish) behaviour on the
 * dataset URI.
 */

public abstract class REST_Quads extends ActionREST
{
    public REST_Quads() {
        super() ;
    }

    @Override
    protected void doOptions(HttpAction action) {
        ServletOps.errorMethodNotAllowed("OPTIONS") ;
    }

    @Override
    protected void doHead(HttpAction action) {
        ServletOps.errorMethodNotAllowed("HEAD") ;
    }

    @Override
    protected void doPost(HttpAction action) {
        ServletOps.errorMethodNotAllowed("POST") ;
    }

    @Override
    protected void doPut(HttpAction action) {
        ServletOps.errorMethodNotAllowed("PUT") ;
    }

    @Override
    protected void doDelete(HttpAction action) {
        ServletOps.errorMethodNotAllowed("DELETE") ;
    }

    @Override
    protected void doPatch(HttpAction action) {
        ServletOps.errorMethodNotAllowed("PATCH") ;
    }
}
