/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.file;

import java.io.IOException ;
import java.nio.ByteBuffer ;

import org.openjena.atlas.io.IO ;
import org.openjena.atlas.lib.FileOps ;


public class BufferChannelFile implements BufferChannel
{
    private FileBase file ;

    public BufferChannelFile(String filename)
    {
        file = new FileBase(filename) ;
    }
    
    @Override
    public BufferChannel duplicate()
    {
        return new BufferChannelFile(file.filename) ;
    }

    @Override
    public long position()
    {
        try { return file.channel.position() ; } 
        catch (IOException e) { IO.exception(e) ; return -1 ; }
    }

    @Override
    public void position(long pos)
    {
        try { file.channel.position(pos) ; } 
        catch (IOException e) { IO.exception(e) ; }
    }

    @Override
    public void truncate(long length)
    {
        try { 
            // http://bugs.sun.com/view_bug.do?bug_id=6191269
            if ( length < file.channel.position() )
                file.channel.position(length) ;
            file.channel.truncate(length) ;
        }
        catch (IOException e) { IO.exception(e) ; }
    }

    @Override
    public int read(ByteBuffer buffer)
    {
        try { return file.channel.read(buffer) ; } 
        catch (IOException e) { IO.exception(e) ; return -1 ; }
    }
    
    
    @Override
    public int read(ByteBuffer buffer, long loc)
    {
        try { return file.channel.read(buffer, loc) ; } 
        catch (IOException e) { IO.exception(e) ; return -1 ; }
    }

    @Override
    public int write(ByteBuffer buffer)
    {
        try { return file.channel.write(buffer) ; } 
        catch (IOException e) { IO.exception(e) ; return -1 ; }
    }

    @Override
    public int write(ByteBuffer buffer, long loc)
    {
        try { return file.channel.write(buffer, loc) ; } 
        catch (IOException e) { IO.exception(e) ; return -1 ; }
    }

    @Override
    public long size()
    {
        try { return file.channel.size() ; }
        catch (IOException e) { IO.exception(e) ; return -1 ; }
    }

    @Override
    public void sync()
    { 
        try { file.channel.force(true) ; }
        catch (IOException e) { IO.exception(e) ; }
    }

    @Override
    public void close()
    {
        try { file.channel.close() ; }
        catch (IOException e) { IO.exception(e) ; }
    }

    @Override
    public String getLabel()
    {
        return FileOps.basename(file.getFilename()) ;
    }

    @Override
    public String toString()
    {
        return file.getFilename() ;
    }

    @Override
    public String getFilename()
    {
        return file.getFilename() ;
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