/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.enhanced;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.graph.GraphTestLib;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.junit.NodeCreateUtils;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.RDFVisitor;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.StatementTerm;
import org.apache.jena.shared.JenaException;
import org.apache.jena.test.JenaTestLib;

/**
 * These tests give a small version of a model-like interface
 {@link T_Model} with different views
 * over the nodes in the graph {@link T_Subject},
 *{@link T_Property} {@link T_Object}
 *Any node can be any one of these three, but the interface only works
 *if the node is the subject, property or object, respectively,
  of some triple in the graph.
 *There are two implementations of the three interfaces. We use four
 * different
 *personalities, in the tests, from various combinations of the implementation
 *classes with the interface classes. A more realistic test would be a basic set
 *of interfaces with implementations, and then some more extended interfaces and
 *implementations which can work together.
 *
 *These tests only test EnhNode polymorphism and not EnhGraph polymorphism.
 *EnhGraph polymorphism currently will not work.
 */
public class TestEnhanced {

    static { JenaTestLib.setup(); }

	static final private  Personality<RDFNode> split = new Personality<>();

	static final private Personality<RDFNode> combo = new Personality<>();

	static final private GraphPersonality bitOfBoth = new GraphPersonality();
	static final private GraphPersonality broken = new GraphPersonality();

	static {
            // Setting up the personalities, involves registering how
            // each interface is implemented by default.
            // Note this does not guarantee that the only implementations
            // of each interface will be the one specified.
            // See bitOfBoth.
        split.add( T_Object.class, T_ObjectImpl.factory );
        split.add( T_Subject.class, T_SubjectImpl.factory );
        split.add( T_Property.class, T_PropertyImpl.factory );

        combo.add( T_Object.class, T_AllImpl.factory );
        combo.add( T_Subject.class, T_AllImpl.factory );
        combo.add( T_Property.class, T_AllImpl.factory );

        bitOfBoth.add( T_Object.class, T_ObjectImpl.factory );
        bitOfBoth.add( T_Subject.class, T_SubjectImpl.factory );
        bitOfBoth.add( T_Property.class, T_AllImpl.factory );

        // broken is misconfigured and must throw an exception.
        broken.add(T_Object.class, T_ObjectImpl.factory );
        broken.add( T_Subject.class, T_SubjectImpl.factory );
        broken.add( T_Property.class, T_ObjectImpl.factory );
	}

    // Create the graph to test.
    // These are model tests so use a same-value model.
    private static Graph graphToTest() {
        return GraphMemFactory.createDefaultGraphSameValue();
    }

    /**
     * test that equals works on an EnhNode (after hedgehog introduced FrontsNode it
     * didn't).
     */
    @Test
    public void testEquals() {
        EnhNode a = new EnhNode(NodeCreateUtils.create("eg:example"), null);
        assertEquals(a, a);
    }

    /**
     * View n as intf. This is supported iff rslt.
     */
    private static <X extends RDFNode> void miniAsSupports(String title, T_Node n, Class<X> intf, boolean rslt ) {
        assertTrue(n instanceof Polymorphic<?>, title +":sanity");

        // It is always possible to view any node with any interface.
        T_Node as1 = (T_Node)((EnhNode)n).viewAs(intf);
        T_Node as2 = (T_Node)((EnhNode)n).viewAs(intf);

        // caching should ensure we get the same result both times.
        assertTrue( as1==as2, title + ":idempotency" );

        // Whether the interface is actually useable depends on the underlying
        // graph. This factoid is the rslt parameter.
        assertEquals( rslt, ((EnhNode) as1).supports( intf ), title +":support" );
    }

    private static void oneNodeAsSupports(String title, T_Node n, boolean rslts[] ) {
    	// Try n with all three interfaces.
        miniAsSupports(title+"/T_Subject",n,T_Subject.class,rslts[0]);
        miniAsSupports(title+"/T_Property",n,T_Property.class,rslts[1]);
        miniAsSupports(title+"/T_Object",n,T_Object.class,rslts[2]);
    }

