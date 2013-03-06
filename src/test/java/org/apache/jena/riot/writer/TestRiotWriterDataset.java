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

import java.io.ByteArrayInputStream ;
import java.io.ByteArrayOutputStream ;
import java.util.Arrays ;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.riot.* ;
import org.junit.Test ;
import org.junit.runner.RunWith ;
import org.junit.runners.Parameterized ;
import org.junit.runners.Parameterized.Parameters ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;

@RunWith(Parameterized.class)
public class TestRiotWriterDataset extends AbstractWriterTest
{
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { RDFFormat.RDFNULL }
            , { RDFFormat.TRIG }
            , { RDFFormat.TRIG_PRETTY }
            , { RDFFormat.TRIG_BLOCKS }
            , { RDFFormat.TRIG_FLAT }
            , { RDFFormat.NQUADS}
        }) ; 
    }

    private RDFFormat format ;
    
    public TestRiotWriterDataset(RDFFormat format)
    {
        this.format = format ;
    }
    
    @Test public void writer00() { test("writer-rt-20.trig") ; }
    @Test public void writer01() { test("writer-rt-21.trig") ; }
    @Test public void writer02() { test("writer-rt-22.trig") ; }
    @Test public void writer03() { test("writer-rt-23.trig") ; }
    
    private void test(String filename)
    {
        String displayname = filename.substring(0, filename.lastIndexOf('.')) ;
        Dataset ds = readDataset(filename) ; 
        Lang lang = format.getLang() ;

        WriterDatasetRIOT rs = RDFWriterRegistry.getWriterDatasetFactory(format).create(format) ;
        assertEquals(lang, rs.getLang()) ;

        ByteArrayOutputStream out = new ByteArrayOutputStream() ;
        RDFDataMgr.write(out, ds, format) ;
        
        if ( lang == Lang.RDFNULL )
            return ;
        
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray()) ;
        String s = StrUtils.fromUTF8bytes(out.toByteArray()) ;
        Dataset ds2 = DatasetFactory.createMem() ;
        try {
            RDFDataMgr.read(ds2, in, lang) ;
        } catch (RiotException ex)
        {
            System.out.println(displayname+" : "+format) ;
            System.out.println(s) ;
            throw ex ;
        }
    }
}

