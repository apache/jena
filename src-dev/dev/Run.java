/*
 * ong time (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.tdb.Const;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.block.BlockMgr;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.base.recordfile.RecordBufferPage;
import com.hp.hpl.jena.tdb.base.recordfile.RecordBufferPageMgr;
import com.hp.hpl.jena.tdb.pgraph.PGraphBase;
import com.hp.hpl.jena.util.FileManager;

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
        readBPlusTreeAll() ;
        
        //tdbquery("dataset.ttl", "SELECT * { ?s ?p ?o }") ;

//      Model model = TDBFactory.createModel("tmp") ;
//      query("SELECT * { ?s ?p ?o}", model) ;
//      System.exit(0) ;

        
    }
    
    private static void readBPlusTreeAll()
    {
        divider() ;
        String filename = "DB/OSP.dat" ;
        BlockMgr blkMgr = BlockMgrFactory.createFile(filename, Const.BlockSize) ;
        RecordFactory f = PGraphBase.indexRecordFactory ; 
        
        RecordBufferPageMgr recordPageMgr = new RecordBufferPageMgr(f, blkMgr) ;
        int idx = 0 ;
        int n = 0 ;
        while ( idx >= 0 )
        {
            RecordBufferPage page = recordPageMgr.get(idx) ;
            System.out.printf("%04d :: %04d -> %04d [%d, %d]\n", n, page.getId(), page.getLink(), page.getCount(), page.getMaxSize()) ;
            idx = page.getLink() ;
            n++ ;
        }
        System.exit(0) ;
        
    }

    static public void smallGraph() 
    {
        // Do NOW!
        // TDB.getContext().set(TDB.symFileMode, "mapped") ;
        TDB.getContext().set(TDB.symIndexType, "bplustree") ;
        
        Location loc = new Location("tmp") ;
        //FileOps.clearDirectory(loc.getDirectoryPath()) ;
        
        Graph graph = TDBFactory.createGraph(loc) ;
        
//        Node s1 = SSE.parseNode("<s1>") ;
//        Node p1 = SSE.parseNode("<p1>") ;
//        Node o1 = SSE.parseNode("<o1>") ;
//        Node s2 = SSE.parseNode("<s2>") ;
//        Node p2 = SSE.parseNode("<p2>") ;
//        Node o2 = SSE.parseNode("<o2>") ;
//        Triple t1 = new Triple(s1,p1,o1) ; 
//        Triple t2 = new Triple(s2,p2,o2) ;
//        
//        graph.add(t1) ;
//        graph.add(t2) ;
//        graph.delete(t1) ;
        
        
        Model model = ModelFactory.createModelForGraph(graph) ;
        
        //query("SELECT * { <no> <no> <no>}", model) ;
        //System.out.flush() ;
        
        if ( model.isEmpty() )
        {  
            System.out.println("**** Load data") ;
            FileManager.get().readModel(model, "D.ttl") ;
            ((PGraphBase)graph).sync(true) ;
            System.out.println("Size = "+model.size()) ;
        }
        query("SELECT * { ?s ?p ?o }", model) ;
        model.close() ;
        
        System.exit(0) ;
        
    }
    
    private static void query(String str, Model model)
    {
        System.out.println(str) ; 
        Query q = QueryFactory.create(str, Syntax.syntaxARQ) ;
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