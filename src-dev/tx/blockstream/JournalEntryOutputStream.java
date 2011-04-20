/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package tx.blockstream;

import java.io.FileOutputStream ;
import java.io.IOException ;
import java.io.OutputStream ;

import org.openjena.atlas.io.IO ;
import tx.base.FileRef ;

public class JournalEntryOutputStream implements JournalEntryOutput
{
    private final OutputStream out ;

    public JournalEntryOutputStream(OutputStream out)
    {
        this.out = out ;
    }
    
    public void write(JournalEntry entry)
    {
        IOBytes.writeInt(out, entry.getType()) ;
        writeFileRef(entry.getFileRef()) ;
        IOBytes.writeBytes(out, entry.getByteBuffer().array())  ;
    }

    private void writeFileRef(FileRef fileRef)
    {
        IOBytes.writeStr(out, fileRef.getFilename()) ;
        IOBytes.writeInt(out, fileRef.getBlockId()) ;
    }

    public void close()
    { IO.close(out) ; }

    public void sync()
    { 
        // Need our own "FileOutput" which is buffered and connects to sync() 
        System.err.println("Need to find the channel of FD and fsync it : ") ;
        if ( out instanceof FileOutputStream )
        {
            // And if it's a buffered ??
            try { ((FileOutputStream)out).getFD().sync() ; } catch(IOException ex) { IO.exception(ex) ; } 
        }
        
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