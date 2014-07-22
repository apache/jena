/**
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

package org.apache.jena.riot.system;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;

public class StreamRowRDFBase implements StreamRowRDF {
    private final Triple triple ;
    private final Quad quad ;

    public StreamRowRDFBase(Triple triple) {
        this(triple, null) ;
    }

    public StreamRowRDFBase(Quad quad) {
        this(null, quad) ;
    }
    
    private StreamRowRDFBase(Triple triple, Quad quad) {
        this.triple = triple ;
        this.quad = quad ;
    }            
    
    @Override
    public boolean isTriple() {
        return triple != null ;
    }

    @Override
    public Triple getTriple() {
        return triple ;
    }

    @Override
    public boolean isQuad() {
        return quad != null ;
    }

    @Override
    public Quad getQuad() {
        return quad ;
    }
}
