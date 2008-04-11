/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.file;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import lib.Bytes;

import com.hp.hpl.jena.tdb.base.block.BlockException;
import com.hp.hpl.jena.tdb.lib.StringAbbrev;
import com.hp.hpl.jena.tdb.pgraph.NodeId;

/** Controls the UTF encoder/decoder and is not limited to 64K byte encoded forms.
 * @author Andy Seaborne
 * @version $Id$
 */
public class ObjectFileDisk extends FileBase implements ObjectFile 
{
    private long filesize ;

    /*
     * Encoding: Simple for now:
     *   length (4 bytes)
     *   UTF-8 bytes. 
     */
    
    ObjectFileDisk(String filename)
    {
        super(filename) ;
        try { 
            filesize = out.length() ;
        } catch (IOException ex) { throw new BlockException("Failed to get filesize", ex) ; } 
    }
    
    @Override
    public NodeId write(String str)
    { 
        str = compress(str) ;
        ByteBuffer bb = ByteBuffer.allocate(4+4*str.length()) ;   // Worst case
        bb.position(4) ;
        Bytes.toByteBuffer(str, bb) ;
        int len = bb.position()-4 ;
        bb.limit(len+4) ;
        bb.putInt(0, len) ;     // Object length
        bb.position(0) ;
        try {
            long location = filesize ;
            channel.position(location) ;
            // write length
            
            int x = channel.write(bb) ;
            if ( x != len+4 )
                throw new FileException("ObjectFile.write: Buffer length = "+len+" : actual write = "+x) ; 
            filesize = filesize+x ;
            return NodeId.create(location) ;
        } catch (IOException ex)
        { throw new FileException("ObjectFile.write", ex) ; }
    }
    
    @Override
    public String read(NodeId id)
    {
        ByteBuffer bb = readBytes(id) ;
        String x = Bytes.fromByteBuffer(bb) ;
        x = decompress(x) ;
        return x ;
    }

    private ByteBuffer readBytes(NodeId id) { return readBytes(id.getId()) ; }
    
    private ByteBuffer readBytes(long loc)
    {
        try {
            ByteBuffer bb = ByteBuffer.allocate(4) ;
            channel.position(loc) ;
            int x = channel.read(bb) ;
            if ( x != 4 )
                throw new FileException("ObjectFile.read: Failed to read the length : got "+x+" bytes") ;
            int len = bb.getInt(0) ;
            bb = ByteBuffer.allocate(len) ;
            channel.position(loc+4) ;
            x = channel.read(bb) ;
            bb.position(0) ;
            bb.limit(len) ;
            if ( x != len )
                throw new FileException("ObjectFile.read: Failed to read the object ("+len+" bytes) : got "+x+" bytes") ;
            return bb ;
        } catch (IOException ex)
        { throw new FileException("ObjectFile.read", ex) ; }
    }
    
    @Override
    public void close()
    {
        try {
            channel.close() ;
        } catch (IOException ex)
        { throw new FileException("ObjectFile.close", ex) ; }

    }

    @Override
    public void sync(boolean force)
    {
//        try {
//            channel.force(true) ;
//        } catch (IOException ex)
//        { throw new FileException("ObjectFile.sync", ex) ; }
    }

    
    public List<String> all()
    {
        try { out.seek(0) ; } 
        catch (IOException ex) { throw new FileException("ObjectFile.all", ex) ; }
        
        List<String> strings = new ArrayList<String>() ;
        int x = 0 ;
        while ( x < filesize )
        {
            ByteBuffer bb = readBytes(x) ;
            String str = Bytes.fromByteBuffer(bb) ;
            strings.add(str) ;
            // Assumes magic.
            x = x + bb.limit() + 4 ; 
        }
        return strings ;
    }
    
    
    // URI compression can be effective but literals are more of a problem.  More variety. 
    public static boolean compression = false ; 
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