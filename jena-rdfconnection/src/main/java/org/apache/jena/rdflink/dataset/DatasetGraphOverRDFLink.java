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

package org.apache.jena.rdflink.dataset;

import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.query.Query;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.engine.dispatch.DatasetGraphOverSparql;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.update.UpdateRequest;

/**
 * DatasetGraph implementation that implements all methods
 * against an RDFLink.
 * All returned iterators are backed by a fresh RDFLink instance.
 * The iterators must be closed to free the resources.
 */
public class DatasetGraphOverRDFLink
    extends DatasetGraphOverSparql
{
    private Creator<RDFLink> rdfLinkCreator;

    public DatasetGraphOverRDFLink(Creator<RDFLink> rdfLinkCreator) {
        super();
        this.rdfLinkCreator = rdfLinkCreator;
    }

    /** This method can be overridden. */
    public RDFLink newLink() {
        RDFLink link = rdfLinkCreator.create();
        return link;
    }

    public DatasetGraphOverRDFLink() {
        initContext();
    }

    @Override
    protected QueryExec query(Query query) {
        RDFLink link = newLink();
        QueryExec base = link.query(query);
        QueryExec result = new QueryExecWrapperCloseRDFLink(base, link);
        return result;
    }

    @Override
    protected UpdateExec update(UpdateRequest update) {
        return new UpdateExecOverRDFLink(this::newLink, null, null, update, null);
    }
}
