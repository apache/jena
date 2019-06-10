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

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.fuseki.servlets.ActionService;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.sparql.core.DatasetGraph;

/**
 * Wrapper for an {@link ActionService} that rejects a request to a graph-access-controlled dataset.
 * Typically, that's one of the update operations -
 * Graph-access-controlled dataset only support read (query and GSP GET).
 */
public class AccessCtl_DenyUpdate extends ActionService {

    private final ActionService other;
    private final Function<HttpAction, String> requestUser;
    private final String label;

    public AccessCtl_DenyUpdate(ActionService other, String label,
                                Function<HttpAction, String> determineUser) {
        this.other = other;
        this.label = label;
        this.requestUser = determineUser;
    }

    @Override
    public void validate(HttpAction action) {
        DatasetGraph dsg = action.getDataset();
        if ( DataAccessCtl.isAccessControlled(dsg) ) {
            if ( label == null )
                ServletOps.errorBadRequest("Not supported");
            ServletOps.errorBadRequest(label+" : not supported");
            throw new InternalErrorException("AccessCtl_DenyUpdate: "+ "didn't reject request");
        }
        other.validate(action);
    }

    @Override
    public void execute(HttpAction action) {
        other.execute(action);
    }
}
