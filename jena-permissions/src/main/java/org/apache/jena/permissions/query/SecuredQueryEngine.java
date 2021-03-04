/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.permissions.query;

import java.security.Principal;
import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.permissions.query.rewriter.OpRewriter;
import org.apache.jena.query.Query;
import org.apache.jena.permissions.graph.SecuredGraph;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.main.QueryEngineMain;
import org.apache.jena.sparql.util.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecuredQueryEngine extends QueryEngineMain {
    private static Logger LOG = LoggerFactory.getLogger(SecuredQueryEngine.class);

    private SecurityEvaluator securityEvaluator;
    private Node graphIRI;

    public SecuredQueryEngine(final Query query, final DatasetGraph dataset, final Binding input,
            final Context context) {
        super(query, dataset, input, context);
        setGraphIRI(dataset);
    }

    public SecurityEvaluator getSecurityEvaluator() {
        return securityEvaluator;
    }

    @Override
    protected Op modifyOp(final Op op) {
        final OpRewriter rewriter = new OpRewriter(securityEvaluator, graphIRI);
        LOG.debug("Before: {}", op);
        op.visit(rewriter);
        Op result = rewriter.getResult();
        result = result == null ? op : result;
        LOG.debug("After: {}", result);
        result = super.modifyOp(result);
        LOG.debug("After Optimize: {}", result);
        return result;
    }

    private void setGraphIRI(final DatasetGraph dataset) {
        final Graph g = dataset.getDefaultGraph();
        if (g instanceof SecuredGraph) {
            final SecuredGraph sg = (SecuredGraph) g;
            graphIRI = sg.getModelNode();
            this.securityEvaluator = sg.getSecurityEvaluator();
        } else {
            graphIRI = NodeFactory.createURI("urn:x-arq:DefaultGraph");
            this.securityEvaluator = new SecurityEvaluator() {

                @Override
                public boolean evaluate(final Object principal, final Action action, final Node graphIRI) {
                    return true;
                }

                @Override
                public boolean evaluate(final Object principal, final Action action, final Node graphIRI,
                        final Triple triple) {
                    return true;
                }

                @Override
                public boolean evaluate(final Object principal, final Set<Action> action, final Node graphIRI) {
                    return true;
                }

                @Override
                public boolean evaluate(final Object principal, final Set<Action> action, final Node graphIRI,
                        final Triple triple) {
                    return true;
                }

                @Override
                public boolean evaluateAny(final Object principal, final Set<Action> action, final Node graphIRI) {
                    return true;
                }

                @Override
                public boolean evaluateAny(final Object principal, final Set<Action> action, final Node graphIRI,
                        final Triple triple) {
                    return true;
                }

                @Override
                public boolean evaluateUpdate(final Object principal, final Node graphIRI, final Triple from,
                        final Triple to) {
                    return true;
                }

                @Override
                public Principal getPrincipal() {
                    return null;
                }

                @Override
                public boolean isPrincipalAuthenticated(Object principal) {
                    return true;
                }
            };

        }
    }
}
