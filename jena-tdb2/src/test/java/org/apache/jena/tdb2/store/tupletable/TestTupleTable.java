/*
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

package org.apache.jena.tdb2.store.tupletable;

import static org.apache.jena.atlas.lib.tuple.TupleFactory.tuple;
import static org.apache.jena.tdb2.store.tupletable.NData.n1;
import static org.apache.jena.tdb2.store.tupletable.NData.n2;
import static org.apache.jena.tdb2.store.tupletable.NData.n3;
import static org.apache.jena.tdb2.store.tupletable.NData.n4;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.dboe.base.record.RecordFactory;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.sys.SystemTDB;

public class TestTupleTable
{
    static RecordFactory factory = new RecordFactory(3*SystemTDB.SizeOfNodeId, 0);

    static private TupleTable create()
    {
        TupleIndex idxSPO = TestTupleIndexRecordDirect.create("SPO");
        TupleIndex idxPOS = TestTupleIndexRecordDirect.create("POS");
        TupleIndex idxOSP = TestTupleIndexRecordDirect.create("OSP");
        TupleIndex x[] = { idxSPO, idxPOS, idxOSP };
        TupleTable table = new TupleTable(3, x);
        return table;
    }

    static private TupleTable create2()
    {
        TupleIndex idxSPO = TestTupleIndexRecordDirect.create("SPO");
        TupleIndex x[] = { idxSPO };
        TupleTable table = new TupleTable(3, x);
        return table;
    }

    static void add(TupleTable table, NodeId x1, NodeId x2, NodeId x3)
    {
        Tuple<NodeId> tuple = tuple(x1, x2, x3);
        table.add(tuple);
    }

    @Test public void create1() { create(); }

    @Test public void createFind1()
    {
        TupleTable table = create();
        add(table, n1, n2, n3);
        // Cast removes compile lint warning.
        Tuple<NodeId> pat = tuple((NodeId)null, null, null);
        Iterator<Tuple<NodeId>> iter = table.find(pat);
        List<Tuple<NodeId>> x = Iter.toList(iter);
        int z = x.size();
        assertEquals(1, z);
        Tuple<NodeId> e1 = x.get(0);
        assertEquals(tuple(n1, n2, n3) , e1);
    }

    @Test public void createFind2()
    {
        TupleTable table = create();
        add(table, n1, n2, n3);
        add(table, n1, n2, n4);

        Tuple<NodeId> pat = tuple(null, n2, null);
        Iterator<Tuple<NodeId>> iter = table.find(pat);
        assertNotNull(iter);
        List<Tuple<NodeId>> x = Iter.toList(iter);
        int z = x.size();
        assertEquals(2, z);

        Tuple<NodeId> e1 = x.get(0);
        Tuple<NodeId> e2 = x.get(1);
        assertEquals(tuple(n1, n2, n3) , e1);
        assertEquals(tuple(n1, n2, n4) , e2);
    }

    @Test public void createFind3()
    {
        // test scan
        TupleTable table = create2();
        add(table, n1, n2, n3);
        add(table, n1, n2, n4);

        Tuple<NodeId> pat = tuple(n1, null, n3);
        Iterator<Tuple<NodeId>> iter = table.find(pat);
        assertNotNull(iter);
        List<Tuple<NodeId>> x = Iter.toList(iter);
        int z = x.size();
        assertEquals(1, z);

        Tuple<NodeId> e1 = x.get(0);
        assertEquals(tuple(n1, n2, n3) , e1);
    }

    @Test public void createFind4()
    {
        // test scan
        TupleTable table = create2();
        add(table, n1, n2, n3);
        add(table, n1, n2, n4);

        Tuple<NodeId> pat = tuple(null, null, n3);
        Iterator<Tuple<NodeId>> iter = table.find(pat);
        assertNotNull(iter);
        List<Tuple<NodeId>> x = Iter.toList(iter);
        int z = x.size();
        assertEquals(1, z);

        Tuple<NodeId> e1 = x.get(0);
        assertEquals(tuple(n1, n2, n3) , e1);
    }

    @Test public void createFind5()
    {
        // test scan
        TupleTable table = create2();
        add(table, n1, n2, n3);
        add(table, n1, n2, n4);

        Tuple<NodeId> pat = tuple(null, NodeId.NodeIdAny, n3);
        Iterator<Tuple<NodeId>> iter = table.find(pat);
        assertNotNull(iter);
        List<Tuple<NodeId>> x = Iter.toList(iter);
        int z = x.size();
        assertEquals(1, z);

        Tuple<NodeId> e1 = x.get(0);
        assertEquals(tuple(n1, n2, n3) , e1);
    }

    @Test public void selectIndex1() {
        TupleTable table = create();

        // With no index type constraint should return an index for S, P and O but not G as test table doesn't have that
        // index
        TupleIndex byS = table.selectIndex("S");
        assertNotNull(byS);
        TupleIndex byP = table.selectIndex("P");
        assertNotNull(byP);
        TupleIndex byO = table.selectIndex("O");
        assertNotNull(byO);
        TupleIndex byG = table.selectIndex("G");
        assertNull(byG);

        // We're using TupleIndexRecord as the index type so this should select indexes for everything but G
        TupleIndexRecord recordByS = table.selectIndex("S", TupleIndexRecord.class);
        assertNotNull(recordByS);
        TupleIndexRecord recordByP = table.selectIndex("P", TupleIndexRecord.class);
        assertNotNull(recordByP);
        TupleIndexRecord recordByO = table.selectIndex("O", TupleIndexRecord.class);
        assertNotNull(recordByO);
        TupleIndexRecord recordByG = table.selectIndex("G", TupleIndexRecord.class);
        assertNull(recordByG);

        // Using the wrong type constraint should select no index for anything
        TupleIndexWrapper wrapperByS = table.selectIndex("S", TupleIndexWrapper.class);
        assertNull(wrapperByS);
        TupleIndexWrapper wrapperByP = table.selectIndex("P", TupleIndexWrapper.class);
        assertNull(wrapperByP);
        TupleIndexWrapper wrapperByO = table.selectIndex("O", TupleIndexWrapper.class);
        assertNull(wrapperByO);
        TupleIndexWrapper wrapperByG = table.selectIndex("G", TupleIndexWrapper.class);
        assertNull(wrapperByG);
    }

}
