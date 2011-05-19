/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.block;

import org.junit.After ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.openjena.atlas.lib.FileOps ;
import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.tdb.ConfigTest ;
import com.hp.hpl.jena.tdb.base.file.FileAccess ;
import com.hp.hpl.jena.tdb.base.file.FileAccessMapped ;

public class TestBlockMgrMapped extends AbstractTestBlockMgr
{
    static boolean logging = false ;
    
    static { if ( logging ) Log.setLog4j() ; }
    
    static final String filename = ConfigTest.getTestingDir()+"/block-mgr" ;
    
    // Windows is iffy about deleting memory mapped files.
    
    @After public void after1()     { clearBlockMgr() ; }
    
    private void clearBlockMgr()
    {
        if ( blockMgr != null )
        {
            blockMgr.close() ;
            FileOps.deleteSilent(filename) ;
            blockMgr = null ;
        }
    }
    
    @BeforeClass static public void remove1() { FileOps.deleteSilent(filename) ; }
    @AfterClass  static public void remove2() { FileOps.deleteSilent(filename) ; }
    
    @Override
    protected BlockMgr make()
    { 
        clearBlockMgr() ;
        FileAccess file = new FileAccessMapped(filename, BlkSize) ;
        return new BlockMgrFileAccess(file, BlkSize) ;
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