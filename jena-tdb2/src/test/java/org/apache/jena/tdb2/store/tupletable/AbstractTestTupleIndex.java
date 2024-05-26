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
import static org.apache.jena.tdb2.store.tupletable.NData.*;

import java.util.Iterator;
import java.util.Set;

import org.apache.jena.atlas.iterator.Iter;
import static org.junit.Assert.*;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.tdb2.store.NodeId;
import org.junit.Test;

/** Test TupleIndexes (general) */
public abstract class AbstractTestTupleIndex
{
    protected abstract TupleIndex create(String description);

    protected static void add(TupleIndex index, NodeId x1, NodeId x2, NodeId x3)
    {
        Tuple<NodeId> tuple = tuple(x1, x2, x3);
        index.add(tuple);
    }

    @Test public void TupleIndex_1()
    {
        TupleIndex index = create("SPO");
        add(index, n1, n2, n3);
    }

    @Test public void TupleIndexRecordSPO_1()
    {
        TupleIndex index = create("SPO");
        add(index, n1, n2, n3);

        Tuple<NodeId> tuple2 = tuple(n1, n2, n3);
        Iterator<Tuple<NodeId>> iter = index.find(tuple2);
        assertTrue(iter.hasNext());
        iter.next();
        assertFalse(iter.hasNext());
    }

    @Test public void TupleIndexRecordSPO_2()
    {
        TupleIndex index = create("SPO");
        add(index, n1, n2, n3);

        Tuple<NodeId> tuple2 = tuple(n1, n2, null);
        Iterator<Tuple<NodeId>> iter = index.find(tuple2);
        assertTrue(iter.hasNext());
        iter.next();
        assertFalse(iter.hasNext());
    }

    @Test public void TupleIndexRecordSPO_3()
    {
        TupleIndex index = create("SPO");
        add(index, n1, n2, n3);

        Tuple<NodeId> tuple2 = tuple(n1, null, n3);
        Iterator<Tuple<NodeId>> iter = index.find(tuple2);
        assertTrue(iter.hasNext());
        iter.next();
        assertFalse(iter.hasNext());
    }

    @Test public void TupleIndexRecordSPO_4()
    {
        TupleIndex index = create("SPO");
        add(index, n1, n2, n3);

        Tuple<NodeId> tuple2 = tuple(n1, NodeId.NodeIdAny, NodeId.NodeIdAny);
        Iterator<Tuple<NodeId>> iter = index.find(tuple2);
        assertTrue(iter.hasNext());
        iter.next();
        assertFalse(iter.hasNext());
    }

    @Test public void TupleIndexRecordSPO_5()
    {
        TupleIndex index = create("SPO");
        add(index, n1, n2, n3);
        add(index, n1, n2, n4);

        Tuple<NodeId> tuple2 = tuple(n1, n2, n3);
        Iterator<Tuple<NodeId>> iter = index.find(tuple2);
        Set<Tuple<NodeId>> x = Iter.toSet(iter);
        assertEquals(1, x.size());
        assertTrue(x.contains(tuple(n1, n2, n3)));
        assertFalse(x.contains(tuple(n1, n2, n4)));
    }

    @Test public void TupleIndexRecordSPO_6()
    {
        TupleIndex index = create("SPO");
        add(index, n1, n2, n3);
        add(index, n1, n2, n4);

        Tuple<NodeId> tuple2 = tuple(n1, n2, NodeId.NodeIdAny);
        Iterator<Tuple<NodeId>> iter = index.find(tuple2);
        Set<Tuple<NodeId>> x = Iter.toSet(iter);
        assertEquals(2, x.size());
        assertTrue(x.contains(tuple(n1, n2, n3)));
        assertTrue(x.contains(tuple(n1, n2, n4)));
    }

    @Test public void TupleIndexRecordSPO_7()
    {
        TupleIndex index = create("SPO");
        add(index, n1, n2, n3);
        add(index, n1, n2, n4);

        Tuple<NodeId> tuple2 = tuple(n1, NodeId.NodeIdAny, NodeId.NodeIdAny);
        Iterator<Tuple<NodeId>> iter = index.find(tuple2);
        Set<Tuple<NodeId>> x = Iter.toSet(iter);
        assertEquals(2, x.size());
        assertTrue(x.contains(tuple(n1, n2, n3)));
        assertTrue(x.contains(tuple(n1, n2, n4)));
    }

