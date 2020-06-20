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

package org.apache.jena.riot.writer;

import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayOutputStream ;
import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.lib.Bytes ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFWriterRegistry ;
import org.junit.Test ;
import org.junit.runner.RunWith ;
import org.junit.runners.Parameterized ;
import org.junit.runners.Parameterized.Parameters ;

@RunWith(Parameterized.class)
public class TestJenaWriters extends AbstractWriterTest
{
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() {
        List<Object[]> x = new ArrayList<>() ;
        for ( String wname :  RDFWriterRegistry.getJenaWriterNames())
            x.add(new Object[]{wname}) ;
        return x ; 
    }

    private String jenaFormatName ;
    
    public TestJenaWriters(String jenaFormatName)
    {
        this.jenaFormatName = jenaFormatName ;
    }
    
    @Test public void jwrite_00() { test("writer-jena.ttl") ; }
    
    // More test don't really add anything.
    // We are not testing the correctness of the writers,
    // only the wiring up of the writers to model.write.

    private void test(String filename) {
        Model m = readModel(filename) ;
        ByteArrayOutputStream out2 = new ByteArrayOutputStream() ;
        RDFDataMgr.write(out2, m, RDFWriterRegistry.getFormatForJenaWriter(jenaFormatName)) ;

        ByteArrayOutputStream out1 = new ByteArrayOutputStream() ;
        m.write(out1, jenaFormatName) ;
        
        try {
            assertArrayEquals("Format: "+jenaFormatName, out2.toByteArray(), out1.toByteArray()) ;
        } catch (AssertionError ex) {
            String s1 = Bytes.bytes2string(out1.toByteArray()) ;
            String s2 = Bytes.bytes2string(out2.toByteArray()) ;
            System.out.print(s1) ;
            System.out.print(s2) ;
            throw ex ;
        }
    }
}
