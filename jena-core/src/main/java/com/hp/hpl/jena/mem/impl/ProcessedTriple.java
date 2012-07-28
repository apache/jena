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

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;

/**
    A ProcessedTriple is three QueryNodes; it knows how to deliver an
    optimised Matcher which will use only the necessary QueryNode.match
    methods.

*/
public class ProcessedTriple extends QueryTriple
    {    
    public ProcessedTriple( QueryNode S, QueryNode P, QueryNode O ) 
        { super( S, P, O ); }

    static final QueryNodeFactory factory = new QueryNodeFactoryBase()
        {
        @Override
        public QueryTriple createTriple( QueryNode S, QueryNode P, QueryNode O )
            { return new ProcessedTriple( S, P, O ); }
        
        @Override
        public QueryTriple [] createArray( int size )
            { return new ProcessedTriple[size]; }
        };

    @Override
    public Applyer createApplyer( Graph g )
        { return ((GraphMem) g).createApplyer( this ); }

    public boolean hasNoVariables()
        { return S.isFrozen() && P.isFrozen() && O.isFrozen(); }
    }
