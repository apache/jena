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

package org.openjena.riot.out;

import java.io.ByteArrayInputStream ;
import java.io.ByteArrayOutputStream ;
import java.io.InputStream ;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.riot.Lang ;
import org.openjena.riot.RiotLoader ;
import org.openjena.riot.RiotWriter ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public class TestOutputRDFJSON extends BaseTest
{
    // Read a file, write it to a string, read it again.  Test for same. 
    
    Triple t1 = SSE.parseTriple("(<foo:x> <foo:p> 123)") ;
    
    @Test public void rdfjson_01()
    {
        Model m = ModelFactory.createDefaultModel() ;
        m.getGraph().add(t1) ;
        ByteArrayOutputStream out = new ByteArrayOutputStream() ;
        RiotWriter.writeRDFJSON(out, m.getGraph()) ;
        
        Model m2 = ModelFactory.createDefaultModel() ;
        InputStream in = new ByteArrayInputStream(out.toByteArray()) ;
        RiotLoader.read(in, m2.getGraph(), Lang.RDFJSON, null) ;
        
        assertTrue (m.isIsomorphicWith(m2)) ;
    }

}
