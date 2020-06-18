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

package org.apache.jena.fuseki.servlets;

/** The "everything unsupported" ActionREST implemenation */
public class BaseActionREST extends ActionREST {

    @Override
    protected void doGet(HttpAction action)  { notSupported(action); }

    @Override
    protected void doHead(HttpAction action) { notSupported(action); }

    @Override
    protected void doPost(HttpAction action)  { notSupported(action); }

    @Override
    protected void doPatch(HttpAction action) { notSupported(action); }

    @Override
    protected void doDelete(HttpAction action) { notSupported(action); }

    @Override
    protected void doPut(HttpAction action) { notSupported(action); }

    @Override
    protected void doOptions(HttpAction action) { notSupported(action); }

    @Override
    public void validate(HttpAction action) { }

    private void notSupported(HttpAction action) {
        ServletOps.errorMethodNotAllowed(action.getMethod()+" "+action.getDatasetName());
    }
}
