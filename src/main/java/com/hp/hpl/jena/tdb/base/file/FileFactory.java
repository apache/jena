/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.file;

import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFileMem ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFileStorage ;
import com.hp.hpl.jena.tdb.base.objectfile.StringFile ;

public class FileFactory
{
    public static StringFile createStringFileDisk(String filename)
    { return new StringFile(createObjectFileDisk(filename)) ; }

    public static StringFile createStringFileMem()
    { return new StringFile(createObjectFileMem()) ; }
    
    public static ObjectFile createObjectFileDisk(String filename)
    {
        BufferChannel file = new BufferChannelFile(filename) ; 
        return new ObjectFileStorage(file) ;
    }

    public static ObjectFile createObjectFileMem()
    { 
        if ( false )
            // Older code.
            return new ObjectFileMem() ;
        else
        {
            // Newer way.
            BufferChannel file = BufferChannelMem.create("mem") ; 
            return new ObjectFileStorage(file) ;
        }
    }
    
    public static PlainFile createPlainFileDisk(String filename)
    { return new PlainFilePersistent(filename) ; }
    
    public static PlainFile createPlainFileMem()
    { return new PlainFileMem() ; }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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