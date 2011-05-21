/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.file;

import java.nio.ByteBuffer ;

import org.openjena.atlas.lib.Closeable ;
import org.openjena.atlas.lib.Sync ;


/** Interface to storage : a simplified version of FileChannel.
 *  This also enables use to implement memory-bcked versions.
 *  @see FileAccess
 */
public interface Channel extends Sync, Closeable
{
    // This is a simple, low level "file = array of bytes" interface"
    // This interface does not support slicing - so it's not suitable for memory mapped I/O
    // but it is suitable for compression.
    
    /** return the position */
    public long position() ;
    
    /** set the position */
    public void position(long pos) ;

    /** Read into a ByteBuffer. Returns the number of bytes read.
     */
    public int read(ByteBuffer buffer) ;
    
    /** Read into a ByteBuffer, starting at position loc. Return the number of bytes read.
     * loc must be within the file.
     */
    public int read(ByteBuffer buffer, long loc) ;

    /** Write from ByteBuffer, starting at position loc.  
     * Return the number of bytes written
     */
    public int write(ByteBuffer buffer) ;
    
    /** Write from ByteBuffer, starting at position loc.  
     * Return the number of bytes written.
     * loc must be within 0 to length - writing at length is append */
    public int write(ByteBuffer buffer, long loc) ;
    
    /** Length of storage, in bytes.*/
    public long length() ;

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