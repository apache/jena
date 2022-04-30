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

package org.apache.jena.reasoner.rulesys.impl;

import java.util.Iterator;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.jena.graph.Factory;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.TransactionHandler;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.graph.impl.TransactionHandlerBase;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.shared.JenaException;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;

public class TestRestartableLBRule extends TestCase {

    public static TestSuite suite() {
        return new TestSuite(TestRestartableLBRule.class);
    }

    Resource Senator = ResourceFactory.createResource("http://example.com/ns/Senator");
    Resource Person = ResourceFactory.createResource("http://example.com/ns/Person");
    Resource Politician = ResourceFactory.createResource("http://example.com/ns/Politician");

    @Test
    public void testCrossTransactionQueryBug() {
        DummyTxnGraph graph = new DummyTxnGraph();
        Model data = ModelFactory.createModelForGraph( graph );
        for(int i = 0; i < 1000; i++) {
            data.add( data.createResource("http://example.com/ns/i-" + i), RDF.type, Senator);
        }

        String rules = "-> tableAll(). " +
                "[r1: (?x rdf:type <http://example.com/ns/Person>) <- (?x rdf:type <http://example.com/ns/Politician>)] " +
                "[r2: (?x rdf:type <http://example.com/ns/Politician>) <- (?x rdf:type <http://example.com/ns/Senator>)] " +
                "[r3: (?x rdf:type <http://example.com/ns/Person>) <- (?x rdf:type <http://example.com/ns/Senator>)] ";
        GenericRuleReasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));

        InfModel infmodel = ModelFactory.createInfModel(reasoner, data);

        assertTrue( queryN(infmodel, Person, 10) );
        assertTrue( queryN(infmodel, Politician, 1000) );
        assertTrue( queryN(infmodel, Person, 1000) );
    }

    private boolean queryN(Model model, Resource c, int n) {
        model.begin();
        StmtIterator si = model.listStatements(null, RDF.type, c);
        try {
            for (int i = 0; i < n; i++) {
                if (si.hasNext()) {
                    si.next();
                } else {
                    return false;
                }
            }
            return true;
        } finally {
            si.close();
            model.commit();
        }
    }

    class DummyTxnHandler extends TransactionHandlerBase implements TransactionHandler {
        int transaction = 0;
        boolean inTransaction = false;

        @Override
        public boolean transactionsSupported() {  return true;   }

        @Override
        public void begin() { if (inTransaction) throw new JenaException("Transaction violation"); else {inTransaction = true; transaction++; } }

        @Override
        public void abort() { inTransaction = false; }

        @Override
        public void commit() { inTransaction = false; }

        public int getTransactionNumber() { if (!inTransaction) throw new JenaException("Transaction violation"); else return transaction; }
    }

    class DummyTxnGraph extends GraphBase implements Graph {
        TransactionHandler th = new DummyTxnHandler();
        Graph base = Factory.createGraphMem();

        @Override
        public void performAdd( Triple t ) { base.add(t); }

        @Override
        public void performDelete( Triple t ) { base.delete(t); }

        @Override
        public TransactionHandler getTransactionHandler() { return th; }

        @Override
        protected ExtendedIterator<Triple> graphBaseFind(Triple triplePattern) {
            return new DummyTxnIterator(base.find(triplePattern), this);
        }
    }

    class DummyTxnIterator extends WrappedIterator<Triple> implements  ExtendedIterator<Triple> {
        DummyTxnGraph graph;
        int transaction;

        public DummyTxnIterator(Iterator<? extends Triple> base, DummyTxnGraph graph) {
            super(base);
            this.graph = graph;
            this.transaction = currentTransaction();

        }

        private int currentTransaction() {
            return ((DummyTxnHandler)graph.getTransactionHandler()).getTransactionNumber();
        }

        @Override
        public boolean hasNext() {
            if (transaction != currentTransaction()) throw new JenaException("Transaction violation");
            return base.hasNext();
        }

        @Override
        public Triple next() {
            if (transaction != currentTransaction()) throw new JenaException("Transaction violation");
            return base.next();
        }

    }
}
