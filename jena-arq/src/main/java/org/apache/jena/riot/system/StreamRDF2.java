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

/** Send to two stream */
public class StreamRDF2 implements StreamRDF 
{
    protected final StreamRDF sink1 ;
    protected final StreamRDF sink2 ;

    public StreamRDF2(StreamRDF sink1, StreamRDF sink2) {
        this.sink1 = sink1 ;
        this.sink2 = sink2 ;
    }
    
    @Override
    public void start() {
        sink1.start() ;
        sink2.start() ;
    }

    @Override
    public void triple(Triple triple) {
        sink1.triple(triple) ;
        sink2.triple(triple) ;
    }

    @Override
    public void quad(Quad quad) {
        sink1.quad(quad) ;
        sink2.quad(quad) ;
    }

    @Override
    public void base(String base) {
        sink1.base(base) ;
        sink2.base(base) ;
    }

    @Override
    public void prefix(String prefix, String iri) {
        sink1.prefix(prefix, iri) ;
        sink2.prefix(prefix, iri) ;
    }

    @Override
    public void finish() {
        sink1.finish() ;
        sink2.finish() ;
    }

}
