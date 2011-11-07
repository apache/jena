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

package com.hp.hpl.jena.util;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;

/**
    Utility class for <code>ModelQueryUtil</code> which converts a query represented
    by a model with jqv: variables into a <code>.graph.query.Query</code> object.
    
 	@author kers
*/
public class QueryMapper
    {
    private Query query;
    private Node [] variables;
    private Graph graph;
    
    public QueryMapper( Model m, Resource [] variables )
        {
        super();
        this.graph = toQueryGraph( m );
        this.query = new Query( this.graph );
        this.variables = toQueryVariables( variables );
        }
        
    public Node [] getVariables()
        { return variables; }
        
    public Query getQuery()
        { return query; }
        
    public Graph getGraph()
        { return graph; }

    public Graph toQueryGraph( Model m )
        {
        StmtIterator st = m.listStatements();
        Graph result = Factory.createDefaultGraph();
        while (st.hasNext()) result.add( toQueryTriple( st.nextStatement() ) );
        return result;
        }
    
    public Triple toQueryTriple( Statement s )
        {
        return Triple.create
            ( 
            toQueryNode( s.getSubject() ), 
            toQueryNode( s.getPredicate() ), 
            toQueryNode( s.getObject() )
            );
        }
    
    public Node [] toQueryVariables( Resource [] vars )
        {
        Node [] result = new Node[vars.length];
        for (int i = 0; i < vars.length; i += 1) result[i] = toQueryNode( vars[i] );
        return result;
        }
    
    static final String varPrefix = "jqv:";
    
    public Node toQueryNode( RDFNode rn )
        {
        Node n = rn.asNode();
        return n.isURI() && n.getURI().startsWith( varPrefix )
            ? Node.createVariable( n.getURI().substring( varPrefix.length() ) )
            : n;
        }
        
    }
