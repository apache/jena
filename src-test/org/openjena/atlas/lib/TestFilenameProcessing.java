/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib;

import java.io.File ;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.IRILib ;

public class TestFilenameProcessing extends BaseTest
{
    // See also TestFunction2 for tests for encode_for_uri
    @Test public void encode_1() { encodeComponent("abc", "abc") ; }
    @Test public void encode_2() { encodeComponent("", "") ; }
    @Test public void encode_3() { encodeComponent(":/", "%3A%2F") ; }

    // ---- Main tests.
    // Portablility
    
    private static String cwd = new File(".").getAbsolutePath() ;
    // Without trailing slash.
    static { cwd = cwd.substring(0, cwd.length()-2) ; }
    
    @Test public void fileURL_1() { assertNotEquals(cwd, "") ; assertNotNull(cwd) ; filenameToIRI("abc", "file://"+cwd+"/abc" ) ; }
    
    @Test public void fileURL_2() { filenameToIRI("/abc", "file:///abc" ) ; }
    
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

/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */