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

package org.apache.jena.riot.system ;

import org.apache.jena.riot.lang.StreamRDFCounting ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** Wrap another StreamRDF and provide counts of items */
public class StreamRDFCountingBase extends StreamRDFWrapper implements StreamRDF, StreamRDFCounting {
    private long countTriples  = 0 ;
    private long countQuads    = 0 ;
    private long countBase     = 0 ;
    private long countPrefixes = 0 ;

    public StreamRDFCountingBase(StreamRDF other) {
        super(other) ;
    }

    @Override
    public void triple(Triple triple) {
        countTriples++ ;
        super.triple(triple) ;
    }

    @Override
    public void quad(Quad quad) {
        countQuads++ ;
        super.quad(quad) ;
    }

    @Override
    public long count() {
        return countTriples + countQuads ;
    }

    @Override
    public long countTriples() {
        return countTriples ;
    }

    @Override
    public long countQuads() {
        return countQuads ;
    }
}