    private static void manyNodeAsSupports(String title, T_Node n[], boolean rslts[][] ) {
    	// Try each n with each interface.
        for (int i=0;i<n.length;i++){
          oneNodeAsSupports(title+"["+i+"]",n[i],rslts[i]);
        }
    }


    /** This test show the basic format of an enhanced test.
     *  This test access data in an enhanced fashion.
     *  All modifications are done through the underlying graph.
     *  The methods tested are as and supports.
     */
    private static void basic(String title, Personality<RDFNode> p) {
        Graph g = graphToTest();
        T_Model model =  new T_ModelImpl(g,p);
        // create some data
        GraphTestLib.graphAdd( g, "x R y;" );

        // The graph has three nodes, extract them as T_Node's,
        // using the minimalist ModelAPI.
        T_Node nodes[] = new T_Node[]{
            model.aSubject(),
            model.aProperty(),
            model.anObject()
        };

        // Run the basic tests.
        manyNodeAsSupports(title+"(a)",nodes,
           new boolean[][]{
               new boolean[]{true,false,false}, // nodes[0] is subj, but not prop, or obj
               new boolean[]{false,true,false},
               new boolean[]{false,false,true}
        });

        GraphTestLib.graphAdd(g,"y R x;" );

        // The expected results are now different.
        // (A node is appropriate for the T_Subject interface if it is
        // the subject of some triple in the graph, so the third node
        // can now be a T_Subject).
        manyNodeAsSupports(title+"(b)",nodes,
           new boolean[][]{
               new boolean[]{true,false,true}, // nodes[0] is subj and obj, but not prop
               new boolean[]{false,true,false},
               new boolean[]{true,false,true}
        });

        g.delete( GraphTestLib.triple( "x R y" ) );

    	// The expected results are now different again.
    	// (A node is appropriate for the T_Subject interface if it is
    	// the subject of some triple in the graph, so the third node
    	// can now be a T_Subject).

        manyNodeAsSupports(title+"(c)",nodes,
           new boolean[][]{
               new boolean[]{false,false,true},
               new boolean[]{false,true,false},
               new boolean[]{true,false,false}
        });


    }

    /**
        Would like to get rid of these, but the abstraction is hard to find at the
        moment. At least they're now just local to this test class.
    */
    static final int S = 1;
    static final int P = 2;
    static final int O = 3;

    // This is like the earlier test: miniAsSupports (the last part of it).
    // However, this time instead of asking whether the interface will work
    // or not, we just try it.
    // Obviously sometimes it is broken, which should be reported using
    // an IllegalStateException.
	private void canImplement(String title, T_Node n, int wh, boolean rslt ) {
		try {
			switch (wh) {
				case S:
					n.asSubject().aProperty();
					break;
				case P:
					n.asProperty().anObject();
					break;
				case O:
					n.asObject().aSubject();
					break;
			}
			assertTrue(rslt, "IllegalStateException expected.");
		}
		catch (IllegalStateException e) {
			assertFalse(rslt, "IllegalStateException at the wrong time.");
		}
	}

	private void canImplement(String title, T_Node n, boolean rslts[] ) {
		canImplement(title+"/T_Subject",n,S,rslts[0]);
		canImplement(title+"/T_Property",n,P,rslts[1]);
		canImplement(title+"/T_Object",n,O,rslts[2]);
	}
	private void canImplement(String title, T_Node n[], boolean rslts[][] ) {
		for (int i=0;i<n.length;i++){
		  canImplement(title+"["+i+"]",n[i],rslts[i]);
		}
	}

