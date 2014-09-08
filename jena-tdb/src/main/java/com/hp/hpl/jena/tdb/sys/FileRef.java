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

package com.hp.hpl.jena.tdb.sys;

import java.util.HashMap ;
import java.util.Map ;
import java.util.TreeMap ;

import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.lib.Tuple ;
import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;



/** File references.  
 *  These are not filenames - they are a per-database file id.
 */
public class FileRef
{
    private final String filename ;
    private final int id ;
    static final int idxOffset = 10 ;   // Must be > journal index name.
    
    static final String journalName = "journal" ;
    static final int journalIdx = idxOffset-1 ;
    // --------
    // May to/from names to a short id.
    // THIS MUST BE PERSISTENT
    static Map<String, Integer> name2id = new HashMap<>() ;
    static Map<Integer, FileRef> id2name = new TreeMap<>() ;// new HashMap<Integer, FileRef>() ;
    
    static {
        //printTable() ;
        add(journalIdx , journalName) ;

        file("SPO.idn") ;
        file("SPO.dat") ;
        file("POS.idn") ;
        file("POS.dat") ;
        file("OSP.idn") ;
        file("OSP.dat") ;
        file("GSPO.idn") ;
        file("GSPO.dat") ;
        file("GPOS.idn") ;
        file("GPOS.dat") ;
        file("GOSP.idn") ;
        file("GOSP.dat") ;
        file("POSG.idn") ;
        file("POSG.dat") ;
        file("OSPG.idn") ;
        file("OSPG.dat") ;
        file("SPOG.idn") ;
        file("SPOG.dat") ;
        file("prefixes.idn") ;
        file("prefixes.dat") ;
        file("prefix2id.idn") ;
        file("prefix2id.dat") ;
        file("nodes.dat") ;
        file("node2id.idn") ;
        file("node2id.dat") ;
        file("prefixIdx.idn") ;
        file("prefixIdx.dat") ;
        
        add(1000+idxOffset, "TEST") ;
        add(1001+idxOffset, "TEST1") ;
        add(1002+idxOffset, "TEST2") ;
    }
    public static final FileRef Journal = get(journalIdx) ;
    
    public static void register(String fn) {
        file(fn) ;
    }
    
    private static void add(int idx, String fn)
    {
        name2id.put(fn, idx) ;
        id2name.put(idx, new FileRef(fn, idx)) ;
    }
    
    private static void bTree(String name)
    {
        file(name+".idn") ;
        file(name+".dat") ;
    }

    /** Public - for testing */
    public static void file(String name)
    {
        int idx = name2id.size() + idxOffset ;
        add(idx, name) ;
    }
    // --------
    
    static public FileRef create(FileSet fileSet, String ext)
    {
        return create(fileSet.filename(ext)) ;
    }

    static public FileRef create(String filename)
    {
        Tuple<String> x = FileOps.splitDirFile(filename) ;
        String key = x.get(1) ;
        if ( ! name2id.containsKey(key) )
        {
            Log.warn(FileRef.class, "File name not registered: "+filename) ;
            file(key) ;
        }
            
        return new FileRef(key, name2id.get(key)) ;
    }
    
    public static FileRef get(int fileId)
    {
        FileRef f = id2name.get(fileId) ;
        if ( f == null )
        {
            Log.fatal(FileRef.class, "No FileRef registered for id: "+fileId) ;
            throw new TDBException("No FileRef registered for id: "+fileId) ;
        }
        return f ;
    }

    //    static public FileRef create(FileRef other)
//    { 
//        return new FileRef(other) ;
//    }
    
    private FileRef(String filename, int id)
    {
        // Canonicalise filename.
        if ( filename == null )
            throw new IllegalArgumentException("Null for a FileRef filename") ;
        this.filename = filename.intern() ;
        this.id = id ;
    }
    
    //private FileRef(FileRef other)  { this.filename = other.filename ; this.id = other.id ; }
    
    public String getFilename() { return filename ; }
    
    @Override
    public String toString()    { return "fileref("+id+"):"+filename ; }

    public int getId()          { return id ; }

    @Override
    public int hashCode()
    {
        final int prime = 31 ;
        int result = 1 ;
        result = prime * result + ((filename == null) ? 0 : filename.hashCode()) ;
        result = prime * result + id ;
        return result ;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true ;
        if (obj == null) return false ;
        if (getClass() != obj.getClass()) return false ;
        FileRef other = (FileRef)obj ;
        if ( id != other.id )
            return false ;
        // Should not be needed.
        if ( ! Lib.equal(filename, other.filename) ) return false ;
        return true ;
    }
}
