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

package org.apache.jena.riot.stream;

import java.io.File ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.web.TypedInputStream ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RiotNotFoundException ;
import org.apache.jena.riot.system.stream.LocatorFile ;
import org.apache.jena.riot.system.stream.LocatorHTTP ;
import org.apache.jena.riot.system.stream.StreamManager ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.util.Context ;

public class TestStreamManager extends BaseTest
{
    private static final String directory = "testing/RIOT/StreamManager" ;
    private static final String absDirectory = new File(directory).getAbsolutePath() ;
    
    private static StreamManager streamMgrDir ;
    private static StreamManager streamMgrHere ;
    private static StreamManager streamMgrNull ;
    private static StreamManager streamMgrStd ;
    
    @BeforeClass static public void beforeClass()
    { 
        streamMgrStd = StreamManager.get() ;
        streamMgrDir = new StreamManager() ;
        // Not current directory.
        streamMgrDir.addLocator(new LocatorFile(directory)) ;
        streamMgrDir.addLocator(new LocatorHTTP()) ;
        
        streamMgrHere = new StreamManager() ;
        // Not current directory.
        streamMgrHere.addLocator(new LocatorFile()) ;
        streamMgrHere.addLocator(new LocatorHTTP()) ;
        
        streamMgrNull = new StreamManager() ;
    }
    
    @AfterClass static public void afterClass()
    { 
        StreamManager.setGlobal(streamMgrStd) ;
    }
    
    private static Context context(StreamManager streamMgr)
    {
        Context context = new Context() ;
        context.put(RDFDataMgr.streamManagerSymbol, streamMgr) ;
        return context ;
    }
    
    @Test public void fm_open_01()  { open(streamMgrNull, directory+"/D.ttl", context(streamMgrHere)) ; }
    @Test public void fm_open_02()  { open(streamMgrHere, directory+"/D.ttl", null) ; }
    
    @Test public void fm_open_03()  { open(streamMgrNull,  "D.ttl", context(streamMgrDir)) ; }
    @Test public void fm_open_04()  { open(streamMgrDir, "D.ttl", null) ; }

    @Test public void fm_open_05()  { open(streamMgrHere, "file:"+directory+"/D.ttl", context(streamMgrHere)) ; }
    @Test public void fm_open_06()  { open(streamMgrHere, "file:"+directory+"/D.ttl", null) ; }

    @Test public void fm_open_07()  { open(streamMgrHere, "file:D.ttl", context(streamMgrDir)) ; }
    @Test public void fm_open_08()  { open(streamMgrDir, "file:D.ttl", null) ; }
    
    @Test public void fm_open_09()  { open(streamMgrHere, absDirectory+"/D.ttl", null) ; }
    @Test public void fm_open_10()  { open(streamMgrDir,  absDirectory+"/D.ttl", null) ; }
    @Test public void fm_open_11()  { open(streamMgrDir,  "file://"+absDirectory+"/D.ttl", null) ; }
    @Test public void fm_open_12()  { open(streamMgrHere, "file://"+absDirectory+"/D.ttl", null) ; }
    
    @Test (expected=RiotNotFoundException.class)
    public void fm_open_20()        { open(null, "nosuchfile", context(streamMgrDir)) ; }
    @Test (expected=RiotNotFoundException.class)
    public void fm_open_21()        { open(streamMgrHere, "nosuchfile", null) ; }
    
    @Test public void fm_read_01()  { read("D.nt") ; }
    @Test public void fm_read_02()  { read("D.ttl") ; }
    @Test public void fm_read_03()  { read("D.rdf") ; }
    @Test public void fm_read_04()  { read("D.json") ; }

    @Test public void fm_read_11()  { read("file:D.nt") ; }
    @Test public void fm_read_12()  { read("file:D.ttl") ; }
    @Test public void fm_read_13()  { read("file:D.rdf") ; }
    @Test public void fm_read_14()  { read("file:D.json") ; }
    
    // TriG
    // NQuads
    
    private static void open(StreamManager streamMgr, String dataName, Context context)
    {
        StreamManager.setGlobal(streamMgr) ;
        try {
            TypedInputStream in = ( context != null ) 
                ? RDFDataMgr.open(dataName, context)
                : RDFDataMgr.open(dataName) ;
            assertNotNull(in) ;
            IO.close(in) ;
        } finally {
            StreamManager.setGlobal(streamMgrStd) ;
        }
    }
    
    private static void read(String dataName)
    {
        try {
            StreamManager.setGlobal(streamMgrDir) ;
            Model m = ModelFactory.createDefaultModel() ;
            RDFDataMgr.read(m, dataName) ;
            assertNotEquals("Read "+dataName, 0, m.size()) ;
        } finally {
            StreamManager.setGlobal(streamMgrStd) ;
        }
    }
}

