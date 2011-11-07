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
import com.hp.hpl.jena.graph.query.Domain;
import com.hp.hpl.jena.util.iterator.*;

import java.util.*;

/**
    A utility for using the graph query interface from a Model. Queries may be represented
    as models, where each statement in the model corresponds to a search for matching
    statements in the model being queried. Variables are represented as resources
    with URIs using the ficticious "jqv" protocol.
<p>    
    See also <code>QueryMapper</code>.
    
 	@author kers
*/
public class ModelQueryUtil
    {
    private ModelQueryUtil()
        {}
    
    public static ExtendedIterator<List<? extends RDFNode>> queryBindingsWith
        ( final Model model, Model query, Resource [] variables )
        {
        Map1<Domain, List<? extends RDFNode>> mm = new Map1<Domain, List<? extends RDFNode>>()
            { @Override
            public List<? extends RDFNode> map1( Domain x ) { return mappy( model, x ); } };
        QueryMapper qm = new QueryMapper( query, variables );
        return
            qm.getQuery().executeBindings( model.getGraph(), qm.getVariables() )
            .mapWith( mm )
            ;
        }

    public static RDFNode asRDF( Model m, Node n )
        { return m.asRDFNode( n ); }
        
    public static List<RDFNode> mappy( Model m, Domain L )
        {
        ArrayList<RDFNode> result = new ArrayList<RDFNode>( L.size() );
        for (int i = 0; i < L.size(); i += 1) result.add( asRDF( m, L.get( i ) ) );
        return result;
        }

    }
