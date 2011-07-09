/*
 * (c) Copyright 2009 Talis Systems Ltd
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.lib;

import java.io.PrintStream ;
import java.util.Arrays ;
import java.util.HashSet ;
import java.util.Iterator ;
import java.util.Set ;

import org.openjena.atlas.io.IndentedWriter ;
import org.openjena.atlas.lib.ByteBufferLib ;
import org.openjena.atlas.lib.Pair ;
import org.openjena.atlas.lib.Tuple ;
import arq.cmd.CmdException ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.index.TupleTable ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTree ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTable ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.DatasetPrefixesTDB ;
import com.hp.hpl.jena.tdb.store.NodeId ;

public class DumpOps
{
    public static void dump(Dataset ds)
    {
        DatasetGraphTDB dsg = (DatasetGraphTDB)(ds.asDatasetGraph()) ;

        NodeTupleTable nodeTupleTableTriples = dsg.getTripleTable().getNodeTupleTable() ;
        NodeTupleTable nodeTupleTableQuads = dsg.getQuadTable().getNodeTupleTable() ;

        if ( nodeTupleTableTriples.getNodeTable() != nodeTupleTableQuads.getNodeTable() )
            throw new CmdException("Different node tables for triples and quads") ;

        NodeTable nodeTable = nodeTupleTableTriples.getNodeTable() ;
        // V special.
        Set<NodeTable> dumpedNodeTables = new HashSet<NodeTable> () ;



        if ( true )
        {
            System.out.print("## Node Table\n") ;
            dumpNodeTable(nodeTupleTableTriples.getNodeTable(), dumpedNodeTables) ;
            dumpNodeTable(nodeTupleTableQuads.getNodeTable(), dumpedNodeTables) ;
        }

        if ( false )
        {
            System.out.print("## Triple Table\n") ;
            dumpNodeTupleTable(nodeTupleTableTriples.getTupleTable()) ;
            System.out.print("## Quad Table\n") ;
            dumpNodeTupleTable(nodeTupleTableQuads.getTupleTable()) ;
        }

        // Indexes.
        if ( true )
        {
            dumpTupleIndexes(nodeTupleTableTriples.getTupleTable().getIndexes()) ;
            dumpTupleIndexes(nodeTupleTableQuads.getTupleTable().getIndexes()) ;
        }

        // Prefixes
        if ( true )
        {
            System.out.print("## Prefix Table\n") ;  
            DatasetPrefixesTDB prefixes = dsg.getPrefixes() ;

            NodeTupleTable pntt = prefixes.getNodeTupleTable() ;
            if ( ! dumpedNodeTables.contains(pntt.getNodeTable()))
            {
                dumpNodeTable(pntt.getNodeTable(), dumpedNodeTables) ;
                dumpedNodeTables.add(pntt.getNodeTable()) ;
            }
            dumpTupleIndexes(prefixes.getNodeTupleTable().getTupleTable().getIndexes()) ;
        }
    }

    public static void dumpNodeTable(NodeTable nodeTable, Set<NodeTable> dumpedNodeTables)
    {
        if ( dumpedNodeTables.contains(nodeTable) )
            return ;

        Iterator<Pair<NodeId, Node>> iter = nodeTable.all() ;
        for ( ; iter.hasNext() ; )
        {
            Pair<NodeId, Node> pair = iter.next() ;
            NodeId nid = pair.car() ;
            Node n = pair.cdr();
            String x = NodeFmtLib.serialize(n) ;
            System.out.printf("%016X %s\n", nid.getId(), x) ; 
        }
        dumpedNodeTables.add(nodeTable) ;
    }

    public static void dumpTupleIndexes(TupleIndex[] tupleIndexes)
    {
        for ( TupleIndex tIdx : tupleIndexes )
            dumpTupleIndex(tIdx) ;
    }

    public static void dumpTupleIndex(TupleIndex tIdx)
    {
        System.out.print("## "+tIdx.getLabel()+"\n") ;
        Iterator<Tuple<NodeId>> iter = tIdx.all() ; 
        for ( ; iter.hasNext() ; )
        {
            Tuple<NodeId> t = iter.next() ;
            System.out.print(t) ;
            System.out.print("\n") ;
        }
    }
    
    public static void dumpBlockMgr(PrintStream out, BlockMgr blkMgr)
    {
        try {
            for ( int id = 0 ; id < 9999999 ; id++)
            {
                if ( ! blkMgr.valid(id) ) break ;
                Block blk = blkMgr.getRead(id) ;
                out.print("id="+blk.getId()+"  ") ;
                ByteBufferLib.print(out, blk.getByteBuffer()) ;
            }
        } catch (Exception ex) { 
            ex.printStackTrace() ;
        }
    }
    
    public static void dumpBPlusTree(PrintStream out, BPlusTree bpt)
    {
        IndentedWriter iw = new IndentedWriter(out) ;
        bpt.dump(iw) ;
    }
    
    
    public static void dumpBPlusTreeBlocks(BPlusTree bpt)
    {
        System.out.println("Data blocks");
        DumpOps.dumpBlockMgr(System.out, bpt.getRecordsMgr().getBlockMgr()) ;
        System.out.println("Node blocks");
        DumpOps.dumpBlockMgr(System.out, bpt.getRecordsMgr().getBlockMgr()) ;
    }    


    public static void dumpNodeTupleTable(TupleTable tupleTable)
    {
        int N = tupleTable.getTupleLen() ;
        NodeId[] nodeIds = new NodeId[N] ;
        Arrays.fill(nodeIds, NodeId.NodeIdAny) ;
        
        Tuple<NodeId> t = Tuple.create(nodeIds) ;

        Iterator<Tuple<NodeId>> iter = tupleTable.find(t) ;
        for ( ; iter.hasNext() ; )
        {
            Tuple<NodeId> tuple = iter.next() ;
            System.out.print(tuple) ;
            System.out.print("\n") ;
        }
    }
}

/*
 * (c) Copyright 2009 Talis Systems Ltd
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