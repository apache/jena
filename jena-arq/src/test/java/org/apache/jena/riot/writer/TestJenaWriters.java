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

import java.io.ByteArrayOutputStream ;
import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.lib.Bytes ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RIOT ;
import org.apache.jena.riot.system.IO_JenaWriters ;
import org.junit.Test ;
import org.junit.runner.RunWith ;
import org.junit.runners.Parameterized ;
import org.junit.runners.Parameterized.Parameters ;

import com.hp.hpl.jena.rdf.model.Model ;

@RunWith(Parameterized.class)
public class TestJenaWriters extends AbstractWriterTest
{
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() {
        RIOT.init();
        List<Object[]> x = new ArrayList<>() ;
        for ( String wname : IO_JenaWriters.getJenaWriterNames() )
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

//    @Test public void jwrite_01() { test("writer-rt-00.ttl") ; }
//    @Test public void jwrite_01() { test("writer-rt-01.ttl") ; }
//    @Test public void jwrite_02() { test("writer-rt-02.ttl") ; }
//    @Test public void jwrite_03() { test("writer-rt-03.ttl") ; }
//    @Test public void jwrite_04() { test("writer-rt-04.ttl") ; }
//    @Test public void jwrite_05() { test("writer-rt-05.ttl") ; }
//    @Test public void jwrite_06() { test("writer-rt-06.ttl") ; }
//    @Test public void jwrite_07() { test("writer-rt-07.ttl") ; }
//    @Test public void jwrite_08() { test("writer-rt-08.ttl") ; }
//    @Test public void jwrite_09() { test("writer-rt-09.ttl") ; }
//    @Test public void jwrite_10() { test("writer-rt-10.ttl") ; }
//    @Test public void jwrite_11() { test("writer-rt-11.ttl") ; }
//    @Test public void jwrite_12() { test("writer-rt-12.ttl") ; }
//    @Test public void jwrite_13() { test("writer-rt-13.ttl") ; }
//    @Test public void jwrite_14() { test("writer-rt-14.ttl") ; }
//    @Test public void jwrite_15() { test("writer-rt-15.ttl") ; }
//    @Test public void jwrite_16() { test("writer-rt-16.ttl") ; }
    
    private void test(String filename) {
        Model m = readModel(filename) ;
        ByteArrayOutputStream out2 = new ByteArrayOutputStream() ;
        RDFDataMgr.write(out2, m, IO_JenaWriters.getFormatForJenaWriter(jenaFormatName)) ;

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
