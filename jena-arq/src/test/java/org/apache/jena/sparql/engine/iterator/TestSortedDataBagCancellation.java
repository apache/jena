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
package org.apache.jena.sparql.engine.iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryCancelledException;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingComparator;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.main.OpExecutorFactory;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.util.Context;
/*
	Test that a SortedDataBag used inside a QueryIterSort
	does indeed cut off when cancelled.
*/
public class TestSortedDataBagCancellation {

    static final Binding b1 = BindingFactory.binding(Var.alloc("v1"), NodeFactory.createLiteralString("alpha"));
    static final Binding b2 = BindingFactory.binding(Var.alloc("v2"), NodeFactory.createLiteralString("beta"));
    static final Binding b3 = BindingFactory.binding(Var.alloc("v3"), NodeFactory.createLiteralString("gamma"));
    static final Binding b4 = BindingFactory.binding(Var.alloc("v4"), NodeFactory.createLiteralString("delta"));

    final Context params = new Context();

    final OpExecutorFactory factory = ec -> { throw new UnsupportedOperationException(); };

    final DatasetGraph dataset = DatasetGraphFactory.empty();
    final List<SortCondition> conditions = new ArrayList<>();

    final ExecutionContext ec =  ExecutionContext.create(dataset, params);

    final BindingComparator base_bc = new BindingComparator(conditions, ec);
    final SpecialBindingComparator bc = new SpecialBindingComparator(base_bc, ec);

    private QueryIteratorItems baseIter = createBaseIter();

    private static QueryIteratorItems createBaseIter() {
        QueryIteratorItems iter = new QueryIteratorItems();
        iter.bindings.add(b1);
        iter.bindings.add(b2);
        iter.bindings.add(b3);
        iter.bindings.add(b4);
        return iter;
    }

    QueryIterSort qs = new QueryIterSort(baseIter, bc, ec);

    /**
     * In this test, the iterator is not cancelled; all items should be
     * delivered, and the compare count should be monotonic-nondecreasing.
     */
    @Test
    public void testIteratesToCompletion() {
        int count = 0;
        assertEquals(0, count = bc.count);
        Set<Binding> results = new HashSet<>();

        assertTrue(qs.hasNext());
        assertTrue(bc.count >= count);
        count = bc.count;
        results.add(qs.next());

        assertTrue(qs.hasNext());
        assertTrue(bc.count >= count);
        count = bc.count;
        results.add(qs.next());

        assertTrue(qs.hasNext());
        assertTrue(bc.count >= count);
        count = bc.count;
        results.add(qs.next());

        assertTrue(qs.hasNext());
        assertTrue(bc.count >= count);
        count = bc.count;
        results.add(qs.next());

        assertFalse(qs.hasNext());

        Set<Binding> expected = new HashSet<>();
        expected.add(b1);
        expected.add(b2);
        expected.add(b3);
        expected.add(b4);

        assertEquals(expected, results);
    }

    /**
     * In this test, the iterator is cancelled after the first result is
     * delivered. Any attempt to run the comparator should be trapped an
     * exception thrown. The iterators should deliver no more values.
     */
    @Test
    public void testIteratesWithCancellation() {
        int count = 0;
        assertEquals(0, count = bc.count);
        Set<Binding> results = new HashSet<>();

        assertTrue(qs.hasNext());
        assertTrue(bc.count >= count);
        count = bc.count;
        results.add(qs.next());

        qs.cancel();
        try {
            bc.noMoreCalls();
            while (qs.hasNext())
                qs.next();
        } catch (QueryCancelledException qe) {
            assertTrue(qs.db.isCancelled());
            return;
        }
        fail("query was not cancelled");
    }

    /**
     * A QueryIterator that delivers the elements of a list of bindings.
     */
    private static final class QueryIteratorItems extends QueryIteratorBase {
        List<Binding> bindings = new ArrayList<>();
        int index = 0;

        @Override
        public void output(IndentedWriter out, SerializationContext sCxt) {
            out.write("a QueryIteratorItems");
        }

        @Override
        protected boolean hasNextBinding() {
            return index < bindings.size();
        }

        @Override
        protected Binding moveToNextBinding() {
            index += 1;
            return bindings.get(index - 1);
        }

        @Override
        protected void closeIterator() {
        }

        @Override
        protected void requestCancel() {
        }
    }

    /**
     * A BindingComparator that wraps another BindingComparator and counts how
     * many times compare() is called.
     */
    static class SpecialBindingComparator extends BindingComparator {
        final BindingComparator base;
        int count = 0;
        boolean trapCompare = false;

        public SpecialBindingComparator(BindingComparator base, ExecutionContext ec) {
            super(base.getConditions(), ec);
            this.base = base;
        }

        public void noMoreCalls() {
            trapCompare = true;
        }

        @Override
        public int compare(Binding x, Binding y) {
            if (trapCompare)
                throw new RuntimeException("compare() no longer allowed.");
            count += 1;
            return base.compare(x, y);
        }
    }
}
