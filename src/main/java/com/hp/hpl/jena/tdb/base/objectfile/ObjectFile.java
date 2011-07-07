/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.objectfile;

import java.nio.ByteBuffer ;
import java.util.Iterator ;

import com.hp.hpl.jena.tdb.base.block.Block ;

import org.openjena.atlas.lib.Closeable ;
import org.openjena.atlas.lib.Pair ;
import org.openjena.atlas.lib.Sync ;

/** 
 * An ObjectFile is an append-read file, that is you can append data
 * to the stream or read any block.
 */

public interface ObjectFile extends Sync, Closeable
{
    public static final String type = "object" ;
    
    /** A label to identify this ObjectFile - liek toString, except it must be implemented */ 
    public String getLabel() ;
    
    /** Allocate space for a write - pass this buffer to completeWrite */ 
    public Block allocWrite(int bytesSpace) ;
    
    /** Announce that a write is complete (buffer must come from allocWrite) - return the accessor number */
    public void completeWrite(Block buffer) ;

    /** Write out the buffer - return the accessor number */ 
    public long write(ByteBuffer buffer) ;

    /** Read a buffer at the accessor number. */
    public ByteBuffer read(long id) ;
    
    /** Length, in units used by read/write for ids */
    public long length() ;
    
    /** Reset the "append" point; may only be moved earlier.
     * The new position must correspond to a position returned by
     * {@link #write(ByteBuffer)} or an id in a {@link Block Block} from {@link #completeWrite(Block)}
     */
    public void reposition(long id) ;
    
    /** 
     */
    public void truncate(long size) ;

    /** All the bytebuffers - debugging aid */
    public Iterator<Pair<Long, ByteBuffer>> all() ;
    
//    /** return a useful short display string */ 
//    public String getLabel() ;
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