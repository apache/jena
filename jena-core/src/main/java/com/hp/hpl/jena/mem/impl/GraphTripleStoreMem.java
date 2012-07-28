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

package com.hp.hpl.jena.mem.impl;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.Triple.Field;
import com.hp.hpl.jena.graph.impl.TripleStore;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.mem.*;

public class GraphTripleStoreMem extends GraphTripleStoreBase implements TripleStore
    {    
    public GraphTripleStoreMem( Graph parent )
        { 
        super( parent,
            new NodeToTriplesMapMem( Field.fieldSubject, Field.fieldPredicate, Field.fieldObject ),
            new NodeToTriplesMapMem( Field.fieldPredicate, Field.fieldObject, Field.fieldSubject ),
            new NodeToTriplesMapMem( Field.fieldObject, Field.fieldSubject, Field.fieldPredicate )
                ); 
        }
    
    public NodeToTriplesMapMem getSubjects()
        { return (NodeToTriplesMapMem) subjects; }

    public NodeToTriplesMapMem getPredicates()
        { return (NodeToTriplesMapMem) predicates; }
    
    public NodeToTriplesMapMem getObjects()
        { return (NodeToTriplesMapMem) objects; }
    
    public Applyer createApplyer( ProcessedTriple pt )
        {
        if (pt.hasNoVariables())
            return containsApplyer( pt );
        if (pt.S instanceof QueryNode.Fixed) 
            return getSubjects().createFixedSApplyer( pt );
        if (pt.O instanceof QueryNode.Fixed) 
            return getObjects().createFixedOApplyer( pt );
        if (pt.S instanceof QueryNode.Bound) 
            return getSubjects().createBoundSApplyer( pt );
        if (pt.O instanceof QueryNode.Bound) 
            return getObjects().createBoundOApplyer( pt );
        return varSvarOApplyer( pt );
        }

    protected Applyer containsApplyer( final ProcessedTriple pt )
        { 
        return new Applyer()
            {
            @Override
            public void applyToTriples( Domain d, Matcher m, StageElement next )
                {
                Triple t = new Triple( pt.S.finder( d ), pt.P.finder( d ), pt.O.finder( d ) );
                if (objects.containsBySameValueAs( t )) next.run( d );
                }    
            };
        }

    protected Applyer varSvarOApplyer( final QueryTriple pt )
        { 
        return new Applyer()
            {
            protected final QueryNode p = pt.P;
        
            public Iterator<Triple> find( Domain d )
                {
                Node P = p.finder( d );
                if (P.isConcrete())
                    return predicates.iterator( P, Node.ANY, Node.ANY );
                else
                    return subjects.iterateAll();
                }
    
            @Override public void applyToTriples( Domain d, Matcher m, StageElement next )
                {
                Iterator<Triple> it = find( d );
                while (it.hasNext()) if (m.match( d, it.next() )) next.run( d );
                }
            };
        }
    }
