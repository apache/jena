/*
 * ong time (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import static lib.FileOps.clearDirectory;
import lib.FileOps;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
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
    
    
    public static void main(String ... args)
    {
//        String[] a = { "--set", "tdb:logBGP=true", "--desc="+assembler, query } ;
//        tdb.tdbquery.main(a) ;
//        System.exit(0) ;
        
        tdbquery("dataset.ttl", "SELECT * { ?s ?p 1}") ;
        
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
//        System.out.println("Mem model") ;
//        report(ModelFactory.createDefaultModel()) ;
        System.out.println("TDB model") ;
        report(TDBFactory.createModel()) ;
        System.exit(0) ;
    }
    
    private static void report(Model model)
    {
        Model model2 = ModelFactory.createDefaultModel() ;
        
        Resource r = model.createResource("http://com.xxx/test");
       
        Property op = model2.createProperty("property");
        //Statement s = null ;
        Statement s1 = r.getProperty(op);
        r.addProperty(op, "string") ;
        Statement s2 = r.getProperty(op);
       
        System.out.println("s1: "+s1) ;
        System.out.println("s2: "+s2) ;
    }
    
    private static void report1()
    {
        FileOps.clearDirectory("tmp") ;
        Model model = TDBFactory.createModel("tmp") ;
        //Model model = TDBFactory.createModel() ;
        
        Resource r = model.createResource("foo");
        RDFList list = model.createList();
        Property p = model.createProperty("p") ;
        Property p2 = model.createProperty("p2") ;
        
        r.addProperty(p, list);
        Statement s = r.getProperty(p);
        System.out.println(s) ;

        s = r.getProperty(p2);
        System.out.println(s) ;
        
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