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
package org.apache.jena.arq.querybuilder.handlers;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.jena.graph.FrontsNode;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Literal;
import org.apache.jena.graph.Node_URI;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;

/**
 * Handler for a dataset.
 *
 */
public class DatasetHandler implements Handler {

    // the query to manage
    private final Query query;

    /**
     * Constructor.
     *
     * @param query The query the handler will manage.
     */
    public DatasetHandler(Query query) {
        this.query = query;
    }

    /**
     * Add all the dataset information from the handler argument.
     *
     * @param datasetHandler The handler to copy from.
     */
    public void addAll(DatasetHandler datasetHandler) {
        from(datasetHandler.query.getGraphURIs());
        fromNamed(datasetHandler.query.getNamedGraphURIs());
    }

    /**
     * Converts an object into a graph name.
     * @param graphName the object that represents the graph name.
     * @return the string that is the graph name.
     */
    String asGraphName(Object graphName) {
        // package private for testing access
        if (graphName instanceof String) {
            return (String) graphName;
        }
        if (graphName instanceof FrontsNode) {
            return asGraphName(((FrontsNode)graphName).asNode());
        }
        if (graphName instanceof Node_URI) {
            return ((Node_URI)graphName).getURI();
        }
        if (graphName instanceof Node_Literal) {
            return asGraphName(((Node_Literal)graphName).getLiteralValue());
        }

        return graphName.toString();
    }

    /**
     * Add one or more named graphs to the query.
     * if {@code graphName} is a {@code collection} or an array each element in the
     * @code collection} or array is converted to a string and the result added to the
     * query.
     *
     * @param graphName the name to add.
     * @see #asGraphName(Object)
     */
    public void fromNamed(Object graphName) {
        processGraphName(query::addNamedGraphURI, graphName);
    }

    /**
     * Converts performs a single level of unwrapping an Iterable before calling
     * the {@code process} method with the graph name converted to a string.
     * @param process the process that accepts the string graph name.
     * @param graphName the Object that represents one or more graph names.
     * @see #asGraphName(Object)
     */
    private void processGraphName(Consumer<String> process, Object graphName) {
        if (graphName instanceof Iterable) {
            for (Object o : (Iterable<?>)graphName) {
                process.accept(asGraphName(o));
            }
        } else {
            process.accept(asGraphName(graphName));
        }
    }

    /**
     * Add one or more graph names to the query.
     * if {@code graphName} is a {@code collection} or an array each element in the
     * @code collection} or array is converted to a string and the result added to the
     * query.
     *
     * @param graphName the name to add.
     * @see #asGraphName(Object)
     */
    public void from(Object graphName) {
        processGraphName(query::addGraphURI, graphName);
    }

    /**
     * Set the variables for field names that contain lists of strings.
     *
     * Strings that start with "?" are assumed to be variable names and will be
     * replaced with matching node.toString() values.
     *
     * @param values The values to set.
     * @param fieldName The field name in Query that contain a list of strings.
     */
    private void setVars(Map<Var, Node> values, List<String> lst) {
        if (values.isEmpty() || lst == null || lst.isEmpty()) {
            return;
        }

        for (int i = 0; i < lst.size(); i++) {
            String s = lst.get(i);
            Node n = null;
            if (s.startsWith("?")) {
                Var v = Var.alloc(s.substring(1));
                n = values.get(v);
                // Need a raw string for URIs and literals.
                // Protect against choice of toString.
                String x = s ;
                if ( n != null ) {
                    if ( n.isURI() )
                        x = n.getURI();
                    else if ( n.isLiteral() )
                        x = n.getLiteralLexicalForm() ;
                    else
                        x = n.toString();
                }
                lst.set(i, x);
            }
        }
    }

    @Override
    public void setVars(Map<Var, Node> values) {
        setVars(values, query.getNamedGraphURIs());
        setVars(values, query.getGraphURIs());
    }

    @Override
    public void build() {
        // nothing special to do
    }

}
