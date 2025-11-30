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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QueryCancelledException;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.adapter.ParseCheckUtils;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.Timeouts;
import org.apache.jena.sparql.engine.Timeouts.Timeout;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateRequest;

/**
 * Deferred update execution that allocates all resources in the
 * execute() method. Note, that UpdateExec does not have a close method,
 * so all needed resources are allocated and closed during {@link #execute()}.
 */
public class UpdateExecOverRDFLink
    implements UpdateExec
{
    private Creator<RDFLink> linkCreator;
    private boolean closeLink;
    private Map<Var, Node> substitutionMap;
    private Context requestContext;

    private boolean parseCheck;
    private UpdateRequest updateRequest;
    private String updateRequestString;

    private Object cancelLock = new Object();

    private volatile boolean isAborted = false;
    private volatile boolean isExecStarted = false;
    private volatile UpdateExec delegate = null;

    public UpdateExecOverRDFLink(Creator<RDFLink> linkCreator, boolean closeLink,
            Map<Var, Node> substitutionMap, Context context,
            boolean parseCheck, UpdateRequest updateRequest, String updateRequestString) {
        super();
        this.linkCreator = linkCreator;
        this.closeLink = closeLink;
        this.substitutionMap = substitutionMap;
        this.requestContext = context;
        this.parseCheck = parseCheck;
        this.updateRequest = updateRequest;
        this.updateRequestString = updateRequestString;
    }

    @Override
    public UpdateRequest getUpdateRequest() {
        return updateRequest;
    }

    @Override
    public String getUpdateRequestString() {
        return updateRequestString;
    }

    /**
     * If the execution has not been started then the context configured with this instance
     * is returned. Otherwise the context of the delegate is returned.
     */
    @Override
    public Context getContext() {
        return delegate == null ? requestContext : delegate.getContext();
    }

    @Override
    public void abort() {
        synchronized (cancelLock) {
            isAborted = true;
            if (delegate != null) {
                delegate.abort();
            }
        }
    }

    @Override
    public void execute() {
        RDFLink link = null;
        try {
            synchronized (cancelLock) {
                if (isExecStarted) {
                    throw new IllegalStateException("Execution was already stated.");
                }
                isExecStarted = true;

                if (isAborted) {
                    throw new QueryCancelledException();
                }

                link = linkCreator.create();
                UpdateExecBuilder r = link.newUpdate();

                r = r.parseCheck(parseCheck);

                if (requestContext != null) {
                    r = r.context(requestContext);
                    Timeout timeout = Timeouts.extractUpdateTimeout(requestContext);
                    applyTimeouts(r, timeout);
                }

                if (substitutionMap != null) {
                    for (Entry<Var, Node> e : substitutionMap.entrySet()) {
                        r = r.substitution(e.getKey(), e.getValue());
                    }
                }

                Optional<Boolean> parseCheck = ParseCheckUtils.getParseCheck(requestContext);
                if (parseCheck.isPresent()) {
                    r = r.parseCheck(parseCheck.get());
                }

                if (updateRequest != null) {
                    r = r.update(updateRequest);
                } else {
                    r = r.update(updateRequestString);
                }

                delegate = r.build();
            }

            delegate.execute();
        } finally {
            if (link != null && closeLink) {
                link.close();
            }
        }
    }

    private static void applyTimeouts(UpdateExecBuilder uExec, Timeout t) {
        if (t != null) {
            if (t.hasOverallTimeout()) {
                uExec.timeout(t.overallTimeout().amount(), t.overallTimeout().unit());
            }
        }
    }
}
