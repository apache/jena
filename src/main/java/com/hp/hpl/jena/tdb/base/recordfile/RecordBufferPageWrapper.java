/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.recordfile;


import java.nio.ByteBuffer;

import org.openjena.atlas.io.IndentedWriter ;


import com.hp.hpl.jena.tdb.base.buffer.RecordBuffer;
import com.hp.hpl.jena.tdb.base.page.Page;

public class RecordBufferPageWrapper implements Page
{
    protected final RecordBufferPage rBuffPage ;
    protected final RecordBuffer rBuff ;

    public RecordBufferPageWrapper(RecordBufferPage page)
    { 
        this.rBuffPage = page ; 
        this.rBuff = page.getRecordBuffer() ;
    }

    public final RecordBufferPage getRecordBufferPage()
    { return rBuffPage ; } 

    public final boolean isFull()       { return rBuff.isFull() ; } 
    public final boolean isEmpty()      { return rBuff.isEmpty() ; } 
    
    //@Override
    public void output(IndentedWriter out)  { out.print(toString()) ; }

    //@Override
    public final int getMaxSize()             { return rBuff.maxSize() ; }
    
    //@Override
    public final int getCount()             { return rBuff.size() ; }
 
    //@Override
    public final void setCount(int count)   { rBuff.setSize(count) ; }
    
    //@Override
    public ByteBuffer getBackingByteBuffer()   { return rBuffPage.getBackingByteBuffer() ; }

    //@Override
    public int getId()                  { return rBuffPage.getId() ; } 

    //@Override
    public void setId(int id)           { rBuffPage.setId(id) ; }
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