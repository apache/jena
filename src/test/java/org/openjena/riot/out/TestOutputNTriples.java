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

package org.openjena.riot.out;

import java.io.ByteArrayOutputStream ;
import java.io.OutputStream ;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.riot.system.JenaWriterNTriples2 ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.RDFWriter ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public class TestOutputNTriples extends BaseTest
{
    // Read a file, write it to a string, read it again.  Test for same. 
    
    Triple t1 = SSE.parseTriple("(<x> <p> 123)") ;
    
    @Test public void ntriples0()
    {
        Model m = ModelFactory.createDefaultModel() ;
        m.getGraph().add(t1) ;
        OutputStream out = new ByteArrayOutputStream() ;
        RDFWriter w = new JenaWriterNTriples2() ;
        w.write(m, out, null) ;
    }
}
