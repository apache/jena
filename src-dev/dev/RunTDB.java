/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010 Epimorphics Ltd.
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.util.concurrent.ExecutorService ;
import java.util.concurrent.Executors ;
import java.util.concurrent.TimeUnit ;

import org.openjena.atlas.lib.Bytes ;
import org.openjena.atlas.lib.Lib ;
import org.openjena.atlas.logging.Log ;
import org.openjena.riot.RiotLoader ;
import setup.DatasetBuilderStd ;
import setup.NoisyBlockMgr ;
import setup.ObjectFileBuilder ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.block.FileMode ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTree ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class RunTDB
{
    static { Log.setLog4j() ; }
    static String divider = "----------" ;
    static String nextDivider = null ;
    static void divider()
    {
        if ( nextDivider != null )
            System.out.println(nextDivider) ;
        nextDivider = divider ;
    }
    
    static void exit(int rc)
    {
        System.out.println("EXIT") ;
        System.exit(rc) ;
    }
    

    public static class DSB2 extends DatasetBuilderStd
    {
        public DSB2()
        {
            super() ;
            ObjectFileBuilder objectFileBuilder         = new ObjectFileBuilderStd() ;
            BlockMgrBuilderStd blockMgrBuilder          = new BlockMgrBuilderStd(SystemTDB.BlockSize)
            {
                @Override
                public BlockMgr buildBlockMgr(FileSet fileset, String name)
                {
                    BlockMgr bMgr = super.buildBlockMgr(fileset, name) ;
                    return new NoisyBlockMgr(bMgr) ;
                }
            } ;
            
            IndexBuilderStd indexBuilder                = new IndexBuilderStd(blockMgrBuilder, blockMgrBuilder) ;
            RangeIndexBuilderStd rangeIndexBuilder      = new RangeIndexBuilderStd(blockMgrBuilder, blockMgrBuilder) ;
            
            NodeTableBuilderStd nodeTableBuilder        = new NodeTableBuilderStd(indexBuilder, objectFileBuilder) ;
            TupleIndexBuilderStd tupleIndexBuilder      = new TupleIndexBuilderStd(rangeIndexBuilder) ;
            set(nodeTableBuilder, tupleIndexBuilder, indexBuilder, rangeIndexBuilder, blockMgrBuilder, objectFileBuilder) ;
        }
    }
    
    public static void main(String[] args) throws Exception
    { 
        {
        DatasetGraph dsg = TDBFactory.createDatasetGraph("DB") ;
        dsg.close() ;
        dsg = TDBFactory.createDatasetGraph("DB") ;
        dsg.close() ;
        exit(0) ;
        }
        SystemTDB.setFileMode(FileMode.direct) ;
        tdb.tdbupdate.main("--loc=DBU", "--file=update.ru") ; exit(0) ;
        
        final Dataset ds = TDBFactory.createDataset() ;
        final DatasetGraph dsg = ds.asDatasetGraph() ;
        final Quad quad = SSE.parseQuad("(<g> <y> <p> 99)") ;
         
        
        RiotLoader.read("/home/afs/Datasets/MusicBrainz/tracks-1k.nt", ds.asDatasetGraph()) ;
        
        ExecutorService exec = Executors.newFixedThreadPool(20) ;
        
        Runnable reader = new Runnable() {
            public void run()
            {
                Query query = QueryFactory.create("SELECT * { <http://musicbrainz.org/mm-2.1/track/ee0d27be-0a8c-4002-bcdc-9c0937c1bb3e> ?p ?o }") ;
                QueryExecution qExec = QueryExecutionFactory.create(query, ds) ;
                ResultSet rs = qExec.execSelect() ;
                while(rs.hasNext())
                {
                    Lib.sleep(20) ;
                    QuerySolution qs = rs.next();
                    System.out.println(qs) ;
                }
            }
        } ;
        
        Runnable writer = new Runnable() {
            public void run()
            {
                for ( int i = 0 ; i < 20 ; i++ )
                {
                    dsg.add(quad) ;
                    System.out.println("Add quad") ;
                    Lib.sleep(100) ;
                }
            }
        } ;
        
//        for ( int i = 0 ; i < 1 ; i++ )
//        {
//            exec.execute(reader) ;
//        }

        for ( int i = 0 ; i < 10 ; i++ )
        {
            exec.execute(writer) ;
        }
        
//        exec.shutdown() ;
        exec.awaitTermination(1000, TimeUnit.SECONDS) ;
        //exec.shutdown() ;
        
        System.out.println("DONE");
        System.exit(0) ;
        
            
        
        
        
        String uri = "http://musicbrainz.org/mm-2.1/track/56eb39eb-22e3-4d39-9380-39cd55a97acc" ;
        
        tdb.tdbquery.main("--loc=DB", "SELECT * { <"+uri+"> ?p ?o }" ) ;
        System.exit(0) ;
        
        System.exit(0) ;
        tdb.tdbstats.main("--loc=DB", "--graph=urn:x-arq:UnionGraph") ; System.exit(0) ;
        tdb.tdbquery.main("--set=tdb:logExec=true", 
                          "--set=tdb:unionDefaultGraph=true", 
                          "--query=Q.rq") ;
        System.exit(0) ;
    }
 
    static void bptLeafBlocks()
    {
        // Are leaf blocks properly packed? 
        {   int order = 3 ;
            BPlusTreeParams params = new BPlusTreeParams(order, 4, 0) ;
            System.out.println(params) ;
            params = new BPlusTreeParams(order, 4, 0) ;
            System.out.println(params) ;
        }
        
        int order = 3 ;
        BPlusTree bpt = BPlusTree.makeMem(order, order, 4, 0) ;
        System.out.println(bpt.getParams()) ;
        System.out.println(bpt.getParams().getBlockSize()) ;
        int blkSize = bpt.getParams().getBlockSize() ;
        RecordFactory rf= bpt.getRecordFactory() ;
        
        int N = 10 ;
        for ( int i = 0 ; i < N ; i++ )
        {
            Record r = rf.create() ;
            Bytes.setInt(i, r.getKey()) ;
            bpt.add(r) ;
        }
        
        BPlusTreeParams.CheckingNode = true ;
        BPlusTreeParams.CheckingTree = true ;
        bpt.dump() ;
        
        int[] x = new int[] {0,1,2,3,4,5,6,7,8,9,10} ;
        
        for ( int i : x )
        {
            Record r = rf.create() ;
            Bytes.setInt(i, r.getKey()) ;
            bpt.delete(r) ;
            System.out.println() ;
            bpt.dump() ;
        }
        System.out.println() ;
        bpt.dump() ;
        System.out.println("DONE") ;
        System.exit(0) ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010 Epimorphics Ltd.
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