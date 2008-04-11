/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.file;

import static lib.FileOps.clearDirectory;
import org.junit.Test;
import test.BaseTest;

import com.hp.hpl.jena.tdb.pgraph.NodeId;

public class TestObjectFile extends BaseTest
{
    @Test public void object_file_1()
    {
        String dir = "testing/tmp" ;
        clearDirectory(dir) ;
        Location loc = new Location(dir) ;
        ObjectFile f = FileFactory.createObjectFileDisk(loc.getPath("xyz", "node")) ;
        String x1 = "孫子兵法" ;
        String x2 = "abbbbbbc" ;
        
        NodeId id1 = f.write(x1) ;
        NodeId id2 = f.write(x2) ;
        
        assertNotEquals("Node Ids", id1, id2) ;
        
        String y2 = f.read(id2) ;
        assertEquals("x2",x2, y2) ;

        String y1 = f.read(id1) ;
        assertEquals("x1", x1, y1) ;

        
        String y1a = f.read(NodeId.create(0)) ;
        assertEquals("x1a", x1, y1) ;
    }
    
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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