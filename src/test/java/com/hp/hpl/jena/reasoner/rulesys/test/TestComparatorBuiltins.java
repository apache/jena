/******************************************************************
 * File:        TestComparatorBuiltins.java
 * Created by:  Dave Reynolds
 * Created on:  16 Oct 2011
 * 
 * (c) Copyright 2011, Epimorphics Limited
 *
 *****************************************************************/

package com.hp.hpl.jena.reasoner.rulesys.test;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
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
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
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
                Node.createLiteral("1.0", "", XSDDatatype.XSDfloat),
                Node.createLiteral("1.1", "", XSDDatatype.XSDdouble) );
        doTestComparator(
                Node.createLiteral("1", "", XSDDatatype.XSDint),
                Node.createLiteral("2", "", XSDDatatype.XSDinteger) );
        doTestComparator(
                Node.createLiteral("1", "", XSDDatatype.XSDint),
                Node.createLiteral("2", "", XSDDatatype.XSDlong) );
    }
    
    public void testComparatorTime() {
        doTestComparator("2000-03-04T20:00:00Z", "2000-03-05T20:00:00Z", XSDDatatype.XSDdateTime);
        doTestComparator("2000-03-04T20:00:00Z", "2000-03-04T21:00:00Z", XSDDatatype.XSDdateTime);
        doTestComparator("2000-03-04T20:00:00Z", "2000-03-05T21:00:00Z", XSDDatatype.XSDdateTime);
        doTestComparator("2000-03-04", "2000-03-05", XSDDatatype.XSDdate);
    }
    
    public void doTestComparator(String lLow, String lHigh, RDFDatatype type) {
        Node nl = Node.createLiteral(lLow, "", type);
        Node nh = Node.createLiteral(lHigh, "", type);
        doTestComparator(nl, nh);
        
        doTestBuiltins(nl, nh);
    }
    
    public void doTestComparator(Node nLow, Node nHigh) {
        assertEquals(0, Util.compareTypedLiterals(nLow, nLow));
        assertEquals(-1, Util.compareTypedLiterals(nLow, nHigh));
        assertEquals(1, Util.compareTypedLiterals(nHigh, nLow));
    }

    public void doTestBuiltins(String lLow, String lHigh, RDFDatatype type) {
        Node nl = Node.createLiteral(lLow, "", type);
        Node nh = Node.createLiteral(lHigh, "", type);
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
