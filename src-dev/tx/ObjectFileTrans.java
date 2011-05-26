/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package tx;

import java.nio.ByteBuffer ;
import java.util.Iterator ;

import org.openjena.atlas.lib.Pair ;
import tx.journal.Journal ;

import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile ;

public class ObjectFileTrans implements ObjectFile
{
    private final Journal journal ;
    private final ObjectFile other ;
    private long alloc ;

    // Objects aren't huge - a block per object and the file ref overhead is a bit much.
    
    public ObjectFileTrans(Journal journal, ObjectFile other)
    {
        this.journal = journal ;
        this.other = other ;
        this.alloc = other.length() ;
    }
    
    @Override
    public void sync()
    {}

    @Override
    public void close()
    {}

    @Override
    public Block allocWrite(int maxBytes)
    {
        ByteBuffer bb = ByteBuffer.allocate(maxBytes) ;
        // Allocation in ObjectFile.other?
        return null ;
    }

    @Override
    public void completeWrite(Block buffer)
    {
    }

    @Override
    public long write(ByteBuffer buffer)
    {
        return 0 ;
    }

    @Override
    public ByteBuffer read(long id)
    {
        return null ;
    }

    @Override
    public long length()
    {
        return 0 ;
    }

    @Override
    public Iterator<Pair<Long, ByteBuffer>> all()
    {
        return null ;
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