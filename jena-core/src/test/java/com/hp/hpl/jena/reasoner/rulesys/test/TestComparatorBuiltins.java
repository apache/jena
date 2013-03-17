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

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.InfGraph;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.BindingEnvironment;
import com.hp.hpl.jena.reasoner.rulesys.Builtin;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.RuleContext;
import com.hp.hpl.jena.reasoner.rulesys.Util;
import com.hp.hpl.jena.reasoner.rulesys.builtins.Equal;
import com.hp.hpl.jena.reasoner.rulesys.builtins.GE;
import com.hp.hpl.jena.reasoner.rulesys.builtins.GreaterThan;
import com.hp.hpl.jena.reasoner.rulesys.builtins.LE;
import com.hp.hpl.jena.reasoner.rulesys.builtins.LessThan;
import com.hp.hpl.jena.reasoner.rulesys.builtins.NotEqual;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test cases for comparison operators, especially as applies to time values
 */
public class TestComparatorBuiltins extends TestCase {
    
    public TestComparatorBuiltins(String name) {
        super(name);
    }
    
    public static TestSuite suite() {
        return new TestSuite( TestComparatorBuiltins.class ); 
    }  
    
    public void testComparatorNumbers() {
        doTestComparator("1", "2", XSDDatatype.XSDint);
        doTestComparator("1.0", "1.1", XSDDatatype.XSDfloat);
        doTestComparator("1.0", "1.1", XSDDatatype.XSDdouble);
        doTestComparator(
                NodeFactory.createLiteral("1.0", "", XSDDatatype.XSDfloat),
                NodeFactory.createLiteral("1.1", "", XSDDatatype.XSDdouble) );
        doTestComparator(
                NodeFactory.createLiteral("1", "", XSDDatatype.XSDint),
                NodeFactory.createLiteral("2", "", XSDDatatype.XSDinteger) );
        doTestComparator(
                NodeFactory.createLiteral("1", "", XSDDatatype.XSDint),
                NodeFactory.createLiteral("2", "", XSDDatatype.XSDlong) );
    }
    
    public void testComparatorTime() {
        doTestComparator("2000-03-04T20:00:00Z", "2000-03-05T20:00:00Z", XSDDatatype.XSDdateTime);
        doTestComparator("2000-03-04T20:00:00Z", "2000-03-04T21:00:00Z", XSDDatatype.XSDdateTime);
        doTestComparator("2000-03-04T20:00:00Z", "2000-03-05T21:00:00Z", XSDDatatype.XSDdateTime);
        doTestComparator("2000-03-04", "2000-03-05", XSDDatatype.XSDdate);
    }
    
    public void doTestComparator(String lLow, String lHigh, RDFDatatype type) {
        Node nl = NodeFactory.createLiteral(lLow, "", type);
        Node nh = NodeFactory.createLiteral(lHigh, "", type);
        doTestComparator(nl, nh);
        
        doTestBuiltins(nl, nh);
    }
    
    public void doTestComparator(Node nLow, Node nHigh) {
        assertEquals(0, Util.compareTypedLiterals(nLow, nLow));
        assertEquals(-1, Util.compareTypedLiterals(nLow, nHigh));
        assertEquals(1, Util.compareTypedLiterals(nHigh, nLow));
    }

    public void doTestBuiltins(String lLow, String lHigh, RDFDatatype type) {
        Node nl = NodeFactory.createLiteral(lLow, "", type);
        Node nh = NodeFactory.createLiteral(lHigh, "", type);
        doTestBuiltins(nl, nh);
    }
    
    public void doTestBuiltins(Node nLow, Node nHigh) {
        assertTrue( call(new Equal(), nLow, nLow) );
        assertFalse( call(new Equal(), nLow, nHigh) );
        
        assertFalse( call(new NotEqual(), nLow, nLow) );
        assertTrue( call(new NotEqual(), nLow, nHigh) );

        assertTrue( call(new LE(), nLow, nHigh) );
        assertFalse( call(new LE(), nHigh, nLow) );
        assertTrue( call(new LE(), nLow, nLow) );
        
        assertTrue( call(new LessThan(), nLow, nHigh) );
        assertFalse( call(new LessThan(), nHigh, nLow) );
        assertFalse( call(new LessThan(), nLow, nLow) );
        
        assertFalse( call(new GE(), nLow, nHigh) );
        assertTrue( call(new GE(), nHigh, nLow) );
        assertTrue( call(new GE(), nLow, nLow) );
        
        assertFalse( call(new GreaterThan(), nLow, nHigh) );
        assertTrue( call(new GreaterThan(), nHigh, nLow) );
        assertFalse( call(new GreaterThan(), nLow, nLow) );
        
    }
    
    public boolean call(Builtin builtin, Node n1, Node n2) {
        return builtin.bodyCall(new Node[] {n1, n2}, 2, new DummyRuleContext());
    }
    
    static class DummyRuleContext implements RuleContext {

        @Override
        public BindingEnvironment getEnv() {
            return new BindingEnvironment() {
                
                @Override
                public Triple instantiate(TriplePattern pattern) {
                    // TODO Auto-generated method stub
                    return null;
                }
                
                @Override
                public Node getGroundVersion(Node node) {
                    return node;
                }
                
                @Override
                public boolean bind(Node var, Node value) {
                    // TODO Auto-generated method stub
                    return false;
                }
            };
        }

        @Override
        public InfGraph getGraph() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Rule getRule() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setRule(Rule rule) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public boolean contains(Triple t) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean contains(Node s, Node p, Node o) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public ClosableIterator<Triple> find(Node s, Node p, Node o) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void silentAdd(Triple t) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void add(Triple t) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void remove(Triple t) {
            // TODO Auto-generated method stub
            
        }
        
    }

}
