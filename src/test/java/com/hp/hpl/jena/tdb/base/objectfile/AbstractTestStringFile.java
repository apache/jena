/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.objectfile;

import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;

import com.hp.hpl.jena.tdb.base.objectfile.StringFile ;

public abstract class AbstractTestStringFile extends BaseTest
{
    StringFile f = null ;
    
    @Before public void setup() { f = createStringFile() ; }
    @After public void teardown() { removeStringFile(f) ; }
    
    protected abstract StringFile createStringFile() ;
    protected abstract void removeStringFile(StringFile f) ;

    @Test public void object_file_01()
    {
        String x1 = "abc" ;
        long id1 = f.write(x1) ;
        test(id1, x1) ;
        test(0, x1) ;
    }

    @Test public void object_file_02()
    {
        String x1 = "" ;
        
        long id1 = f.write(x1) ;
        test(id1, x1) ;
        test(0, x1) ;
    }

    @Test public void object_file_03()
    {
        String x1 = "abbbbbbc" ;
        String x2 = "deeeef" ;
        
        long id1 = f.write(x1) ;
        long id2 = f.write(x2) ;
        
        assertNotEquals("Node Ids", id1, id2) ;
        
        test(id1, x1) ;
        test(id2, x2) ;
        test(0, x1) ;
    }
    
    @Test public void object_file_04()
    {
        String x1 = "abbbbbbc" ;
        String x2 = "deeeef" ;
        
        long id1 = f.write(x1) ;
        long id2 = f.write(x2) ;
        // Read in opposite order
        test(id2, x2) ;
        test(id1, x1) ;
    }

    @Test public void object_file_05()
    {
        String x = "孫子兵法" ;
        
        long id = f.write(x) ;
        test(0, x) ;
    }
    
    @Test public void object_file_06()
    {
        String x1 = "abbbbbbc" ;
        String x2 = "孫子兵法" ;
        
        long id1 = f.write(x1) ;
        f.flush() ;
        long id2 = f.write(x2) ;
        // No flush.
        
        assertNotEquals("Node Ids", id1, id2) ;
        
        String z = f.read(id2) ;
        
        test(id2, x2) ;
        test(id1, x1) ;
        test(0, x1) ;
    }
    
    @Test public void object_file_07()
    {
        String x1 = "abbbbbbc" ;
        String x2 = "孫子兵法" ;
        
        long id1 = f.write(x1) ;
        long id2 = f.write(x2) ;
        f.flush() ;
        
        assertNotEquals("Node Ids", id1, id2) ;
        
        test(id2, x2) ;
        test(id1, x1) ;
        test(0, x1) ;
    }

    @Test public void object_file_08()
    {
        String x1 = "abbbbbbc" ;
        String x2 = "孫子兵法" ;
        
        long id1 = f.write(x1) ;
        f.flush() ;
        long id2 = f.write(x2) ;
        f.flush() ;
        
        assertNotEquals("Node Ids", id1, id2) ;
        
        test(id2, x2) ;
        test(id1, x1) ;
        test(0, x1) ;
    }

    private void test(long id, String x)
    {
        String y = f.read(id) ;
        assertEquals(x, y) ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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