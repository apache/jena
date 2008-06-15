/*
 * ong time (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import static lib.FileOps.clearDirectory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import com.hp.hpl.jena.query.*;

import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.btree.BTreeParams;

public class Run
{
    static String divider = "" ;
    static String nextDivider = null ;
    static void divider()
    {
        if ( nextDivider != null )
            System.out.println(nextDivider) ;
        nextDivider = "" ;
    }
    
    static int BlockSize               = 8*1024 ;
    static int SegmentSize = 8 * 1024 * 1024 ; 
    static int blocksPerSegment = SegmentSize/BlockSize ;
    
    private static int segment(int id) { return id/blocksPerSegment ; }
    private static int byteOffset(int id) { return (id%blocksPerSegment)*BlockSize ; }
    
    public static void main(String ... args)
    {
//        Id: 1179
//        Seg=1
//        Segoff=1,269,760

        System.out.printf("Blocksize = %d , Segment size = %d\n", BlockSize, SegmentSize) ;
        System.out.printf("blocksPerSegment = %d\n", blocksPerSegment) ;
        
        
        for ( int id : new int[]{1,2,3,4,5,1428, 1179})
        {
            int seg = segment(id) ;                     // Segment.
            int segOff = byteOffset(id) ; 
            System.out.printf("%d => [%d, %,d]\n", id, seg, segOff) ;
            System.out.printf("%,d\n", id*BlockSize) ;
        }
        System.exit(0) ;
//        String[] a = { "--set", "tdb:logBGP=true", "--desc="+assembler, query } ;
//        tdb.tdbquery.main(a) ;
//        System.exit(0) ;
        
        //tdbquery("dataset.ttl", "SELECT * { ?s ?p ?o}") ;
        ARQ.getContext().set(TDB.symFileMode, "mapped") ;
        Model model = TDBFactory.createModel("tmp") ;
        query("SELECT * { ?s ?p ?o}", model) ;
        System.exit(0) ;
        
        // ----
        btreePacking(3, 32, 8*1024) ; System.exit(0) ;
        btreePacking(3, 64, 8*1024) ;
        btreePacking(4, 128, 8*1024) ;
        System.exit(0) ;
                
        // ----
        System.exit(0) ;
        
        // ----
        String dir = "tmp" ;
        clearDirectory(dir) ;
        System.exit(0) ;
    }
     
    private static void report()
    {
        ARQ.getContext().set(TDB.symFileMode, "mapped") ;
        
        Model model = TDBFactory.createModel("foo");
        Resource r = model.createResource("http://com.xxx/test");

        Property op = model.createProperty("http://property/bar");
        Statement s = r.getProperty(op);
        if (s == null) {
            Resource list = model.createList();
            r.addProperty(op, list);
            s = r.getProperty(op);
        }

        model.write(System.err);
        
        //model.close() ;
        
        System.err.println("-------------");
        model = TDBFactory.createModel("foo");
        model.write(System.err);


        System.exit(0) ;

    }


    public static void btreePacking(int slots, int slotSize, int blkSize)
    {
        divider() ;
        RecordFactory f  = new RecordFactory(slots*slotSize/8,0) ;
        System.out.printf("Input: %d slots, size %d bytes, %d blocksize\n", slots,slotSize/8, blkSize ) ;
        System.out.println("Btree: "+BTreeParams.calcOrder(blkSize, f.recordLength())) ;      
        System.out.println("Packed leaf : "+blkSize/f.recordLength()) ;
        BTreeParams p = new BTreeParams(BTreeParams.calcOrder(blkSize, f.recordLength()), f) ;
        System.out.println(p) ;
    }             
    
    private static void query(String str, Model model)
    {
        System.out.println(str) ; 
        Query q = QueryFactory.create(str) ;
        QueryExecution qexec = QueryExecutionFactory.create(q, model) ;
        ResultSet rs = qexec.execSelect() ;
        ResultSetFormatter.out(rs) ;
        qexec.close() ;
    }
    
    private static void tdbquery(String assembler, String query)
    {
        String[] a = { "--set", "tdb:logBGP=true", "--desc="+assembler, query } ;
        tdb.tdbquery.main(a) ;
        System.exit(0) ;
    }
    
    private static void tdbloader(String assembler, String file)
    {
        tdb.tdbloader.main("--desc="+assembler, file) ; 
        System.exit(0) ;
    }
    
    private static void tdbconfig(String assembler, String file)
    {
        tdb.tdbconfig.main("stats", "--desc="+assembler) ;
        System.exit(0) ;
    }

}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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