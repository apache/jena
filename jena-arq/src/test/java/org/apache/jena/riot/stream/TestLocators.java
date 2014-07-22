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

package org.apache.jena.riot.stream;

import java.io.File ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.web.TypedInputStream ;
import org.apache.jena.riot.WebContent ;
import org.apache.jena.riot.system.stream.LocatorFile ;
import org.junit.Test ;

public class TestLocators extends BaseTest 
{
    public static final String testingDir = "testing/RIOT/Files/" ;
    
    @Test public void locatorFile_01()
    {
        LocatorFile loc = new LocatorFile() ;
        assertTrue(loc.exists("pom.xml")) ;
        assertTrue(loc.exists(testingDir+"data.ttl")) ;
        assertFalse(loc.exists("IDoNotExist")) ;
    }
    
    @Test public void locatorFile_02()
    {
        LocatorFile loc = new LocatorFile(".") ;
        assertTrue(loc.exists("pom.xml")) ;
        assertTrue(loc.exists(testingDir+"data.ttl")) ;
        assertFalse(loc.exists("IDoNotExist")) ;
    }

    @Test public void locatorFile_03()
    {
        String dir = new File(".").getAbsolutePath() ;
        LocatorFile loc = new LocatorFile(dir) ;
        assertTrue(loc.exists("pom.xml")) ;
        assertFalse(loc.exists("IDoNotExist")) ;
    }
    
    @Test public void locatorFile_04()
    {
        String dir = new File("src").getAbsolutePath() ;
        LocatorFile loc = new LocatorFile(dir) ;
        
        assertFalse(loc.exists("pom.xml")) ;
        assertTrue(loc.exists("main")) ;
        assertFalse(loc.exists(testingDir+"data.ttl")) ;
        assertTrue(loc.exists("../pom.xml")) ;
        assertFalse(loc.exists("/../"+testingDir+"data.ttl")) ;
    }
    
    @Test public void locatorFile_05()
    {
        LocatorFile loc = new LocatorFile() ;
        TypedInputStream ts = loc.open(testingDir+"data.ttl") ;
        assertTrue("Not equal: "+WebContent.contentTypeTurtle+" != "+ts.getMediaType(), 
                   WebContent.contentTypeTurtle.equalsIgnoreCase(ts.getContentType())) ;
    }

    // TypedStream
    
    @Test public void locatorURL_01() {}

    @Test public void locatorZip_01() {}

    @Test public void locatorClassloader_01() {}
}
