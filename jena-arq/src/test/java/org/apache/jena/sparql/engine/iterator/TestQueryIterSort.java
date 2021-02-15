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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Random ;

import org.apache.jena.atlas.data.DataBagExaminer ;
import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryCancelledException ;
import org.apache.jena.query.SortCondition ;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.sparql.ARQNotImplemented ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingComparator ;
import org.apache.jena.sparql.engine.main.OpExecutorFactory ;
import org.apache.jena.sparql.expr.ExprVar ;
import org.apache.jena.sparql.serializer.SerializationContext ;
import org.apache.jena.sparql.util.Context ;
import org.junit.Before ;
import org.junit.Test ;

public class TestQueryIterSort {

    private static final String LETTERS = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM" ;
    private Random random ;
    private List<Binding> unsorted ;
    private BindingComparator comparator ;
    private CallbackIterator iterator ;

    @Before
    public void setup()
    {
        random = new Random();
        Var[] vars = new Var[]{
            Var.alloc("1"), Var.alloc("2"), Var.alloc("3"),
            Var.alloc("4"), Var.alloc("5"), Var.alloc("6"),
            Var.alloc("7"), Var.alloc("8"), Var.alloc("9"), Var.alloc("0")
        };
        unsorted = new ArrayList<>();
        for(int i = 0; i < 500; i++){
            unsorted.add(randomBinding(vars));
        }

        List<SortCondition> conditions = new ArrayList<>();
        conditions.add(new SortCondition(new ExprVar("8"), Query.ORDER_ASCENDING));
        comparator = new BindingComparator(conditions);

        iterator = new CallbackIterator(unsorted.iterator(), 25, null);
        iterator.setCallback(new Callback() {
            @Override
            public void call() { throw new QueryCancelledException() ; }
        });
    }

    @Test
    public void testNoSpill()
    {
        iterator.setCallback(()->{});
        //new Callback() { @Override
        //public void call() { /* do nothing */ } });
        assertEquals(0, iterator.getReturnedElementCount());
        Context context = new Context() ;
        ExecutionContext executionContext = new ExecutionContext(context, (Graph)null, (DatasetGraph)null, (OpExecutorFactory)null) ;
        QueryIterSort qIter = new QueryIterSort(iterator, comparator, executionContext) ;
        try
        {
            assertEquals(0, iterator.getReturnedElementCount()) ;
            assertEquals(0, DataBagExaminer.countTemporaryFiles(qIter.db)) ;
            qIter.hasNext() ;
            assertEquals(500, iterator.getReturnedElementCount()) ;
            assertEquals(0, DataBagExaminer.countTemporaryFiles(qIter.db)) ;
        }
        finally
        {
            qIter.close() ;
        }
    }

    @Test
    public void testCleanAfterClose()
    {
        iterator.setCallback(()->{});   // Do nothing.
        assertEquals(0, iterator.getReturnedElementCount());
        Context context = new Context() ;
        context.set(ARQ.spillToDiskThreshold, 10L) ;
        ExecutionContext executionContext = new ExecutionContext(context, (Graph)null, (DatasetGraph)null, (OpExecutorFactory)null) ;
        QueryIterSort qIter = new QueryIterSort(iterator, comparator, executionContext) ;
        try
        {
            assertEquals(0, iterator.getReturnedElementCount()) ;
            assertEquals(0, DataBagExaminer.countTemporaryFiles(qIter.db)) ;
            qIter.hasNext() ;
            assertEquals(500, iterator.getReturnedElementCount()) ;
            assertEquals(49, DataBagExaminer.countTemporaryFiles(qIter.db)) ;
        }
        finally
        {
            qIter.close() ;
        }

        assertEquals(0, DataBagExaminer.countTemporaryFiles(qIter.db)) ;
    }

    @Test
    public void testCloseClosesSourceIterator() {
        Context context = new Context() ;
        ExecutionContext ec = new ExecutionContext(context, (Graph) null, (DatasetGraph) null, (OpExecutorFactory) null);
        QueryIterSort qis = new QueryIterSort(iterator, comparator, ec);
        qis.close();
        assertTrue("source iterator should have been closed", iterator.isClosed());
    }

    @Test
    public void testExhaustionClosesSourceIterator() {
        iterator.setCallback(() -> {});
        Context context = new Context() ;
        ExecutionContext ec = new ExecutionContext(context, (Graph) null, (DatasetGraph) null, (OpExecutorFactory) null);
        QueryIterSort qis = new QueryIterSort(iterator, comparator, ec);
        while (qis.hasNext()) qis.next();
        assertTrue("source iterator should have been closed", iterator.isClosed());
    }

