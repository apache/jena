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

package com.hp.hpl.jena.reasoner.rulesys.test;

import java.util.Iterator;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.hp.hpl.jena.graph.BulkUpdateHandler;
import com.hp.hpl.jena.graph.Capabilities;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphEventManager;
import com.hp.hpl.jena.graph.GraphStatisticsHandler;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.TransactionHandler;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.reasoner.Derivation;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ValidityReport;
import com.hp.hpl.jena.reasoner.rulesys.ForwardRuleInfGraphI;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.impl.FRuleEngine;
import com.hp.hpl.jena.reasoner.rulesys.impl.FRuleEngineI;
import com.hp.hpl.jena.reasoner.rulesys.impl.FRuleEngineIFactory;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETEEngine;
import com.hp.hpl.jena.shared.AddDeniedException;
import com.hp.hpl.jena.shared.DeleteDeniedException;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;


public class FRuleEngineIFactoryTest extends TestCase {
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite( FRuleEngineIFactoryTest.class ); 
    }  
    
    @Override
    public void tearDown() {
        FRuleEngineIFactory.setInstance(new FRuleEngineIFactory());
    }

    public void testItShouldBeASingleton() {
        FRuleEngineIFactory instance = FRuleEngineIFactory.getInstance();
        
        assertNotNull("A default instance must be created", instance);

        assertSame("The same instance should have be returned", 
                instance, FRuleEngineIFactory.getInstance());
    }

    public void testItShouldLetYouReplaceTheSingletonInstance() {
        MyFRuleEngineIFactory anotherFactory  = new MyFRuleEngineIFactory();
        FRuleEngineIFactory.setInstance(anotherFactory);

        assertSame("The instance should have been replaced", 
                   anotherFactory, FRuleEngineIFactory.getInstance());
    }
    
    public void testItShouldInstantiateAFRuleEngineIfUseRETEisFalse() {
        ForwardRuleInfGraphI infGraph = new DummyForwardRuleInfGraph();
        FRuleEngineI engine = 
                FRuleEngineIFactory.getInstance().createFRuleEngineI(infGraph, null, false);

        assertSame("A FRuleEngine should have been instantiated", FRuleEngine.class, engine.getClass());
    }

    public void testItShouldInstantiateAReteEngineIfUseRETEisTrue() {
        ForwardRuleInfGraphI infGraph = new DummyForwardRuleInfGraph();
        FRuleEngineI engine = 
                FRuleEngineIFactory.getInstance().createFRuleEngineI(infGraph, null, true);

        assertSame("A RETEEngine should have been instantiated", RETEEngine.class, engine.getClass());
    }
    
    private static final class MyFRuleEngineIFactory extends FRuleEngineIFactory {
    }
    
    private static final class DummyForwardRuleInfGraph implements ForwardRuleInfGraphI{

        @Override
        public Graph getRawGraph() { return null; }

        @Override
        public Reasoner getReasoner() { return null; }

        @Override
        public void rebind(Graph data) {}

        @Override
        public void rebind() {}

        @Override
        public void prepare() {}

        @Override
        public void reset() {}

        @Override
        public Node getGlobalProperty(Node property) { return null; }

        @Override
        public boolean testGlobalProperty(Node property) { return false; }

        @Override
        public ValidityReport validate() { return null; }

        @Override
        public ExtendedIterator<Triple> find(Node subject, Node property, Node object, Graph param) { return null; }

        @Override
        public void setDerivationLogging(boolean logOn) {}
        
        @Override
        public Iterator<Derivation> getDerivation(Triple triple) { return null; }

        @Override
        public boolean dependsOn(Graph other) { return false; }

        @Override
        public TransactionHandler getTransactionHandler() { return null; }

        @Deprecated
        @Override
        public BulkUpdateHandler getBulkUpdateHandler() { return null; }

        @Override
        public Capabilities getCapabilities() { return null; }

        @Override
        public GraphEventManager getEventManager() { return null; }

        @Override
        public GraphStatisticsHandler getStatisticsHandler() { return null; }

        @Override
        public PrefixMapping getPrefixMapping() { return null; }

        @Override
        public void add(Triple t) throws AddDeniedException {}

        @Override
        public void delete(Triple t) throws DeleteDeniedException {}

        @Override
        public ExtendedIterator<Triple> find(TripleMatch m) { return null; }

        @Override
        public ExtendedIterator<Triple> find(Node s, Node p, Node o) { return null; }

        @Override
        public boolean isIsomorphicWith(Graph g) { return false; }

        @Override
        public boolean contains(Node s, Node p, Node o) { return false; }

        @Override
        public boolean contains(Triple t) { return false; }

        @Override
        public void clear() {}

        @Override
        public void remove(Node s, Node p, Node o) {}

        @Override
        public void close() {}
        
        @Override
        public boolean isEmpty() { return false; }

        @Override
        public int size() { return 0; }

        @Override
        public boolean isClosed() { return false; }

        @Override
        public void silentAdd(Triple t) {}

        @Override
        public boolean shouldTrace() { return false; }

        @Override
        public void addBRule(Rule brule) {}

        @Override
        public void deleteBRule(Rule brule) {}

        @Override
        public Graph getDeductionsGraph() { return null; }

        @Override
        public Graph getCurrentDeductionsGraph() { return null; }
        
        @Override
        public void addDeduction(Triple t) {}
        
        @Override
        public ExtendedIterator<Triple> findDataMatches(Node subject, Node predicate, Node object) {
            return null;
        }

        @Override
        public boolean shouldLogDerivations() { return false;}

        @Override
        public void logDerivation(Triple t, Derivation derivation) {}

        @Override
        public void setFunctorFiltering(boolean param) {}
        
    }
 }
