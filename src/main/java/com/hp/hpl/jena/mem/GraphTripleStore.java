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

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.Triple.*;
import com.hp.hpl.jena.graph.impl.TripleStore;

/**
    GraphTripleStore - the underlying triple-indexed triple store for GraphMem et al,
    ripped out from the heart of GraphMem as part of simplifying the reification code.
    A GraphTripleStore is a searchable repository for triples. 

*/

public class GraphTripleStore extends GraphTripleStoreBase implements TripleStore
    {   
    public GraphTripleStore( Graph parent )
        { 
        super( parent,
            new NodeToTriplesMap( Field.fieldSubject, Field.fieldPredicate, Field.fieldObject ),
            new NodeToTriplesMap( Field.fieldPredicate, Field.fieldObject, Field.fieldSubject ),
            new NodeToTriplesMap( Field.fieldObject, Field.fieldSubject, Field.fieldPredicate )
            ); 
        }
    }