    private void follow(String title, Personality<RDFNode> p) {
        Graph g = graphToTest();
        T_Model model =  new T_ModelImpl(g,p);
        // create some data
        GraphTestLib.graphAdd( g, "a b c;" );
        T_Node nodes[] = new T_Node[]{
            model.aSubject(),
            model.aProperty(),
            model.anObject()
        };

        // Similar to the basic test.
        canImplement(title+"(a)",nodes,
           new boolean[][]{
               new boolean[]{true,false,false},
               new boolean[]{false,true,false},
               new boolean[]{false,false,true}
        });

        GraphTestLib.graphAdd(g, "b a c;" );

    	// Again like in the basic test the triples have now changed,
    	// so different methods will now work.
        canImplement(title+"(b)",nodes,
           new boolean[][]{
               new boolean[]{true,true,false},
               new boolean[]{true,true,false},
               new boolean[]{false,false,true}
        });

        g.delete(GraphTestLib.triple( "a b c" ) );


    	// Again like in the basic test the triples have now changed,
    	// so different methods will now work.
        canImplement(title+"(c)",nodes,
           new boolean[][]{
               new boolean[]{false,true,false},
               new boolean[]{true,false,false},
               new boolean[]{false,false,true}
        });

        // Another twist.
        canImplement(title+"(c)",new T_Node[]{
            nodes[1].asSubject().aProperty(),
            nodes[2].asObject().aSubject(),
            nodes[0].asProperty().anObject()
        },
           new boolean[][]{
               new boolean[]{false,true,false},
               new boolean[]{true,false,false},
               new boolean[]{false,false,true}
        });
        assertTrue(nodes[0].asProperty().anObject().equals(nodes[2]), "Recreated node");
    }

    @Test
    public void testSplitBasic() { basic("Split: ",split); }

    @Test
    public void testComboBasic() { basic("Combo: ",combo); }

    @Test
    public void testSplitFollow() { follow("Split: ",split); }

    @Test
    public void testComboFollow() { follow("Combo: ",combo); }

    @Test
    public void testBitOfBothBasic() { basic("bob: ",bitOfBoth); }

    @Test
    public void testBitOfBothFollow() { follow("bob: ",bitOfBoth); }

    @Test
    public  void testBitOfBothSurprise() {
    	// bitOfBoth is a surprising personality ...
    	// we can have two different java objects implementing the same interface.

		Graph g = graphToTest();
		T_Model model =  new T_ModelImpl(g,bitOfBoth);
		// create some data
		GraphTestLib.graphAdd( g, "a a a;" );
		T_Subject testSubjectImpl = model.aSubject();
		assertTrue(testSubjectImpl instanceof T_SubjectImpl,
		         "BitOfBoth makes subjects using T_SubjectImpl");
		T_Property testAllImpl = testSubjectImpl.aProperty();
    	assertTrue(testAllImpl instanceof T_AllImpl,
    			 "BitOfBoth makes properties using T_AllImpl");
    	assertTrue(testAllImpl == testAllImpl.asSubject(),
    	          "turning a T_AllImpl into a T_Subject is a no-op");
    	assertTrue(testSubjectImpl != testAllImpl.asSubject(),
    			  "turning a T_AllImpl into a T_Subject is a no-op");
    	assertTrue(testSubjectImpl.asSubject() != testSubjectImpl.asSubject().asProperty().asSubject(),
    			  "turning a T_AllImpl into a T_Subject is a no-op");

    }

    @Test
    public void testBrokenBasic() {
        // Any of the tests ought to work up and til the point
        // that they don't. At that point they need to detect the
        // error and throw the PersonalityConfigException.
        assertThrows(PersonalityConfigException.class,
                     ()->basic("Broken: ",broken),
                     "broken is a misconfigured personality, but it wasn't detected.");
    }

    static class Example extends EnhNode implements RDFNode {
        public Example(Node n, EnhGraph g) {
            super(n, g);
        }

        static final Implementation factory = new Implementation() {
            @Override
            public EnhNode wrap(Node n, EnhGraph g) {
                return new EnhNode(n, g);
            }

            @Override
            public boolean canWrap(Node n, EnhGraph g) {
                return n.isURI();
            }
        };

        @Override
        public RDFNode inModel( Model m )
        { return null; }

        @Override
        public Model getModel()
        { throw new JenaException( "getModel() should not be called in the EnhGraph/Node tests" ); }

        @Override
        public Resource asResource()
        { throw new JenaException( "asResource() should not be called in the EnhGraph/Node tests" ); }

