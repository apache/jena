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
import org.apache.jena.fuseki.servlets.SPARQL_Upload;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.sparql.core.DatasetGraph;

/**
 * Filter for {@link REST_Quads_R} that inserts a security filter on read-access to the
 * {@link DatasetGraph}.
 */
public class Filtered_SPARQL_Upload extends SPARQL_Upload {
    
    private final Function<HttpAction, String> requestUser;
    
    public Filtered_SPARQL_Upload(Function<HttpAction, String> determineUser) {
        this.requestUser = determineUser; 
    }

    @Override
    protected void validate(HttpAction action) {
        super.validate(action);
        DatasetGraph dsg = action.getDataset();
        if ( ! DataAccessCtl.isAccessControlled(dsg) )
            return;
        ServletOps.errorBadRequest("Upload not supported");
    }
}
