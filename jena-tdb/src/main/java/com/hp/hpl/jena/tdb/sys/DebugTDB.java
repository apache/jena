/**
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

import java.util.Iterator ;

import org.apache.jena.atlas.lib.Tuple ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.index.Index ;
import com.hp.hpl.jena.tdb.index.RangeIndex ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTableLib ;
import com.hp.hpl.jena.tdb.store.tupletable.TupleIndex ;
import com.hp.hpl.jena.tdb.store.tupletable.TupleIndexRecord ;

/** Lowlevel utilities for working with TDB */

public class DebugTDB
{
    public static NodeId lookup(DatasetGraphTDB dsg, Node n)
    {
        NodeTable nt = dsg.getTripleTable().getNodeTupleTable().getNodeTable() ;
        NodeId nid = nt.getNodeIdForNode(n) ;
        return nid ;
    }
    
    public static void dumpInternals(DatasetGraphTDB dsg, boolean includeNamedGraphs)
    {
        dumpNodeTable("Nodes", dsg) ;
        TupleIndex[] indexes1 = dsg.getTripleTable().getNodeTupleTable().getTupleTable().getIndexes() ;
        TupleIndex[] indexes2 = dsg.getQuadTable().getNodeTupleTable().getTupleTable().getIndexes() ;
        for ( TupleIndex idx : indexes1 )
        {
            System.out.println(idx.getName()) ;
            dumpIndex(idx) ;
        }
        
        if ( ! includeNamedGraphs ) return ;
        
        for ( TupleIndex idx : indexes2 )
        {
            System.out.println(idx.getName()) ;
            dumpIndex(idx) ;
        }
    }
    
    public static void dumpNodeTable(String label, DatasetGraphTDB dsg)
    {
        NodeTable nt1 = dsg.getTripleTable().getNodeTupleTable().getNodeTable() ;
        NodeTableLib.print(label, nt1) ;
    }
    
//    public static RangeIndex makeRangeIndex(Location location, String indexName, 
//                                            int dftKeyLength, int dftValueLength,
//                                            int readCacheSize,int writeCacheSize)
    
    public static void dumpNodeIndex(String dir)
    {
        Location location = new Location(dir) ;
        Index nodeToId = SetupTDB.makeIndex(location, Names.indexNode2Id, SystemTDB.BlockSize, SystemTDB.LenNodeHash, SystemTDB.SizeOfNodeId, -1 ,-1) ;
        for ( Record aNodeToId : nodeToId )
        {
            System.out.println( aNodeToId );
        }
    }
    
    
    public static TupleIndex getIndex(String idxName, DatasetGraphTDB dsg)
    {
        System.out.println(idxName) ;
        TupleIndex[] indexes1 = dsg.getTripleTable().getNodeTupleTable().getTupleTable().getIndexes() ;
        TupleIndex[] indexes2 = dsg.getQuadTable().getNodeTupleTable().getTupleTable().getIndexes() ;
        TupleIndex idx = null ; 
        
        for ( TupleIndex i : indexes1 )
        {
            if ( i.getName().equals(idxName) )
                return i ;
        }
        
        for ( TupleIndex i : indexes2 )
        {
            if ( i.getName().equals(idxName) )
                return i ;
        }
        return null ;
    }

    public static void dumpIndex(TupleIndex idx)
    {
        Iterator<Tuple<NodeId>> iter = idx.all() ;
        while(iter.hasNext())
        {
            Tuple<NodeId> tuple = iter.next() ;
            System.out.println(tuple) ;
        }
        
        if ( false )
        {
            // Dump raw
            TupleIndexRecord tir = (TupleIndexRecord)idx ;
            RangeIndex rIdx = tir.getRangeIndex() ;
            for ( Record aRIdx : rIdx )
            {
                System.out.println( aRIdx );
            }
        }
    }

}

