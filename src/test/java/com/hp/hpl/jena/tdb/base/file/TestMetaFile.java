/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.file;

import java.io.File;


import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.openjena.atlas.junit.BaseTest ;

import com.hp.hpl.jena.tdb.ConfigTest;
import com.hp.hpl.jena.tdb.sys.Names ;

public class TestMetaFile extends BaseTest
{
    static String testfile = ConfigTest.getTestingDir()+"/file" ;
    static String testfileMeta = ConfigTest.getTestingDir()+"/file."+Names.extMeta ;
    
    @Before public void before()
    {
        File f = new File(testfileMeta) ;
        f.delete() ;
    }
    
    @Test public void meta1()
    {
        clear() ;
        MetaFile f = new MetaFile("META", testfile) ;
        assertFalse(new File(testfileMeta).exists()) ;
        f.setProperty("key", "value") ;
        f.flush() ;
        assertTrue(new File(f.getFilename()).exists()) ;
    }
    
    @Test public void meta2()
    {
        clear() ;
        MetaFile f = new MetaFile("META", testfile) ;
        f.setProperty("test.value1", "1") ;
        f.flush();
        MetaFile f2 = new MetaFile("META", testfile) ;
        assertEquals("1", f2.getProperty("test.value1")) ;
        assertNull(f2.getProperty("test.value.other")) ;
    }

    // Test MetaBase
    
    @AfterClass public static void afterClass()
    { clear() ; }
    
    private static void clear()
    {
        File f = new File(testfileMeta) ;
        f.delete() ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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