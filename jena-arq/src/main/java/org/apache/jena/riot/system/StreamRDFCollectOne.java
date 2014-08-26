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

/** Take one item from a StreamRDF and present as a StreamRowRDF
 *  @see StreamRDF
 *  @see StreamRowRDF
 */
public class StreamRDFCollectOne implements StreamRDF {
    StreamRowRDF row = null ;
    private final PrefixMap pmap ;
    public StreamRDFCollectOne() {
        pmap = PrefixMapFactory.create() ;
    }
    
    public StreamRDFCollectOne(PrefixMap pmap) {
        this.pmap = pmap ;
    }

    @Override
    public void start() {}

    @Override
    public void triple(Triple triple) {
        row = new StreamRowRDFBase(triple) ;
    }

    @Override
    public void quad(Quad quad) {
        row = new StreamRowRDFBase(quad) ;
    }

    @Override
    public void base(String base) {}

    @Override
    public void prefix(String prefix, String iri) {
        row = null ;
        pmap.add(prefix, iri) ;
    }

    @Override
    public void finish() {}
    
    public StreamRowRDF getRow() {
        return row ;
    }
}
