/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package fm2.atlas;

import java.io.File ;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.web.TypedStream ;
import org.openjena.riot.WebContent ;

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
        assertTrue(loc.exists("org")) ;
        assertFalse(loc.exists(testingDir+"data.ttl")) ;
        assertTrue(loc.exists("../pom.xml")) ;
        assertFalse(loc.exists("/../"+testingDir+"data.ttl")) ;
    }
    
    @Test public void locatorFile_05()
    {
        LocatorFile loc = new LocatorFile() ;
        TypedStream ts = loc.open(testingDir+"data.ttl") ;
        assertTrue("Not equal: "+WebContent.contentTypeTurtle1+" != "+ts.getContentType().contentType, WebContent.contentTypeTurtle2.equalsIgnoreCase(ts.getContentType().contentType)) ;
    }

    // TypedStream
    
    @Test public void locatorURL_01() {}

    @Test public void locatorZip_01() {}

    @Test public void locatorClassloader_01() {}
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