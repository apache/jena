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

package org.apache.jena.sparql.pfunction.library;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jena.graph.Node;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterNullIterator;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.pfunction.PFuncSimple;
import org.apache.jena.sparql.pfunction.PropertyFunction;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sys.JenaSystem;
import org.junit.Test;

public class TestCustomPropertyFunction {

    static { JenaSystem.init(); }

    static private String customPropertyFunctionName = "http://ex/function";


    @Test
    public void testCustomPF_success() {
        // Register a property function in new PropertyFunctionRegistry, in clean context.
        Context context = new Context();
        AtomicInteger count = new AtomicInteger(0);
        PropertyFunctionRegistry registry = new PropertyFunctionRegistry();
        PropertyFunctionRegistry.set(context, registry);
        registry.put(customPropertyFunctionName, x -> CountingPropertyFunction.create(count));
        // GH-1381: Jena 4.5.0 and before required:
        //context.set(ARQ.enablePropertyFunctions, true);

        run(context, count, 1);
    }

    @Test
    public void testCustomPF_no_registry() {
        String customPropertyFunctionName = "http://ex/function";
        Context context = ARQ.getContext().copy();
        PropertyFunctionRegistry.set(context, null);
        AtomicInteger count = new AtomicInteger(0);

        run(context, count, 0);
    }

    @Test
    public void testCustomPF_switched_off() {
        Context context = new Context();
        AtomicInteger count = new AtomicInteger(0);
        PropertyFunctionRegistry registry = new PropertyFunctionRegistry();
        PropertyFunctionRegistry.set(context, registry);
        registry.put(customPropertyFunctionName, x -> CountingPropertyFunction.create(count));
        // Switch off.
        context.set(ARQ.enablePropertyFunctions, false);

        run(context, count, 0);
    }

        private static void run(Context context, AtomicInteger count, int expectedValue ) {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        String query = "SELECT ?s WHERE { ?s <" + customPropertyFunctionName + "> ?o }";

        // GH-1381: Jena 4.5.0 and before required:
        //context.set(ARQ.enablePropertyFunctions, true);

        count.set(0);
        try (QueryExec qe = QueryExec.dataset(dsg).query(query).context(context).build()) {
            qe.select().hasNext();
        }
        assertEquals(expectedValue, count.get());
    }

    private static class CountingPropertyFunction extends PFuncSimple {

        private static PropertyFunction create(AtomicInteger count) {
            return new CountingPropertyFunction(count);
        }

        private AtomicInteger count;

        CountingPropertyFunction(AtomicInteger count) {
            this.count = count;
        }

        @Override
        public QueryIterator execEvaluated(Binding binding, Node subject, Node predicate, Node object, ExecutionContext execCxt) {
            count.incrementAndGet();
            return QueryIterNullIterator.create(execCxt);
        }
    }
}
