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

package org.apache.jena.atlas.lib;

import java.io.File ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.base.Sys ;
import org.junit.Test ;

public class TestFilenameProcessing extends BaseTest
{
    // See also TestFunction2 for tests for encode_for_uri
    @Test public void encode_1() { encodeComponent("abc", "abc") ; }
    @Test public void encode_2() { encodeComponent("", "") ; }
    @Test public void encode_3() { encodeComponent(":/", "%3A%2F") ; }

    // ---- Main tests.
    // Portablility
    
    private static String cwd = new File(".").getAbsolutePath() ;
    static {
        if ( Sys.isWindows )
            // Canonical
            cwd = cwd.replace('\\', '/') ;
    }
    // Without trailing slash.
    static { cwd = cwd.substring(0, cwd.length()-2) ; }
    
    @Test public void fileIRI_1()
    {
        String uri = testFileIRI("D.ttl") ; 
        assertTrue(uri.endsWith("D.ttl")) ;
    }
    
    @Test public void fileIRI_2()
    {
        String uri = testFileIRI("file:/D.ttl") ; 
        assertTrue(uri.endsWith("D.ttl")) ;
    }
    
    @Test public void fileIRI_3()
    {
        String uri = testFileIRI("file://D.ttl") ; 
        assertTrue(uri.endsWith("D.ttl")) ;
    }

    @Test public void fileIRI_4()
    {
        String iri = testFileIRI("file:///D.ttl") ;
        // Even on windows, this is used as-is so no drive letter. 
        assertEquals("file:///D.ttl", iri) ;
    }
    
    private static String testFileIRI(String fn)
    {
        String uri1 = IRILib.filenameToIRI(fn) ;
        assertTrue(uri1.startsWith("file:///")) ;
        String uri2 = IRILib.filenameToIRI(uri1) ;
        assertEquals(uri1, uri2) ;
        return uri1 ;
    }

    
    @Test
    public void fileURL_1() {
        assertNotEquals(cwd, "") ;
        assertNotNull(cwd) ;
        filenameToIRI("abc", "file://" + cwd + "/abc") ;
    }

    @Test
    public void fileURL_2() {
        filenameToIRI("/abc", "file:///abc") ;
    }
    
    static boolean isWindows = File.pathSeparator.equals(";") ;

    
    @Test public void fileURL_3()
    { 
        if ( isWindows )
            filenameToIRI("c:/Program File/App File", "file:///c:/Program%20File/App%20File") ; 
        else
            filenameToIRI("/Program File/App File", "file:///Program%20File/App%20File") ;
    }

    @Test public void fileURL_4()
    { 
        if ( isWindows )
            filenameToIRI("c:/Program File/App Dir/", "file:///c:/Program%20File/App%20Dir/") ; 
        else
            filenameToIRI("/Program File/App Dir/", "file:///Program%20File/App%20Dir/") ;
    }
    
    @Test public void fileURL_5()
    {
        if ( isWindows )
            filenameToIRI("c:\\Windows\\Path", "file:///c:/Windows/Path") ;
        else
            filenameToIRI("c:\\Windows\\Path", "file://"+cwd+"/c:%5CWindows%5CPath") ;
        
    }
    
    @Test public void fileURL_6() { filenameToIRI("~user", "file://"+cwd+"/~user") ; }
    @Test public void fileURL_7() { filenameToIRI(".", "file://"+cwd) ; }
    
    @Test public void fileURL_10() { filenameToIRI("file:abc", "file://"+cwd+"/abc" ) ; }
    @Test public void fileURL_11() { filenameToIRI("file:/abc", "file:///abc" ) ; }
    @Test public void fileURL_12() { filenameToIRI("file:", "file://"+cwd ) ; }
    
    @Test public void fileURL_13() { filenameToIRI("file:.", "file://"+cwd+"" ) ; }
    @Test public void fileURL_14() {
        String x = cwd ;
        if ( isWindows )
            x = x.replace('\\', '/') ; 
        x = cwd.replaceAll("/[^/]*$", "") ;
        filenameToIRI("file:..", "file://"+x ) ;
    }

    
    private static void encodeComponent(String string, String result)
    {
        String r = IRILib.encodeUriComponent(string) ;
        assertEquals(result, r) ;
    }
    
    private static void filenameToIRI(String string, String result)
    {
        String r = IRILib.filenameToIRI(string) ;
        assertEquals(result, r) ;
    }
    
}