        @Override
        public Literal asLiteral()
        { throw new JenaException( "asLiteral() should not be called in the EnhGraph/Node tests" ); }

        @Override
        public StatementTerm asStatementTerm()
        { throw new JenaException( "asStatementTerm() should not be called in the EnhGraph/Node tests" ); }

        @Override
        public Object visitWith( RDFVisitor rv )
        { return null; }
    }

    @Test
    public void testSimple() {
        Graph g = graphToTest();
        Personality<RDFNode> ours = BuiltinPersonalities.model.copy().add(Example.class, Example.factory);
        EnhGraph eg = new EnhGraph(g, ours);
        EnhNode eNode = new EnhNode(NodeFactory.createURI("spoo:bar"), eg);
        EnhNode eBlank = new EnhNode(NodeFactory.createBlankNode(), eg);
        assertTrue(eNode.supports(Example.class), "URI node can be an Example");
        assertFalse(eBlank.supports(Example.class), "Blank node cannot be an Example");
    }

    static class AnotherExample {
        static final Implementation factory = new Implementation() {
            @Override
            public EnhNode wrap(Node n, EnhGraph g) {
                return new EnhNode(n, g);
            }

            @Override
            public boolean canWrap(Node n, EnhGraph g) {
                return n.isURI();
            }
        };
    }

    @Test
    public void testAlreadyLinkedViewException() {
        Graph g = graphToTest();
        Personality<RDFNode> ours = BuiltinPersonalities.model.copy().add(Example.class, Example.factory);
        EnhGraph eg = new EnhGraph(g, ours);
        Node n = NodeCreateUtils.create("spoo:bar");
        EnhNode eNode = new Example(n, eg);
        EnhNode multiplexed = new Example(n, eg);
        multiplexed.as(Property.class);
        eNode.viewAs(Example.class);
        assertThrows(AlreadyLinkedViewException.class,
                     ()->eNode.addView(multiplexed),
                     "should raise an AlreadyLinkedViewException");
    }

    /**
     * Test that an attempt to polymorph an enhanced node into a class that isn't
     * supported by the enhanced graph generates an UnsupportedPolymorphism
     * exception.
     */
    @Test
    public void testNullPointerTrap() {
        Graph g = graphToTest();
        EnhGraph eg = new EnhGraph(g, new Personality<RDFNode>());
        Node n = NodeCreateUtils.create("eh:something");
        EnhNode en = new EnhNode(n, eg);
        UnsupportedPolymorphismException e =
            assertThrows(UnsupportedPolymorphismException.class, ()->en.as(Property.class));
        assertEquals(en, e.getBadNode());
        assertTrue(eg == ((EnhNode)e.getBadNode()).getGraph(), "exception should have cuplprit graph");
        assertSame(Property.class, e.getBadClass(), "exception should have culprit class");
    }

    @Test
    public void testNullPointerTrapInCanSupport() {
        Graph g = graphToTest();
        EnhGraph eg = new EnhGraph(g, new Personality<RDFNode>());
        Node n = NodeCreateUtils.create("eh:something");
        EnhNode en = new EnhNode(n, eg);
        assertFalse(en.canAs(Property.class));
    }

    @Test
    public void testAsToOwnClassWithNoModel() {
        Resource r = ResourceFactory.createResource();
        assertEquals(null, r.getModel());
        assertTrue(r.canAs(Resource.class));
        assertSame(r, r.as(Resource.class));
    }

    @Test
    public void testCanAsReturnsFalseIfNoModel() {
        Resource r = ResourceFactory.createResource();
        assertEquals(false, r.canAs(Example.class));
    }

    @Test
    public void testAsThrowsPolymorphismExceptionIfNoModel() {
        Resource r = ResourceFactory.createResource();
        UnsupportedPolymorphismException e =
            assertThrows(UnsupportedPolymorphismException.class, ()->r.as(Example.class));
        assertTrue(e.getBadNode() instanceof EnhNode);
        assertEquals(null, ((EnhNode)e.getBadNode()).getGraph());
        assertEquals(Example.class, e.getBadClass());
    }
}
