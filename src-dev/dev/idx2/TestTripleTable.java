/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.idx2;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import test.BaseTest;

import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.index.IndexBuilder;
import com.hp.hpl.jena.tdb.index.RangeIndex;
import com.hp.hpl.jena.tdb.pgraph.GraphTDB;
import com.hp.hpl.jena.tdb.pgraph.NodeTable;
import com.hp.hpl.jena.tdb.pgraph.NodeTableIndex;

public class TestTripleTable extends BaseTest
{
    static {
        Logger.getLogger("com.hp.hpl.jena.tdb.info").setLevel(Level.WARN) ;
        Logger.getLogger("com.hp.hpl.jena.tdb.exec").setLevel(Level.WARN) ;
    }
    
    static RecordFactory factory = GraphTDB.indexRecordFactory ;
    
    // Move to TDBFactoryGraph
    
    private static TripleTable2 create()
    {
        
        TupleIndex spo = createIndex("SPO") ;
        TupleIndex pos = createIndex("POS") ;
        TupleIndex osp = createIndex("OSP") ;
        TupleIndex indexes[] = { spo, pos, osp } ;
        
        NodeTable nodeTable = new NodeTableIndex(IndexBuilder.mem()) ;
        return new TripleTable2(indexes, nodeTable, factory, null) ;
    }

    private static TupleIndex createIndex(String desc)
    {
        RangeIndex rIdx1 = IndexBuilder.mem().newRangeIndex(null, factory, desc) ;
        TupleIndex tupleIndex = new TupleIndex(3, new ColumnMap("SPO", desc), factory, rIdx1) ; 
        return tupleIndex ;
    }
    
    @Test public void createTripleTable() { create() ; }
    
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