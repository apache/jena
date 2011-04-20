/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package tx.transaction;

import java.nio.ByteBuffer ;
import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Map ;
import java.util.Properties ;

import org.openjena.atlas.lib.ByteBufferLib ;
import org.openjena.atlas.lib.NotImplemented ;
import org.openjena.atlas.lib.Pair ;
import setup.BlockMgrBuilder ;
import setup.DatasetBuilder ;
import setup.DatasetBuilderStd ;
import setup.DatasetBuilderStd.BlockMgrBuilderStd ;
import setup.IndexBuilder ;
import setup.NodeTableBuilder ;
import setup.ObjectFileBuilder ;
import setup.RangeIndexBuilder ;
import setup.TupleIndexBuilder ;
import tx.DatasetGraphTxView ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetPrefixStorage ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderTransformation ;
import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.block.BlockMgrWrapper ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.nodetable.NodeTableWrapper ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.QuadTable ;
import com.hp.hpl.jena.tdb.store.TripleTable ;
import com.hp.hpl.jena.tdb.sys.ConcurrencyPolicy ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class TransactionManager
{
    // Setup.
    // Fragile.
    BlockMgrBuilderRemember baseBlockMgrBuilder ;
    NodeTable nodeTable ;
    //Location location = new Location("dummy") ;
    static long transactionId = 10 ;
    
    public DatasetGraphTDB build(Location location)
    {
        DatasetBuilderTxnBase x = new DatasetBuilderTxnBase() ;
        x.setStd() ;
        DatasetGraphTDB dsg = x.build(location, null) ;
        baseBlockMgrBuilder = x.blockMgrBuilder1 ;
        nodeTable = dsg.getTripleTable().getNodeTupleTable().getNodeTable() ;
        return dsg ;
    }
    
    public DatasetGraphTxView begin(DatasetGraph dsg)
    {
        // If already a transaction ... 
        // Subs transactions are a new view - commit is only comit to parent transaction.  
        if ( dsg instanceof DatasetGraphTxView )
        {
            System.err.println("Already in transactional DatasetGraph") ;
            // Either:
            //   error -> implies nested
            //   create new transaction 
        }
        DatasetBuilder x = new DatasetBuilderTxn(baseBlockMgrBuilder, nodeTable) ;
        DatasetGraph dsg2 = x.build(Location.mem(), null) ;
        
        Transaction txn = new Transaction(transactionId++, this) ;
        return new DatasetGraphTxView(txn, dsg2) ;
    }
    
    public void commit(Transaction transaction)
    {
        System.err.println("Commit") ;
    }

    public void abort(Transaction transaction)
    {
        System.err.println("Abort") ;

    }
    
    static class DatasetBuilderTxnBase extends DatasetBuilderStd
    {
        // Need block manager below.
        BlockMgrBuilderRemember blockMgrBuilder1         = new BlockMgrBuilderRemember(SystemTDB.BlockSize) ;
        
        DatasetBuilderTxnBase() {}
        
        @Override
        protected void setStd()
        {
            ObjectFileBuilder objectFileBuilder     = new ObjectFileBuilderStd() ;
            //BlockMgrBuilder blockMgrBuilder         = new BlockMgrBuilderStd(SystemTDB.BlockSize) ;
            
            BlockMgrBuilder blockMgrBuilder = blockMgrBuilder1 ;
            
            IndexBuilder indexBuilder               = new IndexBuilderStd(blockMgrBuilder, blockMgrBuilder) ;
            RangeIndexBuilder rangeIndexBuilder     = new RangeIndexBuilderStd(blockMgrBuilder, blockMgrBuilder) ;
            
            NodeTableBuilder nodeTableBuilder       = new NodeTableBuilderStd(indexBuilder, objectFileBuilder) ;
            TupleIndexBuilder tupleIndexBuilder     = new TupleIndexBuilderStd(rangeIndexBuilder) ;
            
            set(nodeTableBuilder, tupleIndexBuilder, 
                indexBuilder, rangeIndexBuilder, 
                blockMgrBuilder, objectFileBuilder) ;
        }

    }
    
    static class DatasetBuilderTxn extends DatasetBuilderStd
    {
        
        
        
        private BlockMgrBuilderRemember blockMgrBuilderRemember ;
        private NodeTableBuilder nodeTableBuilder ; 

        DatasetBuilderTxn(BlockMgrBuilderRemember blockMgrBuilderRemember, NodeTable nodeTable)
        {
            super() ;
            this.blockMgrBuilderRemember = blockMgrBuilderRemember ;
            
            
            ObjectFileBuilder objectFileBuilder     = new ObjectFileBuilderStd() ;
            
            BlockMgrBuilder blockMgrBuilder2         = new BlockMgrBuilderTxn(blockMgrBuilderRemember) ;
            
            IndexBuilder indexBuilder               = new IndexBuilderStd(blockMgrBuilder2, blockMgrBuilder2) ;
            RangeIndexBuilder rangeIndexBuilder     = new RangeIndexBuilderStd(blockMgrBuilder2, blockMgrBuilder2) ;
            
            NodeTableBuilder nodeTableBuilder       = new NodeTableBuilderTxn(nodeTable) ;
            this.nodeTableBuilder = nodeTableBuilder ;
            
            TupleIndexBuilder tupleIndexBuilder     = new TupleIndexBuilderStd(rangeIndexBuilder) ;
            
            set(nodeTableBuilder, tupleIndexBuilder, 
                indexBuilder, rangeIndexBuilder, 
                blockMgrBuilder2, objectFileBuilder) ;
        }
        
        @Override
        public DatasetGraphTDB build(Location location, Properties config)
        {
            NodeTable nodeTable = nodeTableBuilder.buildNodeTable(null, null, 0, 0) ;
            
            ConcurrencyPolicy policy = createConcurrencyPolicy() ;
            TripleTable tripleTable = makeTripleTable(location, nodeTable, policy) ; 
            QuadTable quadTable = makeQuadTable(location, nodeTable, policy) ;
            DatasetPrefixStorage prefixes = makePrefixTable(location, policy) ;
            ReorderTransformation transform  = chooseReorderTransformation(location) ;
            DatasetGraphTDB dsg = new DatasetGraphTDB(tripleTable, quadTable, prefixes, transform, location, config) ;
            return dsg ;
        }
    }
    
    static class BlockMgrBuilderTxn implements BlockMgrBuilder
    {

        private BlockMgrBuilderRemember baseBlockMgr ;

        public BlockMgrBuilderTxn(BlockMgrBuilderRemember blockMgr)
        {
            this.baseBlockMgr = blockMgr ;
        }

        public BlockMgr buildBlockMgr(FileSet fileSet, String ext)
        {
            BlockMgr base = baseBlockMgr.created(fileSet, ext) ;
            return new BlockMgrTxn(base) ;
        }
    }
    
    static class BlockMgrTxn extends BlockMgrWrapper
    {
        Map<Integer, ByteBuffer> blocks = new HashMap<Integer, ByteBuffer>() ;
        
        public BlockMgrTxn(BlockMgr blockMgr)
        {
            super(blockMgr) ;
        }
        
        // Create new blocks here in-memory
        
        @Override
        public int allocateId()
        {
            return blockMgr.allocateId() ;
        }

        @Override
        public ByteBuffer allocateBuffer(int id)
        {
            ByteBuffer bb = ByteBuffer.allocate(blockMgr.blockSize()) ;
            blocks.put(id, bb) ;
            return bb ;
            //return blockMgr.allocateBuffer(id) ;
        }
     
        
        @Override
        public ByteBuffer get(int id)
        {
            ByteBuffer bb = blocks.get(id) ;
            if ( bb != null )
                return bb ;
            // Until we track read and write gets, need to copy for safety
            // INEFFCIEINT
            bb = super.get(id) ;
            bb = ByteBufferLib.duplicate(bb) ;
            blocks.put(id, bb) ;
            return bb ;
        }

        @Override
        public void put(int id, ByteBuffer block)
        {
            ByteBuffer bb = blocks.get(id) ;
            if ( bb != block )
                System.err.println("Odd!") ;
            blocks.put(id, block) ;
        }

        @Override
        public void freeBlock(int id)
        {
            blocks.remove(id) ;
        }
    }
    
    static class BlockMgrBuilderRemember implements BlockMgrBuilder
    {

        private Map<String, BlockMgr> created = new  HashMap<String, BlockMgr>() ;
        BlockMgrBuilderStd bmb ;
        
        public BlockMgrBuilderRemember(int blockSize)
        {
            bmb = new BlockMgrBuilderStd(blockSize) ;
        }

        public BlockMgr buildBlockMgr(FileSet fileSet, String ext)
        {
            String key = fileSet.getBasename()+"."+ext ;
            BlockMgr blkMgr = bmb.buildBlockMgr(fileSet, ext) ;
            created.put(key, blkMgr) ;
            return blkMgr ;
        }
        
        public BlockMgr created(FileSet fileSet, String ext)
        {
            String key = fileSet.getBasename()+"."+ext ;
            return created.get(key) ;
        }
        
    }
    
    static class NodeTableBuilderTxn implements NodeTableBuilder
    {
        
        
        private NodeTable other ;


        NodeTableBuilderTxn(NodeTable other)
        {
            this.other = other ;
        }
        
        
        public NodeTable buildNodeTable(FileSet fsIndex, FileSet fsObjectFile, int sizeNode2NodeIdCache,
                                        int sizeNodeId2NodeCache)
        {
            return new NodeTableTxn(other) ;
        }
    }
    
    static class NodeTableTxn extends NodeTableWrapper
    {
        
        Map<NodeId, Node> nodeIdToNode = new HashMap<NodeId, Node>() ;
        Map<Node, NodeId> nodeToNodeId = new HashMap<Node, NodeId>() ;
        // Well side practical space.
        long alloc = 0x888888888L ;

        public NodeTableTxn(NodeTable other)
        {
            super(other) ;
        }

        @Override
        public NodeId getAllocateNodeId(Node node)
        {
            NodeId nodeId = getNodeIdForNode(node) ;
            if ( nodeId != NodeId.NodeDoesNotExist )
                return nodeId ;
            // Need to create.
            // HACK
            nodeId = new NodeId(alloc) ;  
            alloc++ ;
            nodeIdToNode.put(nodeId, node) ;
            nodeToNodeId.put(node, nodeId) ;
            return nodeId ;
        }

        @Override
        public NodeId getNodeIdForNode(Node node)
        {
            NodeId nodeId = nodeToNodeId.get(node) ;
            if ( nodeId != null )
                return nodeId ;
            return super.getNodeIdForNode(node) ;
        }

        @Override
        public Node getNodeForNodeId(NodeId id)
        {
            Node n = nodeIdToNode.get(id) ;
            if ( n != null )
                return n ;
            return super.getNodeForNodeId(id) ;
        }

        @Override
        public Iterator<Pair<NodeId, Node>> all()
        {
            List<Pair<NodeId, Node>> here = new ArrayList<Pair<NodeId, Node>>() ;
            for ( Map.Entry<NodeId, Node> e : nodeIdToNode.entrySet() )
            {
                here.add(new Pair<NodeId, Node>(e.getKey(), e.getValue())) ;
            }
            //Iter<T>.concat(here.iterator(), super.all()) ;
            return here.iterator() ;
        }
        
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