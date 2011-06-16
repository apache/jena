/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package tx.base;

import java.util.HashMap ;
import java.util.Map ;
import java.util.TreeMap ;

import org.openjena.atlas.lib.FileOps ;
import org.openjena.atlas.lib.Lib ;
import org.openjena.atlas.lib.Tuple ;
import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.sys.Names ;



/** File references.  
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
    static Map<String, Integer> name2id = new HashMap<String, Integer>() ;
    static Map<Integer, FileRef> id2name = new TreeMap<Integer, FileRef>() ;// new HashMap<Integer, FileRef>() ;
    
    static private void printTable()
    {
        for ( String name : Names.tripleIndexes )
            bTree(name) ;
        for ( String name : Names.quadIndexes )
            bTree(name) ;
//        // Not the name of the index.
//        for ( String name : Names.prefixIndexes )
//            bTree(name) ;

        bTree(Names.prefixId2Node) ;
        bTree(Names.prefixNode2Id) ;
        file(Names.indexId2Node+".dat") ;
        bTree(Names.indexNode2Id) ;
        bTree(Names.indexPrefix) ;
        
        for ( Map.Entry<Integer, FileRef> e : id2name.entrySet() )
        {
            System.out.printf("    add(%s+idxOffset , %s) ;\n", e.getKey()-idxOffset, '"'+e.getValue().filename+'"') ;
        }
        
        
    }
    
    static {
        //printTable() ;
        add(journalIdx , journalName) ;
        // The code above produces this:
        // To make it clear and stable, we keep this form.
        add(0+idxOffset , "SPO.idn") ;
        add(1+idxOffset , "SPO.dat") ;
        add(2+idxOffset , "POS.idn") ;
        add(3+idxOffset , "POS.dat") ;
        add(4+idxOffset , "OSP.idn") ;
        add(5+idxOffset , "OSP.dat") ;
        add(6+idxOffset , "GSPO.idn") ;
        add(7+idxOffset , "GSPO.dat") ;
        add(8+idxOffset , "GPOS.idn") ;
        add(9+idxOffset , "GPOS.dat") ;
        add(10+idxOffset , "GOSP.idn") ;
        add(11+idxOffset , "GOSP.dat") ;
        add(12+idxOffset , "POSG.idn") ;
        add(13+idxOffset , "POSG.dat") ;
        add(14+idxOffset , "OSPG.idn") ;
        add(15+idxOffset , "OSPG.dat") ;
        add(16+idxOffset , "SPOG.idn") ;
        add(17+idxOffset , "SPOG.dat") ;
        add(18+idxOffset , "prefixes.idn") ;
        add(19+idxOffset , "prefixes.dat") ;
        add(20+idxOffset , "prefix2id.idn") ;
        add(21+idxOffset , "prefix2id.dat") ;
        add(22+idxOffset , "nodes.dat") ;
        add(23+idxOffset , "node2id.idn") ;
        add(24+idxOffset , "node2id.dat") ;
        add(25+idxOffset , "prefixIdx.idn") ;
        add(26+idxOffset , "prefixIdx.dat") ;
        
        add(50+idxOffset , "TEST") ;
        add(51+idxOffset , "TEST1") ;
        add(52+idxOffset , "TEST2") ;
    }
    public static final FileRef Journal = get(journalIdx) ;
    
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
        name2id.put(name, idx) ;
        id2name.put(idx, new FileRef(name, idx)) ;
    }
    // --------

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