    @Test
    public void testCancelClosesSourceIterator() {
        Context context = new Context() ;
        ExecutionContext ec = new ExecutionContext(context, (Graph) null, (DatasetGraph) null, (OpExecutorFactory) null);
        QueryIterSort qis = new QueryIterSort(iterator, comparator, ec);
        try {
            while (qis.hasNext()) qis.next();
            fail("query should have been cancelled by trigger");
        } catch (QueryCancelledException q) {
            assertTrue("source iterator should have been closed", iterator.isClosed());
        }
    }

    @Test
    public void testCleanAfterExhaustion()
    {
        iterator.setCallback(() -> {});
        assertEquals(0, iterator.getReturnedElementCount());
        Context context = new Context() ;
        context.set(ARQ.spillToDiskThreshold, 10L) ;
        ExecutionContext executionContext = new ExecutionContext(context, (Graph)null, (DatasetGraph)null, (OpExecutorFactory)null) ;
        QueryIterSort qIter = new QueryIterSort(iterator, comparator, executionContext) ;

        // Usually qIter should be in a try/finally block, but we are testing the case that the user forgot to do that.
        // As a failsafe, QueryIteratorBase should close it when the iterator is exhausted.
        assertEquals(0, iterator.getReturnedElementCount()) ;
        assertEquals(0, DataBagExaminer.countTemporaryFiles(qIter.db)) ;
        qIter.hasNext() ;
        assertEquals(500, iterator.getReturnedElementCount()) ;
        assertEquals(49, DataBagExaminer.countTemporaryFiles(qIter.db)) ;
        while (qIter.hasNext())
            qIter.next();
        assertEquals(0, DataBagExaminer.countTemporaryFiles(qIter.db)) ;
        qIter.close() ;
    }

    @Test(expected=QueryCancelledException.class)
    public void testCancelInterruptsInitialisation()
    {

        assertEquals(0, iterator.getReturnedElementCount());
        Context context = new Context() ;
        context.set(ARQ.spillToDiskThreshold, 10L) ;
        ExecutionContext executionContext = new ExecutionContext(context, (Graph)null, (DatasetGraph)null, (OpExecutorFactory)null) ;
        QueryIterSort qIter = new QueryIterSort(iterator, comparator, executionContext) ;
        try
        {
            assertEquals(0, iterator.getReturnedElementCount()) ;
            assertEquals(0, DataBagExaminer.countTemporaryFiles(qIter.db)) ;
            qIter.cancel() ;
            qIter.hasNext() ;  // throws a QueryCancelledException
        }
        finally
        {
            assertTrue(iterator.isCanceled()) ;
            assertEquals(0, iterator.getReturnedElementCount()) ;
            assertEquals(0, DataBagExaminer.countTemporaryFiles(qIter.db));
            qIter.close() ;
        }

        assertEquals(0, DataBagExaminer.countTemporaryFiles(qIter.db)) ;
    }

    @Test(expected=QueryCancelledException.class)
    public void testCancelInterruptsExternalSortAfterStartingIteration()
    {
        assertEquals(0, iterator.getReturnedElementCount());
        Context context = new Context() ;
        context.set(ARQ.spillToDiskThreshold, 10L) ;
        ExecutionContext executionContext = new ExecutionContext(context, (Graph)null, (DatasetGraph)null, (OpExecutorFactory)null) ;
        QueryIterSort qIter = new QueryIterSort(iterator, comparator, executionContext) ;
        try
        {
            assertEquals(0, iterator.getReturnedElementCount()) ;
            assertEquals(0, DataBagExaminer.countTemporaryFiles(qIter.db)) ;
            qIter.hasNext() ;  // throws a QueryCancelledException
        }
        catch ( QueryCancelledException e )
        {
            // expected
            assertEquals(26, iterator.getReturnedElementCount()) ;
            // This is zero because QueryIteratorBase will call close() before throwing the QueryCancelledException.
            // It does this as a failsafe in case the user doesn't close the QueryIterator themselves.
            assertEquals(0, DataBagExaminer.countTemporaryFiles(qIter.db)) ;
            throw e ;
        }
        finally
        {
            qIter.close() ;
        }

        assertEquals(0, DataBagExaminer.countTemporaryFiles(qIter.db)) ;
    }

