/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.objectfile;

import java.nio.ByteBuffer ;
import java.util.Iterator ;

import org.openjena.atlas.lib.Pair ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.tdb.base.block.Block ;

public class ObjectFileLogger implements ObjectFile
{
    protected final ObjectFile other ;
    private static Logger defaultLogger = LoggerFactory.getLogger(ObjectFile.class) ; 
    private final Logger log ;
    private final String label  ;

    public ObjectFileLogger(String label, ObjectFile other)
    {
        this.other = other ;
        this.label = label ;
        log = defaultLogger ;
    }

    @Override
    public Block allocWrite(int maxBytes)
    {
        Block blk = other.allocWrite(maxBytes) ;
        info("allocWrite("+maxBytes+") -> "+blk.getId()) ;
        return blk ;
    }

    @Override
    public void completeWrite(Block buffer)
    {
        info("completeWrite("+buffer.getId()+")") ;
        other.completeWrite(buffer) ;
    }

    @Override
    public long write(ByteBuffer buffer)
    {
        info("write"+buffer) ;
        return other.write(buffer) ;
    }

    @Override
    public void reposition(long id)
    {
        info("reposition("+id+")") ;
        other.reposition(id) ;
    }

    @Override
    public ByteBuffer read(long id)
    {
        info("read("+id+")") ;
        return other.read(id) ;
    }

    @Override
    public Iterator<Pair<Long, ByteBuffer>> all()
    {
        info("all()") ;
        return other.all() ;
    }

    @Override
    public void sync()
    {
        info("sync") ;
        other.sync() ;
    }

    @Override
    public void close()
    {
        info("close") ;
        other.close() ;
    }

    @Override
    public long length()
    {
        info("") ;
        return other.length() ;
    }
    
    private void info(String string)
    {
        if ( label != null )
            string = label+": "+string ;
        log.info(string) ; 
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