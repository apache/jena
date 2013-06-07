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

package org.apache.jena.dsg2;

import org.apache.jena.atlas.lib.Tuple ;
import org.apache.jena.riot.system.StreamRDF ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;

class StreamRDFSplitter implements StreamRDF
{
    protected final StreamRDF stream1 ;
    protected final StreamRDF stream2 ;

    public StreamRDFSplitter(StreamRDF stream1, StreamRDF stream2) {
        this.stream1 = stream1 ;
        this.stream2 = stream2 ;
    }
    
    @Override
    public void start()
    { stream1.start() ; stream2.start() ; }

    @Override
    public void triple(Triple triple)
    { stream1.triple(triple) ; stream2.triple(triple) ; }

    @Override
    public void quad(Quad quad)
    { stream1.quad(quad) ; stream2.quad(quad) ; }

    @Override
    public void tuple(Tuple<Node> tuple)
    { stream1.tuple(tuple) ; stream2.tuple(tuple) ; }

    @Override
    public void base(String base)
    { stream1.base(base) ; stream2.base(base) ; }

    @Override
    public void prefix(String prefix, String iri)
    { stream1.prefix(prefix, iri) ; stream2.prefix(prefix, iri) ; }

    @Override
    public void finish()
    { stream1.finish() ; stream2.finish() ; }

}
