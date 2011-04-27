/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.objectfile;

import java.nio.ByteBuffer ;

import org.openjena.atlas.lib.Bytes ;
import org.openjena.atlas.lib.Closeable ;
import org.openjena.atlas.lib.Sync ;


import com.hp.hpl.jena.tdb.lib.StringAbbrev ;

/** Wrap a {@link ObjectFile} with a string encoder/decoder.  
 * Controls the UTF encoder/decoder and is not limited to 64K byte encoded forms.
 */

public class StringFile implements Sync, Closeable
{
    protected final ObjectFile file ;
    /*
     * Encoding: Simple for now:
     *   length (4 bytes)
     *   UTF-8 bytes. 
     */
    
    public StringFile(ObjectFile file)
    {
        this.file = file ;
    }
    
    public long write(String str)
    { 
        str = compress(str) ;
        ByteBuffer bb = file.allocWrite(4*str.length()) ;
        int len = Bytes.toByteBuffer(str, bb) ;
        bb.flip() ;
        return file.completeWrite(bb) ;
        
        
//        ByteBuffer bb = ByteBuffer.allocate(4*str.length()) ;   // Worst case
//        int len = Bytes.toByteBuffer(str, bb) ;
//        bb.flip() ;
//        return file.write(bb) ;
    }
    
    public String read(long id)
    {
        ByteBuffer bb = file.read(id) ;
        String x = Bytes.fromByteBuffer(bb) ;
        x = decompress(x) ;
        return x ;
    }

    @Override
    public void close()
    { file.close() ; }

    @Override
    public void sync() { file.sync() ; }
    
    public void flush() { sync() ; }

    public ObjectFile getByteBufferFile()
    {
        return file ;
    }
    
    // ---- Dump
    public void dump() { dump(handler) ; }

    public interface DumpHandler { void handle(long fileIdx, String str) ; }  
    
    public void dump(DumpHandler handler)
    {
        long fileIdx = 0 ;
        while ( true )
        {
            ByteBuffer bb = file.read(fileIdx) ;
            String str = Bytes.fromByteBuffer(bb) ;
            handler.handle(fileIdx, str) ;
            fileIdx = fileIdx + bb.limit() + 4 ;
            if ( fileIdx >= file.length() )
                break ;
        }
    }
    
    static StringFile.DumpHandler handler = new StringFile.DumpHandler() {
        @Override
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