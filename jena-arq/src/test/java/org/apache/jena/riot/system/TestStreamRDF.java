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

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.riot.lang.StreamRDFCounting ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public class TestStreamRDF extends BaseTest {
    private static Triple triple1 = SSE.parseTriple("(<s> <p> <o>)") ;
    private static Quad quad1 = SSE.parseQuad("(<g> <s> <p> <o>)") ;
    
    
    @Test public void stream_count_01() {
        StreamRDFCounting stream = StreamRDFLib.count() ;
        stream.start();
        assertEquals(0, stream.count()) ;
        stream.triple(triple1) ;
        assertEquals(1, stream.count()) ;
        stream.triple(triple1) ;
        assertEquals(2, stream.count()) ;
        stream.finish() ;
        
        assertEquals(2, stream.count()) ;
        assertEquals(2, stream.countTriples()) ;
        assertEquals(0, stream.countQuads()) ;
    }
    
    @Test public void stream_count_02() {
        StreamRDFCounting stream = StreamRDFLib.count() ;
        stream.start();
        stream.triple(triple1) ;
        stream.quad(quad1) ;
        
        assertEquals(2, stream.count()) ;
        assertEquals(1, stream.countTriples()) ;
        assertEquals(1, stream.countQuads()) ;
        
        stream.finish();
    }
}

