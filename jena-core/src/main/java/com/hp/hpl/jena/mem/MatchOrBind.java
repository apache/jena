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

package com.hp.hpl.jena.mem;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.Domain;
import com.hp.hpl.jena.graph.query.QueryNode;

public abstract class MatchOrBind
    {
    public static MatchOrBind createSP( final ProcessedTriple Q )
        {
        return new MatchOrBind()
            {
            protected Domain d;
            protected final QueryNode S = Q.S;
            protected final QueryNode P = Q.P;
            
            @Override
            public MatchOrBind reset( Domain d )
                { this.d = d; return this; }
            
            @Override
            public boolean matches( Triple t )
                {
                return 
                    S.matchOrBind( d, t.getSubject() )
                    && P.matchOrBind( d, t.getPredicate() )
                    ;
                }
            };
        }
    
    public static MatchOrBind createPO( final ProcessedTriple Q )
        {
        return new MatchOrBind()
            {
            protected Domain d;
            protected final QueryNode P = Q.P;
            protected final QueryNode O = Q.O;
            
            @Override
            public MatchOrBind reset( Domain d )
                { this.d = d; return this; }
            
            @Override
            public boolean matches( Triple t )
                {
                return 
                    P.matchOrBind( d, t.getPredicate() )
                    && O.matchOrBind( d, t.getObject() )
                    ;
                }
            };
        }   
    public abstract boolean matches( Triple t );
    
    public abstract MatchOrBind reset( Domain d );
    }
