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

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.fuseki.servlets.ActionService;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServletOps;

/**
 * An {@link ActionService} that rejects requests.
 */
public class AccessCtl_Deny extends ActionService {

    private final String label;

    public AccessCtl_Deny(String label) {
        this.label = label;
    }

    @Override
    public void validate(HttpAction action) {
        if ( label == null )
            ServletOps.errorBadRequest("Not supported");
        ServletOps.errorBadRequest(label+" : not supported");
    }

    @Override
    public void execute(HttpAction action) {
        throw new InternalErrorException("AccessCtl_DenyUpdate: "+ "didn't reject request");
    }
}
