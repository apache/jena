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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.NodeUtils;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb2.DatabaseMgr;

/** A {@link SecurityContext} is the things actor (user, role) is allowed to do. 
 * Currently version: the set of graphs, by graph name, they can access.
 * It can be inverted into a "deny" policy with {@link Predicate#negate()}.
 */ 
public class SecurityContext {
    
    public static SecurityContext NONE = new SecurityContext();
    public static SecurityContext DFT_GRAPH = new SecurityContext(true);

    private final Collection<Node> graphNames = new ArrayList<>();
    private final boolean matchDefaultGraph;
    
    private SecurityContext() {
        this(false);
    }

    private SecurityContext(boolean matchDefaultGraph) {
        this.matchDefaultGraph = matchDefaultGraph;
    }

    public SecurityContext(String...graphNames) {
        this(NodeUtils.convertToSetNodes(graphNames));
    }

    public SecurityContext(Node...graphNames) {
        this(Arrays.asList(graphNames));
    }

    public SecurityContext(Collection<Node> visibleGraphs) {
        this.graphNames.addAll(visibleGraphs);
        this.matchDefaultGraph = visibleGraphs.stream().anyMatch(Quad::isDefaultGraph);
        if ( matchDefaultGraph ) {
            this.graphNames.remove(Quad.defaultGraphIRI);
            this.graphNames.remove(Quad.defaultGraphNodeGenerated);
        }
    }
    
    public Collection<Node> visibleGraphs() {
        return Collections.unmodifiableCollection(graphNames);
    }
    public Collection<String> visibleGraphNames() {
        return graphNames.stream()
                .filter(Node::isURI)
                .map(Node::getURI)
                .collect(Collectors.toList()) ;
    }
    
    /**
     * Apply a filter suitable for the TDB-backed {@link DatasetGraph}, to the {@link Context} of the
     * {@link QueryExecution}. This does not modify the {@link DatasetGraph}
     */
    /*package*/ void filterTDB(DatasetGraph dsg, QueryExecution qExec) {
        GraphFilter<?> predicate = predicate(dsg);
        qExec.getContext().set(predicate.getContextKey(), predicate);
    }

    public QueryExecution createQueryExecution(String queryString, DatasetGraph dsg) {
        return createQueryExecution(QueryFactory.create(queryString), dsg);
    }
    
    public QueryExecution createQueryExecution(Query query, DatasetGraph dsg) {
        if ( ! DataAccessCtl.isAccessControlled(dsg) ) {
//            throw new InternalErrorException("SecurityContext.createQueryExecution called on an unsecured DatasetGraph");
//            // Internal error?
            // Already setup or no security context.
            return QueryExecutionFactory.create(query, dsg);
        }
        if ( isAccessControlledTDB(dsg) ) {
            QueryExecution qExec = QueryExecutionFactory.create(query, dsg);
            filterTDB(dsg, qExec);
            return qExec;
        }
        
        // XXX Does not work on GRAPH ?g {}
        DatasetGraph dsgA = DataAccessCtl.filteredDataset(dsg, this);
        return QueryExecutionFactory.create(query, dsgA);
    }
    
    @Override
    public String toString() {
        return "dft:"+matchDefaultGraph+" / "+graphNames.toString();
    }

    public Predicate<Quad> predicateQuad() {
        return quad -> {
            if ( quad.isDefaultGraph() )
                return matchDefaultGraph;
            if ( quad.isUnionGraph() ) 
                // Union graph is automatically there but its visible contents are different.
                return true;
            return graphNames.contains(quad.getGraph());
        };
    }

    /**
     * Create a GraphFilter for a TDB backed dataset.
     * 
     * @return GraphFilter
     * @throws IllegalArgumentException
     *             if not a TDB database, or a {@link DatasetGraphAccessControl} wrapped
     *             TDB database.
     */
    public GraphFilter<?> predicate(DatasetGraph dsg) {
        dsg = DatasetGraphAccessControl.removeWrapper(dsg);
        // dsg has to be the database dataset, not wrapped.
        //  DatasetGraphSwitchable is wrapped but should not be unwrapped. 
        if ( TDBFactory.isTDB1(dsg) )
            return filterTDB1(dsg);
        if ( DatabaseMgr.isTDB2(dsg) )
            return filterTDB2(dsg);
        throw new IllegalArgumentException("Not a TDB1 or TDB2 database: "+dsg.getClass().getSimpleName());
    }

    public boolean isAccessControlledTDB(DatasetGraph dsg) {
        DatasetGraph dsgBase = DatasetGraphAccessControl.unwrapOrNull(dsg);
        if ( dsgBase == null )
            return false;
        if ( TDBFactory.isTDB1(dsgBase) )
            return true;
        if ( DatabaseMgr.isTDB2(dsgBase) )
            return true;
        return false;
    }
    
    public GraphFilterTDB2 filterTDB2(DatasetGraph dsg) {
        GraphFilterTDB2 f = GraphFilterTDB2.graphFilter(dsg, graphNames, matchDefaultGraph);
        return f;
    }
    
    public GraphFilterTDB1 filterTDB1(DatasetGraph dsg) {
        GraphFilterTDB1 f = GraphFilterTDB1.graphFilter(dsg, graphNames, matchDefaultGraph);
        return f; 
    }
}
