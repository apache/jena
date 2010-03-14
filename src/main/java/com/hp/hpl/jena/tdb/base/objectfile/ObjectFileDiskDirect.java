/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.objectfile;

import java.io.IOException ;
import java.nio.ByteBuffer ;
import java.util.Iterator ;

import org.openjena.atlas.lib.Bytes ;
import org.openjena.atlas.lib.Pair ;


import com.hp.hpl.jena.tdb.base.block.BlockException ;
import com.hp.hpl.jena.tdb.base.file.FileBase ;
import com.hp.hpl.jena.tdb.base.file.FileException ;
import com.hp.hpl.jena.tdb.lib.StringAbbrev ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

/** Variable length ByteBuffer file on disk.  Read by id ; write is append-only */  

public class ObjectFileDiskDirect implements ObjectFile 
{
    /* No synchronization - assumes that the caller has some appropriate lock
     * because the combination of file and cache operations needs to be thread safe.  
     */
    protected long filesize ;
    protected final FileBase file ;

    public ObjectFileDiskDirect(String filename)
    {
        file = new FileBase(filename) ;
        try { 
            filesize = file.out.length() ;
        } catch (IOException ex) { throw new BlockException("Failed to get filesize", ex) ; } 
    }
    
    private ByteBuffer lengthBuffer = ByteBuffer.allocate(SystemTDB.SizeOfInt) ;
    
    //@Override
    public long write(ByteBuffer bb)
    {
        try {
            // Write length
            int len = bb.limit() - bb.position();
            lengthBuffer.clear() ;
            lengthBuffer.putInt(0, len) ;
            
            long location = filesize ;
            file.channel.position(location) ;
            int x1 = file.channel.write(lengthBuffer) ;
            int x2 = file.channel.write(bb) ;
            if ( x2 != len )
                throw new FileException("ObjectFile.write: Buffer length = "+len+" : actual write = "+x2) ;
            
            filesize = filesize+x1+x2 ;
            return location ;
        } catch (IOException ex)
        { throw new FileException("ObjectFile.write", ex) ; }
    }
    
    //@Override
    public ByteBuffer read(long loc)
    {
        try {
            file.channel.position(loc) ;
            lengthBuffer.position(0) ;
            int x = file.channel.read(lengthBuffer) ;  // Updates position.
            if ( x != 4 )
                throw new FileException("ObjectFile.read: Failed to read the length : got "+x+" bytes") ;
            int len = lengthBuffer.getInt(0) ;
            ByteBuffer bb = ByteBuffer.allocate(len) ;
            //file.channel.position(loc+4) ; // Unnecessary.
            x = file.channel.read(bb) ;
            bb.flip() ;
            if ( x != len )
                throw new FileException("ObjectFile.read: Failed to read the object ("+len+" bytes) : got "+x+" bytes") ;
            return bb ;
        } catch (IOException ex)
        { throw new FileException("ObjectFile.read", ex) ; }
    }
    
    public long length()
    {
        return filesize ;
    }

    public Iterator<Pair<Long, ByteBuffer>> all()
    {
        try { file.out.seek(0) ; } 
        catch (IOException ex) { throw new FileException("ObjectFile.all", ex) ; }

        ObjectIterator iter = new ObjectIterator(0, filesize) ;
        return iter ;
    }
    
    private class ObjectIterator implements Iterator<Pair<Long, ByteBuffer>>
    {
        final private long start ;
        final private long finish ;
        private long current ;

        public ObjectIterator(long start, long finish)
        {
            this.start = start ;
            this.finish = finish ;
            this.current = start ;
        }
        
        public boolean hasNext()
        {
            return ( current < finish ) ;
        }

        public Pair<Long, ByteBuffer> next()
        {
            long x = current ;
            ByteBuffer bb = read(current) ;
            current = current + bb.limit() + 4 ; 
            return new Pair<Long, ByteBuffer>(x, bb) ;
        }

        public void remove()
        { throw new UnsupportedOperationException() ; }
    }
    

    //@Override
    public void close()
    { file.close() ; }

    //@Override
    public void sync()                  { file.sync() ; }
    
    //@Override
    public void sync(boolean force)     { file.sync(force) ; }

    // ---- Dump
    public void dump() { dump(handler) ; }

    public interface DumpHandler { void handle(long fileIdx, String str) ; }  
    
    public void dump(DumpHandler handler)
    {
        try { file.out.seek(0) ; } 
        catch (IOException ex) { throw new FileException("ObjectFile.all", ex) ; }
        
        long fileIdx = 0 ;
        while ( fileIdx < filesize )
        {
            ByteBuffer bb = read(fileIdx) ;
            String str = Bytes.fromByteBuffer(bb) ;
            handler.handle(fileIdx, str) ;
            fileIdx = fileIdx + bb.limit() + 4 ;
        }
    }
    
    static ObjectFileDiskDirect.DumpHandler handler = new ObjectFileDiskDirect.DumpHandler() {
        //@Override
        public void handle(long fileIdx, String str)
        {
            System.out.printf("0x%08X : %s\n", fileIdx, str) ;
        }
    } ;
    // ----
 
    
    // URI compression can be effective but literals are more of a problem.  More variety. 
    public final static boolean compression = false ; 
    private static StringAbbrev abbreviations = new StringAbbrev() ;
    static {
        abbreviations.add(  "rdf",      "<http://www.w3.org/1999/02/22-rdf-syntax-ns#") ;
        abbreviations.add(  "rdfs",     "<http://www.w3.org/2000/01/rdf-schema#") ;
        abbreviations.add(  "xsd",      "<http://www.w3.org/2001/XMLSchema#") ;
        
        // MusicBrainz
        abbreviations.add(  "mal",      "<http://musicbrainz.org/mm-2.1/album/") ;
        abbreviations.add(  "mt",       "<http://musicbrainz.org/mm-2.1/track/") ;
        abbreviations.add(  "mar",      "<http://musicbrainz.org/mm-2.1/artist/") ;
        abbreviations.add(  "mtr",      "<http://musicbrainz.org/mm-2.1/trmid/") ;
        abbreviations.add(  "mc",       "<http://musicbrainz.org/mm-2.1/cdindex/") ;
        
        abbreviations.add(  "m21",      "<http://musicbrainz.org/mm/mm-2.1#") ;
        abbreviations.add(  "dc",       "<http://purl.org/dc/elements/1.1/") ;
        // DBPedia
        abbreviations.add(  "r",        "<http://dbpedia/resource/") ;
        abbreviations.add(  "p",        "<http://dbpedia/property/") ;
    }
    private String compress(String str)
    {
        if ( !compression || abbreviations == null ) return str ;
        return abbreviations.abbreviate(str) ;
    }

    private String decompress(String x)
    {
        if ( !compression || abbreviations == null ) return x ;
        return abbreviations.expand(x) ;
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