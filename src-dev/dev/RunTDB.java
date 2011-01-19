/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010 Epimorphics Ltd.
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package dev;

import org.openjena.atlas.lib.Bytes ;
import org.openjena.atlas.lib.FileOps ;
import org.openjena.atlas.logging.Log ;
import org.openjena.riot.RiotLoader ;
import setup.DatasetBuilder ;
import setup.DatasetBuilderStd ;
import setup.NoisyBlockMgr ;
import setup.ObjectFileBuilder ;

import com.hp.hpl.jena.rdf.model.RDFList.ReduceFn ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.IndexBuilder ;
import com.hp.hpl.jena.tdb.index.bplustree.BPTreeNode ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTree ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeRewriter ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
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
        // Are leaf blocks properly packed? 
        
        tdb.tdbdump.main("--loc=DB") ;
        System.exit(0) ;
        
        
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
        
        
        // New setup
        FileOps.clearDirectory("DB1") ;
        
        DatasetBuilder builder = new DSB2() ;
        DatasetGraphTDB dsg = builder.build(new Location("DB1"), null) ;
        
        //DatasetGraphTDB dsg = DatasetBuilderStd.build(new Location("DB1")) ;

        
        RiotLoader.read("D.nq", dsg) ;
        dsg.sync() ;
        
        TDB.closedown() ;
        System.out.println("DONE") ;
        System.exit(0) ;
        
        
        tdb.tdbstats.main("--loc=DB", "--graph=urn:x-arq:UnionGraph") ; System.exit(0) ;
        
        tdb.tdbquery.main("--set=tdb:logExec=true", 
                          "--set=tdb:unionDefaultGraph=true", 
                          "--query=Q.rq") ;
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