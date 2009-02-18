/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.objectfile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import lib.Bytes;
import lib.Pair;

import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.base.block.BlockException;
import com.hp.hpl.jena.tdb.base.file.FileBase;
import com.hp.hpl.jena.tdb.base.file.FileException;
import com.hp.hpl.jena.tdb.lib.StringAbbrev;
import com.hp.hpl.jena.tdb.store.NodeId;

/** Controls the UTF encoder/decoder and is not limited to 64K byte encoded forms.
 * @see ObjectFileDisk_DataIO
 * @author Andy Seaborne
 * @version $Id$
 */

public class ObjectFileDiskWithCache extends FileBase implements ObjectFile 
{
    // Not used - this is not faster.  It is more complicated. 
    
    /* No synchronization - assumes that the caller has some appropriate lock
     * because the combination of file and cache operations need to be thread safe.  
     */
    private long filesize ;

    /*
     * Encoding: Simple for now:
     *   length (4 bytes)
     *   UTF-8 bytes. 
     */
    
    // Write cache.  
    // 1 - Strings remembered that ar in the cache but not written yet 
    // 2 - Accumulate a large buffer before writing  
    // Seems to make a small difference?
    int delayCacheSize = 100 ;
    List<Pair<NodeId, String>> delayCache = (delayCacheSize == -1 ? null : new ArrayList<Pair<NodeId, String>>(delayCacheSize)) ;
    //ByteBuffer delayCache
    ByteBuffer buffer = (delayCacheSize == -1 ? null : ByteBuffer.allocate(delayCacheSize*100) ) ;
    long idAllocation ;

    public ObjectFileDiskWithCache(String filename)
    {
        super(filename) ;
        try { 
            filesize = out.length() ;
            idAllocation = filesize ;
        } catch (IOException ex) { throw new BlockException("Failed to get filesize", ex) ; } 
    }
    
    // Write cache.  Strings written but not sent to disk. 
    // List<Pair<NodeId, String>>
    
    @Override
    public NodeId write(String str)
    { 
        if ( delayCache != null && delayCache.size() >= delayCacheSize )
            flushCache() ;
        
        str = compress(str) ;
        if ( buffer == null )
            _writeNow(str) ;
        
        // Write to the buffer now.
        
        int max = 4+4*str.length() ;        // Worst case.
        int x = buffer.position() ;
        if ( x+max > buffer.limit() )
        {
            flushCache() ;
            // Will never fit
            if ( max > buffer.limit() )
                return _writeNow(str) ;
        }
        
        buffer.position(x+4) ;              // Space for length
        Bytes.toByteBuffer(str, buffer) ;
        int y = buffer.position() ;
        int len = y-x-4 ;
        buffer.position(x) ;
        buffer.putInt(0, len) ;             // Object length
        buffer.position(y) ;

        long location = idAllocation ;
        idAllocation = idAllocation + len+4 ;
        
        NodeId nodeId = NodeId.create(location) ;
        
        //int i = delayCache.size() ;
        delayCache.add(new Pair<NodeId, String>(nodeId, str)) ;
        return nodeId ; 
    }
    
    private NodeId _writeNow(String str)
    {

        try {
            long location = filesize ;
            ByteBuffer bb = ByteBuffer.allocate(4+4*str.length()) ;
            bb.position(4) ;
            Bytes.toByteBuffer(str, bb) ;
            int len = bb.position()-4 ;
            bb.limit(len+4) ;
            bb.putInt(0, len) ;     // Object length
            bb.position(0) ;
            int x = channel.write(bb) ;
            if ( x != bb.limit() )
                throw new FileException("ObjectFile.write: Buffer length = "+bb.limit()+" : actual write = "+x) ; 
            filesize = filesize+x ;
            return NodeId.create(location) ;
        } catch (IOException ex)
        { throw new FileException("ObjectFile.write", ex) ; }
    }

    private void flushCache()
    {
        if ( delayCache == null )
            return ;
        // Convert to one big write.
        long id = filesize ;
        try {
            // write length
            buffer.flip() ;
            long x = channel.write(buffer) ;
            if ( x != (idAllocation-filesize) )
                throw new FileException("ObjectFile.flushCache: Buffer length = "+(idAllocation-filesize)+" : actual write = "+x) ; 
            filesize = idAllocation ;
            buffer.clear() ;
        } catch (IOException ex)
        { throw new FileException("ObjectFile.write", ex) ; }

        delayCache.clear() ;
        buffer.clear() ;
    }
    
    @Override
    public String read(NodeId id)
    {
        // Check cache.
        String x = null ;
        if ( id.getId() > filesize )
            x = findInCache(id) ;
        else
        {
            ByteBuffer bb = readBytes(id) ;
            x = Bytes.fromByteBuffer(bb) ;
        }
        x = decompress(x) ;
        return x ;
    }

    private String findInCache(NodeId id)
    {
        for ( Pair<NodeId, String> elt : delayCache )
        {
            NodeId n = elt.car() ;
            if ( n.equals(id) )
                return elt.cdr();
            if ( n.getId() <  elt.car().getId() )
                break ;
        }
        throw new TDBException("Asked for impossible NodeId: "+id) ;
    }

    private ByteBuffer readBytes(NodeId id) { return readBytes(id.getId()) ; }
    
    private ByteBuffer readBytes(long loc)
    {
        try {
            ByteBuffer bb = ByteBuffer.allocate(4) ;
            channel.position(loc) ;
            int x = channel.read(bb) ;  // Updates position.
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
        flushCache() ;
        try {
            channel.close() ;
        } catch (IOException ex)
        { throw new FileException("ObjectFile.close", ex) ; }

    }

    @Override
    public void sync(boolean force)
    {
        if ( force )
            flushCache() ;
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
        long x = 0 ;
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
    
    public void dump()
    {
        try { out.seek(0) ; } 
        catch (IOException ex) { throw new FileException("ObjectFile.all", ex) ; }
        
        long fileIdx = 0 ;
        while ( fileIdx < filesize )
        {
            ByteBuffer bb = readBytes(fileIdx) ;
            String str = Bytes.fromByteBuffer(bb) ;
            System.out.printf("0x%08X : %s\n", fileIdx, str) ;
            fileIdx = fileIdx + bb.limit() + 4 ; 
        }
    }
    
    
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