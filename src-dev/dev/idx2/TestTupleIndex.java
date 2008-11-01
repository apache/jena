/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.idx2;

import java.util.Iterator;

import lib.Tuple;
import org.junit.Test;
import tdb.Cmd;
import test.BaseTest;

import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.index.Descriptor;
import com.hp.hpl.jena.tdb.index.IndexBuilder;
import com.hp.hpl.jena.tdb.index.RangeIndex;
import com.hp.hpl.jena.tdb.pgraph.NodeId;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

public class TestTupleIndex extends BaseTest
{
    static { Cmd.setLog4j() ; }
    
    static RecordFactory factory = new RecordFactory(3*SystemTDB.SizeOfNodeId, 0) ;
    static NodeId n1 = new NodeId(1) ;
    static NodeId n2 = new NodeId(2) ;
    static NodeId n3 = new NodeId(3) ;
    static NodeId n4 = new NodeId(0x4040404040404040L) ;
    static NodeId n5 = new NodeId(0x5555555555555555L) ;
    static NodeId n6 = new NodeId(0x6666666666666666L) ; 
    
    static TupleIndex create(String description)
    {
        RangeIndex rIdx = IndexBuilder.mem().newRangeIndex(null, factory, "TupleIndexTest") ;
        TupleIndex index = new TupleIndex(3, new Descriptor(description, factory), rIdx) ;
        return index ;
    }
    
    @Test public void tupleIndex_1()
    {
        Tuple<NodeId> tuple = new Tuple<NodeId>(n1, n2, n3) ;
        RangeIndex rIdx = IndexBuilder.mem().newRangeIndex(null, factory, "TupleIndexTest") ;
        TupleIndex index = create("SPO") ;
        index.add(tuple) ;
    }
    
    @Test public void tupleIndexFind_1()
    {
        Tuple<NodeId> tuple = new Tuple<NodeId>(n1, n2, n3) ;
        RangeIndex rIdx = IndexBuilder.mem().newRangeIndex(null, factory, "TupleIndexTest") ;
        TupleIndex index = create("SPO") ;
        index.add(tuple) ;
        
        Tuple<NodeId> tuple2 = new Tuple<NodeId>(n1, n2, n3) ;
        Iterator<Tuple<NodeId>> iter = index.find(tuple2) ;
        assertTrue(iter.hasNext()) ;
        iter.next();
        assertFalse(iter.hasNext()) ;
    }
 
    @Test public void tupleIndexFind_2()
    {
        Tuple<NodeId> tuple = new Tuple<NodeId>(n1, n2, n3) ;
        RangeIndex rIdx = IndexBuilder.mem().newRangeIndex(null, factory, "TupleIndexTest") ;
        TupleIndex index = create("SPO") ;
        index.add(tuple) ;
        
        Tuple<NodeId> tuple2 = new Tuple<NodeId>(n4, n5, n6) ;
        Iterator<Tuple<NodeId>> iter = index.find(tuple2) ;
        assertFalse(iter.hasNext()) ;
   }
 
    
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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