/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.file;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.tdb.base.block.BlockException;

/** Single, unsegmented file with ByteBuffer */
public class PlainFilePersistent extends PlainFile
{
    private static Logger log = LoggerFactory.getLogger(PlainFilePersistent.class) ;
    
    private FileChannel channel ;
    private RandomAccessFile out ;
    private String filename ;

    // Plain file over mmapped ByteBuffer 
    PlainFilePersistent(Location loc, String filename)
    {
        this(loc.getPath(filename, "dat")) ;
    }
    
    PlainFilePersistent(String filename)
    {
        try {
            this.filename = filename ;
            // "rwd" - Syncs only the file contents
            // "rws" - Syncs the file contents and metadata
            // "rw" - cached?
            
            out = new RandomAccessFile(filename, "rw") ;
            long filesize = out.length() ;
            channel = out.getChannel() ;
            byteBuffer = allocateBuffer(filesize) ;
            //if ( channel.size() == 0 ) {}
        } catch (IOException ex) { throw new FileException("Failed to create BlockMgrFile", ex) ; }
    }
    
    protected PlainFilePersistent() 
    {
        //For simulating subclasses.
        channel = null ;
        out = null ;
    }
    
    
    @Override
    public void sync()
    { 
        try
        {
            channel.force(false) ;
        } catch (IOException ex)
        { throw new FileException("force", ex) ; }
    }
    
    @Override
    public void close()
    {
        try {
            //sync() ;
            //channel.close();
            out.close();        // Closes the channel.
            channel = null ;
            out = null ;
        } catch (IOException ex)
        { throw new BlockException("BlockMgrMapped.close", ex) ; }

    }

    @Override
    protected ByteBuffer allocateBuffer(long size)
    {
        try {
            return channel.map(FileChannel.MapMode.READ_WRITE, 0, size) ;
        } catch (IOException ex)
        {
            throw new FileException("allocateBuffer", ex) ;
        }
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