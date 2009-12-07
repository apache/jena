/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.file;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import com.hp.hpl.jena.sparql.core.Closeable;
import com.hp.hpl.jena.tdb.base.block.BlockException;
import com.hp.hpl.jena.tdb.lib.Sync;

public class FileBase implements Sync, Closeable
{
    public final String filename ;
    public final FileChannel channel ;
    public final RandomAccessFile out ;

    public FileBase(String filename)
    {
        this.filename = filename ;
        try {
            // "rwd" - Syncs only the file contents
            // "rws" - Syncs the file contents and metadata
            // "rw" - cached?
            out = new RandomAccessFile(filename, "rw") ;
            channel = out.getChannel() ;
        } catch (IOException ex) { throw new BlockException("Failed to create FileBase", ex) ; } 
    }

    //@Override
    public void close()
    {
        try {
            channel.close() ;
        } catch (IOException ex)
        { throw new FileException("FileBase.close", ex) ; }

    }

  //@Override
    public void sync() { sync(true) ; }
    
    //@Override
    public void sync(boolean force)
    {
        try {
            channel.force(true) ;
        } catch (IOException ex)
        { throw new FileException("FileBase.sync", ex) ; }
    }

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