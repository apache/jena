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

package org.apache.jena.fuseki.access;

import java.util.function.Function;

import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.REST_Quads_R;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphWrapper;
import org.apache.jena.sparql.util.Context;

public class Filtered_REST_Quads_R extends REST_Quads_R {
    public Filtered_REST_Quads_R(Function<HttpAction, String> determineUser) {}

    @Override
    protected void validate(HttpAction action) {
        super.validate(action);
    }

    @Override
    protected void doGet(HttpAction action) {
        
        DatasetGraph dsg0 = action.getActiveDSG();
        DatasetGraph dsg = new DatasetGraphWrapper(dsg0) {
            @Override public Context getContext() { return super.getContext(); }
        };
        // Replace datasetGraph
        // XXX Implement access control for REST_Quads_R

        HttpAction action2 = action;
        
        super.doGet(action2);
    }
}
