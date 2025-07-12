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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.data.DistinctDataBag;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.util.Context;

public class TestCancelDistinct {

    private final class MockQueryIterator extends QueryIteratorBase {

        Iterator<Binding> bindings;

        MockQueryIterator() {
            this(new ArrayList<Binding>());
        }

        MockQueryIterator(Binding ... bindings) {
            this(Arrays.asList(bindings));
        }

        MockQueryIterator(List<Binding> bindings) {
            this.bindings = bindings.iterator();
        }

        @Override
        public void output(IndentedWriter out, SerializationContext sCxt) {

        }

        @Override
        protected boolean hasNextBinding() {
            return bindings.hasNext();
        }

        @Override
        protected Binding moveToNextBinding() {
            return bindings.next();
        }

        @Override
        protected void closeIterator() {

        }

        @Override
        protected void requestCancel() {

        }
    }

    /**
       test that of a QueryIterDistinct is cancelled, so is the
       iterator that it wraps.
    */
    @Test public void testUnbaggedCancelPropagates() {
        // Something better than null would be good. But making
        // an ExecutionContext is non-trivial.
        ExecutionContext c = null;
        QueryIteratorBase base = new MockQueryIterator();

        QueryIterDistinct d = new QueryIterDistinct(base, null, c);
        assertFalse(base.getRequestingCancel());
        d.cancel();
        assertTrue(base.getRequestingCancel());
    }

    final Context params = new Context();
    final ExecutionContext cxt = ExecutionContext.create(params);

    /**
       test that of a QueryIterDistinct with an active databag is
       cancelled, so is the iterator that it wraps.
    */
    @Test public void testBaggedCancelPropagates() {
        params.set(ARQ.spillToDiskThreshold, 0);

        QueryIteratorBase base = new MockQueryIterator(BindingFactory.empty());
        QueryIterDistinct d = new QueryIterDistinct(base, null, cxt);

        assertNull(d.db);

        Binding b = d.next();

        assertNotNull(d.db);
        DistinctDataBag<Binding> db = d.db;

        assertFalse(base.getRequestingCancel());
        d.cancel();
        assertTrue(base.getRequestingCancel());

    }

    @Test public void testCloseWhenNoBag() {
        params.set(ARQ.spillToDiskThreshold, 0);

        QueryIteratorBase base = new MockQueryIterator(BindingFactory.empty());
        QueryIterDistinct d = new QueryIterDistinct(base, null, cxt);

        // when there is no databag, close leaves it null
        assertNull(d.db);
        d.close();
        assertNull(d.db);
    }

    @Test public void testCloseWhenBagPresent() {
        params.set(ARQ.spillToDiskThreshold, 0);

        QueryIteratorBase base = new MockQueryIterator(BindingFactory.empty());
        QueryIterDistinct d = new QueryIterDistinct(base, null, cxt);

        assertNull(d.db);
        Binding ignored = d.next();
        assertNotNull(d.db);
        DistinctDataBag<Binding> bag = d.db;
        d.close();
        assertTrue(bag.isClosed());
        assertNull(d.db);
    }
}
