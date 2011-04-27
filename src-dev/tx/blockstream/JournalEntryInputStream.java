/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package tx.blockstream;

import java.io.InputStream ;
import java.nio.ByteBuffer ;

import org.openjena.atlas.io.IO ;
import tx.base.FileRef ;

public class JournalEntryInputStream implements JournalEntryInput
{
    private final InputStream in ;
    public JournalEntryInputStream(InputStream in)
    {
        this.in = in ;
        
    }
    
    public JournalEntry read()
    {
        int type = IOBytes.readInt(in) ;
        if ( type == -1 )
            return null ;
        
        FileRef fRef = readFileRef() ;
        if ( fRef == null )
            return null ;
        
        byte b[] = IOBytes.readBytes(in) ;
        if ( b == null )
            return null ;
        ByteBuffer bb = ByteBuffer.wrap(b) ;
        return new JournalEntry(type, fRef, bb) ;
    }
    
    public void close()
    {
        IO.close(in) ;
    }

    private FileRef readFileRef()
    {
        String fn = IOBytes.readStr(in) ;
        if ( fn == null )
            return null ;
        int blockId = IOBytes.readInt(in) ;
        if ( blockId == -1 )
            return null ;
        return new FileRef(fn, blockId) ;
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