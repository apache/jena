/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010 Epimorphics Ltd.
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.util.ArrayList ;
import java.util.Collection ;
import java.util.List ;
import java.util.concurrent.ExecutorService ;
import java.util.concurrent.Executors ;
import java.util.concurrent.TimeUnit ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.lib.Bytes ;
import org.openjena.atlas.lib.Lib ;
import org.openjena.atlas.logging.Log ;
import org.openjena.riot.RiotLoader ;
import org.openjena.riot.RiotReader ;
import org.openjena.riot.RiotWriter ;
import setup.DatasetBuilderStd ;
import setup.NoisyBlockMgr ;
import setup.ObjectFileBuilder ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
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
import com.hp.hpl.jena.tdb.migrate.GraphDynamicUnion ;
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
        DatasetGraph dsg = RiotLoader.load("D.trig") ;
        
        List<Node> gn = new ArrayList<Node>() ;
        gn.add(Node.createURI("http://example/g1")) ;
        gn.add(Node.createURI("http://example/g2")) ;
        //gn.add(Node.createURI("http://example/g3")) ;
        
        Graph g = new GraphDynamicUnion(dsg, gn) ;
        Model model = ModelFactory.createModelForGraph(g) ;
        RiotWriter.writeTriples(System.out, g) ;
        exit(0) ;
        
        //g.find(Node.createURI(""), null,null) ;
        
        
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