    @Test public void TupleIndexRecordSPO_8()
    {
        TupleIndex index = create("SPO");
        add(index, n1, n2, n3);
        add(index, n2, n3, n4);

        {
            Tuple<NodeId> tuple2 = tuple(n1, NodeId.NodeIdAny, NodeId.NodeIdAny);
            Iterator<Tuple<NodeId>> iter = index.find(tuple2);
            Set<Tuple<NodeId>> x = Iter.toSet(iter);
            assertEquals(1, x.size());
            assertTrue(x.contains(tuple(n1, n2, n3)));
        }

        {
            Tuple<NodeId> tuple2 = tuple(n2, NodeId.NodeIdAny, NodeId.NodeIdAny);
            Iterator<Tuple<NodeId>> iter = index.find(tuple2);
            Set<Tuple<NodeId>> x = Iter.toSet(iter);
            assertEquals(1, x.size());
            assertTrue(x.contains(tuple(n2, n3, n4)));
        }
    }

    @Test public void TupleIndexRecordPOS_1()
    {
        TupleIndex index = create("POS");
        add(index, n1, n2, n3);
        Tuple<NodeId> tuple2 = tuple(n1, n2, n3);
        Iterator<Tuple<NodeId>> iter = index.find(tuple2);
        assertTrue("Can't find tuple", iter.hasNext());
        iter.next();
        assertFalse(iter.hasNext());
    }

    @Test public void TupleIndexRecordPOS_2()
    {
        TupleIndex index = create("POS");
        add(index, n1, n2, n3);

        Tuple<NodeId> tuple2 = tuple(null, n2, null);
        Iterator<Tuple<NodeId>> iter = index.find(tuple2);
        assertTrue("Can't find tuple",iter.hasNext());
        iter.next();
        assertFalse(iter.hasNext());
    }


    @Test public void TupleIndexRecordPOS_3()
    {
        TupleIndex index = create("POS");
        add(index, n1, n2, n3);

        Tuple<NodeId> tuple2 = tuple(null, n2, n3);
        Iterator<Tuple<NodeId>> iter = index.find(tuple2);
        assertTrue("Can't find tuple", iter.hasNext());
        iter.next();
        assertFalse(iter.hasNext());
    }

    @Test public void TupleIndexRecordFindNot_1()
    {
        TupleIndex index = create("SPO");
        add(index, n1, n2, n3);

        Tuple<NodeId> tuple2 = tuple(n4, n5, n6);
        Iterator<Tuple<NodeId>> iter = index.find(tuple2);
        assertNotNull(iter);
        assertFalse(iter.hasNext());
   }

    @Test public void TupleIndexRecordFindNot_2()
    {
        TupleIndex index = create("SPO");
        add(index, n1, n2, n3);

        Tuple<NodeId> tuple2 = tuple(n1, n5, n6);
        Iterator<Tuple<NodeId>> iter = index.find(tuple2);
        assertFalse(iter.hasNext());
   }

    @Test public void TupleIndexRecordFindNot_3()
    {
        TupleIndex index = create("SPO");
        add(index, n1, n2, n3);

        Tuple<NodeId> tuple2 = tuple(n1, null, n6);
        Iterator<Tuple<NodeId>> iter = index.find(tuple2);
        assertFalse(iter.hasNext());
   }

    @Test public void TupleIndexRecordFindNot_4()
    {
        TupleIndex index = create("SPO");
        add(index, n1, n2, n3);
        add(index, n1, n5, n6);

        Tuple<NodeId> tuple2 = tuple(n4, n5, n6);
        Iterator<Tuple<NodeId>> iter = index.find(tuple2);
        assertFalse(iter.hasNext());
   }

    @Test public void TupleIndexRecordFindNot_5()
    {
        TupleIndex index = create("SPO");
        add(index, n1, n2, n3);
        add(index, n1, n5, n6);

        Tuple<NodeId> tuple2 = tuple(n2, n5, n6);
        Iterator<Tuple<NodeId>> iter = index.find(tuple2);
        assertFalse(iter.hasNext());
   }

    @Test public void TupleIndexRecordFindNot_6()
    {
        TupleIndex index = create("SPO");
        add(index, n1, n2, n3);
        add(index, n4, n5, n6);

        Tuple<NodeId> tuple2 = tuple(n1, null, n6);
        Iterator<Tuple<NodeId>> iter = index.find(tuple2);
        assertFalse(iter.hasNext());
   }


}