    @Test(expected=QueryCancelledException.class)
    public void testCancelInterruptsExternalSortAtStartOfIteration()
    {
        iterator = new CallbackIterator(unsorted.iterator(), 25, null);
        iterator.setCallback(()->{});
        assertEquals(0, iterator.getReturnedElementCount());
        Context context = new Context() ;
        context.set(ARQ.spillToDiskThreshold, 10L) ;
        ExecutionContext executionContext = new ExecutionContext(context, (Graph)null, (DatasetGraph)null, (OpExecutorFactory)null) ;
        QueryIterSort qIter = new QueryIterSort(iterator, comparator, executionContext) ;
        try
        {
            assertTrue(qIter.hasNext()) ;
            assertEquals(49, DataBagExaminer.countTemporaryFiles(qIter.db));
            assertNotNull(qIter.next()) ;
            assertTrue(qIter.hasNext()) ;
            qIter.cancel() ;
            qIter.hasNext() ;  // throws a QueryCancelledException
        }
        finally
        {
            //assertTrue(iterator.isCanceled()) ;
            assertEquals(500, iterator.getReturnedElementCount()) ;
            assertEquals(0, DataBagExaminer.countTemporaryFiles(qIter.db));
            qIter.close() ;
        }

        assertEquals(0, DataBagExaminer.countTemporaryFiles(qIter.db)) ;
    }

    @Test
    public void testTopNCloseClosesSource() {
        long numItems = 3;
        boolean distinct = false;
        Context context = new Context() ;
        ExecutionContext ec = new ExecutionContext(context, (Graph) null, (DatasetGraph) null, (OpExecutorFactory) null);
        QueryIterTopN tn = new QueryIterTopN(iterator, comparator, numItems, distinct, ec);
        tn.close();
        assertTrue(iterator.isClosed());
    }

    @Test
    public void testTopNExhaustionClosesSource() {
        iterator.setCallback(() -> {});
        long numItems = 3;
        boolean distinct = false;
        Context context = new Context() ;
        ExecutionContext ec = new ExecutionContext(context, (Graph) null, (DatasetGraph) null, (OpExecutorFactory) null);
        QueryIterTopN tn = new QueryIterTopN(iterator, comparator, numItems, distinct, ec);
        while (tn.hasNext()) tn.next();
        assertTrue(iterator.isClosed());
    }


    private Binding randomBinding(Var[] vars)
    {
        BindingBuilder builder = Binding.builder();
        builder.add(vars[0], NodeFactory.createBlankNode());
        builder.add(vars[1], NodeFactory.createURI(randomURI()));
        builder.add(vars[2], NodeFactory.createURI(randomURI()));
        builder.add(vars[3], NodeFactory.createLiteral(randomString(20)));
        builder.add(vars[4], NodeFactory.createBlankNode());
        builder.add(vars[5], NodeFactory.createURI(randomURI()));
        builder.add(vars[6], NodeFactory.createURI(randomURI()));
        builder.add(vars[7], NodeFactory.createLiteral(randomString(5)));
        builder.add(vars[8], NodeFactory.createLiteral("" + random.nextInt(), XSDDatatype.XSDinteger));
        builder.add(vars[9], NodeFactory.createBlankNode());
        return builder.build();
    }

    private String randomURI()
    {
        return String.format("http://%s.example.com/%s", randomString(10), randomString(10));
    }

    private String randomString(int length)
    {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < length; i++){
            builder.append(LETTERS.charAt(random.nextInt(LETTERS.length())));
        }
        return builder.toString();
    }


    private static class CallbackIterator implements QueryIterator
    {
        int elementsReturned = 0 ;
        Callback callback ;
        int trigger ;
        Iterator<Binding> delegate ;
        boolean canceled = false ;
        boolean closed = false ;

        public CallbackIterator(Iterator<Binding> delegate, int trigger, Callback callback)
        {
            this.delegate = delegate ;
            this.callback = callback ;
            this.trigger = trigger ;
        }

        public void setCallback(Callback callback)
        {
            this.callback = callback ;
        }

        @Override
        public boolean hasNext()
        {
        // self-closing
        boolean has = delegate.hasNext() ;
        if (has == false) closed = true;
        return has ;
        }

        @Override
        public Binding next()
        {
            if (elementsReturned++ >= trigger)
            {
                callback.call() ;
            }
            return delegate.next() ;
        }

        @Override
        public void remove()
        {
            delegate.remove() ;
        }

        public int getReturnedElementCount()
        {
            return elementsReturned ;
        }

        public boolean isClosed() {
            return closed ;
        }

        public boolean isCanceled() {
            return canceled ;
        }

        @Override
        public Binding nextBinding()
        {
            if (elementsReturned++ >= trigger) callback.call() ;
            return delegate.next() ;
        }

        @Override
        public void cancel() { canceled = true ; }

        @Override
        public void close() { closed = true ; }

        @Override
        public void output(IndentedWriter out, SerializationContext sCxt) { throw new ARQNotImplemented() ; }

        @Override
        public String toString(PrefixMapping pmap) { throw new ARQNotImplemented() ; }

        @Override
        public void output(IndentedWriter out) { throw new ARQNotImplemented() ; }

    }

    public interface Callback
    {
        public void call() ;
    }

}
