/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.rdflink.dataset;

import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.exec.UpdateExecBuilderBase;
import org.apache.jena.update.UpdateRequest;

/**
 * A deferred update execution builder over a creator of RDFLink instances.
 * Calling {@link UpdateExec#execute()} on the built {@link UpdateExec} will perform the following:
 * <ol>
 *   <li>Acquire a fresh link.</li>
 *   <li>From the link, acquire the actual update exec builder via {@link RDFLink#newUpdate()}.</li>
 *   <li>Apply the settings of this update builder to the actual one.</li>
 *   <li>Call {@link UpdateExec#execute()} on the actual builder.</li>
 *   <li>Close the link.</li>
 * </ol>
 */
public class UpdateExecBuilderOverRDFLink
    extends UpdateExecBuilderBase<UpdateExecBuilderOverRDFLink>
{
    private Creator<RDFLink> linkCreator;

    /**
     *
     * @param linkCreator The factory for links from which the actual UpdateExecBuilder will be taken.
     * @param dataset An optional dataset
     */
    public UpdateExecBuilderOverRDFLink(Creator<RDFLink> linkCreator, DatasetGraph dataset) {
        super();
        this.linkCreator = linkCreator;
        this.dataset = dataset;
    }

    @Override
    public UpdateExec build() {
        UpdateRequest updateRequest = null;
        String updateRequestString = null;
        if (updateEltAcc.isParsed()) {
            updateRequest = updateEltAcc.buildUpdateRequest();
        } else {
            updateRequestString = updateEltAcc.buildString();
        }

        boolean parseCheck = effectiveParseCheck();
        return new UpdateExecOverRDFLink(linkCreator, true, substitutionMap, contextAcc.context(), parseCheck, updateRequest, updateRequestString);
    }
}
