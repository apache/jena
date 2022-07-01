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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File ;
import java.nio.file.Path;

import org.junit.Test ;

public class TestFilenameProcessing
{
    @Test public void encode_1() { encodeComponent("abc", "abc") ; }
    @Test public void encode_2() { encodeComponent("", "") ; }
    @Test public void encode_3() { encodeComponent(":/", "%3A%2F") ; }

    // ---- Main tests.
    // Portablility

    static boolean isWindows = File.separatorChar != '/' ;

    private static String cwd = Path.of(".").toAbsolutePath().normalize().toString() ;
    // Sort out cwd, not using the IRILib code.
    //   Must start "/", must not end "/"
    //   Must be /-style, not \
    //   Must %-encode URI-metacharacters
    static {
        if ( isWindows ) {
            // Canonical
            cwd = cwd.replace(File.separatorChar, '/') ;
            // Drive letters.
            if ( ! cwd.startsWith("/" ) )
                cwd = "/" + cwd ;
        }
        cwd = IRILib.encodeUriPath(cwd) ;
    }

    @Test
    public void fileIRI_1() {
        String uri = testFileIRI("D.ttl") ;
        assertTrue(uri.endsWith("D.ttl")) ;
    }

    @Test
    public void fileIRI_2() {
        String uri = testFileIRI("file:/D.ttl") ;
        assertTrue(uri.endsWith("D.ttl")) ;
    }

    @Test
    public void fileIRI_3() {
        String fn = "file://some.host/D.ttl" ;
        String uri1 = IRILib.filenameToIRI(fn) ;
        assertEquals(fn, uri1);
        String uri2 = IRILib.filenameToIRI(uri1) ;
        assertEquals(uri1, uri2) ;
    }

    @Test
    public void fileIRI_4() {
        String iri = testFileIRI("file:///D.ttl") ;
        // Even on windows, this is used as-is so no drive letter.
        assertEquals("file:///D.ttl", iri) ;
    }

    private static String testFileIRI(String fn) {
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
        if ( ! isWindows )
            // Windows inserts a drive letter
            filenameToIRI("/abc", "file:///abc") ;
    }

    @Test
    public void fileURL_3() {
        if ( isWindows )
            filenameToIRI("C:/Program File/App File", "file:///C:/Program%20File/App%20File") ;
        else
            filenameToIRI("/Program File/App File", "file:///Program%20File/App%20File") ;
    }

    @Test
    public void fileURL_4() {
        if ( isWindows )
            filenameToIRI("C:/Program File/App Dir/", "file:///C:/Program%20File/App%20Dir/") ;
        else
            filenameToIRI("/Program File/App Dir/", "file:///Program%20File/App%20Dir/") ;
    }

    @Test
    public void fileURL_5() {
        if ( isWindows )
            filenameToIRI("C:\\Windows\\Path", "file:///C:/Windows/Path") ;
        else
            filenameToIRI("C:\\Windows\\Path", "file://" + cwd + "/C:%5CWindows%5CPath") ;
    }

    @Test
    public void fileURL_6() {
        filenameToIRI("~user", "file://" + cwd + "/~user") ;
    }

    @Test
    public void fileURL_7() {
        filenameToIRI(".", "file://" + cwd) ;
    }

    @Test
    public void fileURL_10() {
        filenameToIRI("file:abc", "file://" + cwd + "/abc") ;
    }
    @Test public void fileURL_11() {
        if ( ! isWindows )
            // Windows inserts a drive letter
            filenameToIRI("file:/abc", "file:///abc" ) ;
    }

    @Test
    public void fileURL_12() {
        filenameToIRI("file:", "file://" + cwd) ;
    }

    @Test
    public void fileURL_13() {
        filenameToIRI("file:.", "file://" + cwd + "") ;
    }

    @Test public void fileURL_14() {
        String x = cwd.replaceAll("/[^/]*$", "") ;
        filenameToIRI("file:..", "file://"+x ) ;
    }

    // Windows + URL.toString().
    // Should be stable on all systems.
    @Test public void fileURL_15() {
        filenameToIRI("file:/C:/path/file", "file:///C:/path/file") ;
    }

    private static void encodeComponent(String string, String result) {
        String r = IRILib.encodeUriComponent(string) ;
        assertEquals(result, r) ;
    }

    private static void filenameToIRI(String string, String result) {
        String r = IRILib.filenameToIRI(string) ;
        assertEquals(result, r) ;
    }
}
