/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.file;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.tdb.base.block.BlockException;
import com.hp.hpl.jena.tdb.pgraph.NodeId;

/** A file for writing serialized objects to disk, using DataInput/DataOuput
 * but that limits it to 64K bytes encoded forms. 
 * 
 *  The file is currently "read/append"
 *  Allocates an id (actually the byte offset in the file)
 * @author Andy Seaborne
 * @version $Id$
 */
public class ObjectFileDisk_DataIO implements ObjectFile
{
    // Uses DataInput/DataOutput string encoding.
    private String filename ;
    private RandomAccessFile out ;
    private long filesize ;

    ObjectFileDisk_DataIO(String filename)
    {
        this.filename = filename ;
        try {
            this.filename = filename ;
            // "rwd" - Syncs only the file contents
            // "rws" - Syncs the file contents and metadata
            // "rw" - cached?
            out = new RandomAccessFile(filename, "rw") ;
            filesize = out.length() ;
        } catch (IOException ex) { throw new BlockException("Failed to create ObjectFileDisk", ex) ; } 
    }
    
    public NodeId write(String str)
    { 
        try {
            long id = filesize ;
            out.seek(filesize) ;
            out.writeUTF(str) ;     // Limited to 64Kbytes. 
            filesize = out.length();
            return NodeId.create(id) ;
        } catch (IOException ex)
        { throw new FileException("ObjectFile.write", ex) ; }
    }
    
    public String read(NodeId id)
    {
        try {
            out.seek(id.getId()) ;
            String s = out.readUTF() ;
            return s ;
        } catch (IOException ex)
        { throw new FileException("ObjectFile.read", ex) ; }
    }
    
    @Override
    public void close()
    {
        try {
            out.close() ;
        } catch (IOException ex)
        { throw new FileException("ObjectFile.close", ex) ; }

    }

    @Override
    public void sync(boolean force)
    {
        try {
            out.getChannel().force(true) ;
        } catch (IOException ex)
        { throw new FileException("ObjectFile.sync", ex) ; }
    }

    public List<String> all()
    {
        try {
            List<String> strings = new ArrayList<String>() ;
            out.seek(0) ;
            try { 
                while(true)
                {
                    String s = out.readUTF() ;
                    strings.add(s) ;
                } 
            } catch (EOFException ex) {}
            return strings ;
        } catch (IOException ex)
        { throw new FileException("ObjectFile.all", ex) ; }
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