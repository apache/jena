/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.tdb.base.objectfile;

import java.nio.ByteBuffer ;

import org.apache.jena.atlas.lib.Bytes ;
import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.Sync ;


import com.hp.hpl.jena.tdb.base.block.Block ;
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
        Block block = file.allocWrite(4*str.length()) ;
        int len = Bytes.toByteBuffer(str, block.getByteBuffer()) ;
        block.getByteBuffer().flip() ;
        file.completeWrite(block) ;
        return block.getId() ;
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
