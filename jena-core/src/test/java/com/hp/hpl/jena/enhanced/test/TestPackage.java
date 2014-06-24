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

/*
 * EnhancedTestSuite.java
 *
 * Created on 27 November 2002, 04:53
 */

package com.hp.hpl.jena.enhanced.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.test.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.enhanced.*;

import junit.framework.*;

/**
 * These tests give a small version of a model-like interface
 {@link TestModel} with different views
 * over the nodes in the graph {@link TestSubject},
 *{@link TestProperty} {@link TestObject} 
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
public class TestPackage extends GraphTestBase  {
    
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
        split.add( TestObject.class, TestObjectImpl.factory );
        split.add( TestSubject.class, TestSubjectImpl.factory );
        split.add( TestProperty.class, TestPropertyImpl.factory );
        
        combo.add( TestObject.class, TestAllImpl.factory );
        combo.add( TestSubject.class, TestAllImpl.factory );
        combo.add( TestProperty.class, TestAllImpl.factory );
        
        bitOfBoth.add( TestObject.class, TestObjectImpl.factory );
        bitOfBoth.add( TestSubject.class, TestSubjectImpl.factory );
        bitOfBoth.add( TestProperty.class, TestAllImpl.factory );
        
        // broken is misconfigured and must throw an exception.
        broken.add(TestObject.class, TestObjectImpl.factory );
        broken.add( TestSubject.class, TestSubjectImpl.factory );
        broken.add( TestProperty.class, TestObjectImpl.factory );
	}
    /** Creates a new instance of EnhancedTestSuite */
   	public TestPackage(String name)
		{
		super( name );
		}
		
    public static TestSuite suite()
        { return new TestSuite( TestPackage.class ); }
    
    /**
        test that equals works on an EnhNode (after hedgehog introduced FrontsNode
        it didn't).
    */
    public void testEquals()
        {
        EnhNode a = new EnhNode( NodeCreateUtils.create( "eg:example" ), null );
        assertEquals( a, a );
        }
        
    /**
     * View n as intf. This is supported iff rslt.
     */
    private static <X extends RDFNode> void miniAsSupports(String title, TestNode n, Class<X> intf, boolean rslt ) {
        assertTrue(title +":sanity",n instanceof Polymorphic<?>);
        
        // It is always possible to view any node with any interface.
        TestNode as1 = (TestNode)((EnhNode)n).viewAs(intf);
        TestNode as2 = (TestNode)((EnhNode)n).viewAs(intf);
        
        // caching should ensure we get the same result both times.
        assertTrue( title + ":idempotency", as1==as2 );
        
        // Whether the interface is actually useable depends on the underlying
        // graph. This factoid is the rslt parameter.
        assertEquals( title +":support",rslt,((EnhNode) as1).supports( intf ) ); 
    }
    
    private static void oneNodeAsSupports(String title, TestNode n, boolean rslts[] ) {
    	// Try n with all three interfaces.
        miniAsSupports(title+"/TestSubject",n,TestSubject.class,rslts[0]);
        miniAsSupports(title+"/TestProperty",n,TestProperty.class,rslts[1]);
        miniAsSupports(title+"/TestObject",n,TestObject.class,rslts[2]);
    }
    
    private static void manyNodeAsSupports(String title, TestNode n[], boolean rslts[][] ) {
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
        Graph g = Factory.createGraphMem();
        TestModel model =  new TestModelImpl(g,p);
        // create some data
        graphAdd( g, "x R y;" );
        
        // The graph has three nodes, extract them as TestNode's,
        // using the minimalist ModelAPI.
        TestNode nodes[] = new TestNode[]{
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
        
        graphAdd(g,"y R x;" );
        
        // The expected results are now different.
        // (A node is appropriate for the TestSubject interface if it is
        // the subject of some triple in the graph, so the third node
        // can now be a TestSubject).
        manyNodeAsSupports(title+"(b)",nodes, 
           new boolean[][]{
               new boolean[]{true,false,true}, // nodes[0] is subj and obj, but not prop
               new boolean[]{false,true,false},
               new boolean[]{true,false,true}
        });
        
        g.delete( triple( "x R y" ) );

    	// The expected results are now different again.
    	// (A node is appropriate for the TestSubject interface if it is
    	// the subject of some triple in the graph, so the third node
    	// can now be a TestSubject).
        
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
	private  void canImplement(String title, TestNode n, int wh, boolean rslt ) {
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
			assertTrue("IllegalStateException expected.",rslt);
		}
		catch (IllegalStateException e) {
			assertFalse("IllegalStateException at the wrong time.",rslt);
		}
	}

	private  void canImplement(String title, TestNode n, boolean rslts[] ) {
		canImplement(title+"/TestSubject",n,S,rslts[0]);
		canImplement(title+"/TestProperty",n,P,rslts[1]);
		canImplement(title+"/TestObject",n,O,rslts[2]);
	}
	private  void canImplement(String title, TestNode n[], boolean rslts[][] ) {
		for (int i=0;i<n.length;i++){
		  canImplement(title+"["+i+"]",n[i],rslts[i]);
		}
	}
	
    private  void follow(String title, Personality<RDFNode> p) {
        Graph g = Factory.createGraphMem();
        TestModel model =  new TestModelImpl(g,p);
        // create some data
        graphAdd( g, "a b c;" );
        TestNode nodes[] = new TestNode[]{
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
        
        graphAdd(g, "b a c;" );

    	// Again like in the basic test the triples have now changed,
    	// so different methods will now work.
        canImplement(title+"(b)",nodes, 
           new boolean[][]{
               new boolean[]{true,true,false}, 
               new boolean[]{true,true,false},
               new boolean[]{false,false,true}
        });
        
        g.delete(triple( "a b c" ) );


    	// Again like in the basic test the triples have now changed,
    	// so different methods will now work.
        canImplement(title+"(c)",nodes, 
           new boolean[][]{
               new boolean[]{false,true,false}, 
               new boolean[]{true,false,false},
               new boolean[]{false,false,true}
        });

        // Another twist.
        canImplement(title+"(c)",new TestNode[]{
            nodes[1].asSubject().aProperty(),
            nodes[2].asObject().aSubject(),
            nodes[0].asProperty().anObject()
        }, 
           new boolean[][]{
               new boolean[]{false,true,false}, 
               new boolean[]{true,false,false},
               new boolean[]{false,false,true}
        });                
        assertTrue("Model cache test",nodes[0].asProperty().anObject()==nodes[2]);
    }
    private  void cache(String title, Personality<RDFNode> p) {
        Graph g = Factory.createGraphMem();
        TestModel model =  new TestModelImpl(g,p);
        // create some data
        graphAdd( g, "a b a;" );
        
        // get the same node in two different ways.
        assertTrue("Caching is on",model.aSubject().asObject()==model.anObject());
        
        ((TestModelImpl)model).getNodeCacheControl().setEnabled(false);
        

    	// get the same node in two different ways; if there isn't any caching
    	// then we reconstruct the node.
        assertFalse("Caching is off",model.aSubject()==model.anObject());
        
    }
    public static void testSplitBasic() {
       basic("Split: ",split);
    }
    public static void testComboBasic() {
     basic("Combo: ",combo);
    }
    public  void testSplitFollow() {
       follow("Split: ",split);
    }
    public  void testComboFollow() {
     follow("Combo: ",combo);
    }
    
    public  void testSplitCache() {
        cache("Split: ",split);
    }
    public  void testComboCache() {
     cache("Combo: ",combo);
    }
    
    public static void testBitOfBothBasic() {
       basic("bob: ",bitOfBoth);
    }
    public  void testBitOfBothFollow() {
       follow("bob: ",bitOfBoth);
    }
    
    public  void testBitOfBothCache() {
        cache("bob: ",bitOfBoth);
    }
    
    public static void testBitOfBothSurprise() {
    	// bitOfBoth is a surprising personality ...
    	// we can have two different java objects implementing the same interface.
    	
		Graph g = Factory.createGraphMem();
		TestModel model =  new TestModelImpl(g,bitOfBoth);
		// create some data
		graphAdd( g, "a a a;" );
		TestSubject testSubjectImpl = model.aSubject();
		assertTrue("BitOfBoth makes subjects using TestSubjectImpl",
		         testSubjectImpl instanceof TestSubjectImpl);
		TestProperty testAllImpl = testSubjectImpl.aProperty();
    	assertTrue("BitOfBoth makes properties using TestAllImpl",
    			 testAllImpl instanceof TestAllImpl);
    	assertTrue("turning a TestAllImpl into a TestSubject is a no-op",
    	          testAllImpl == testAllImpl.asSubject() );
    	assertTrue("turning a TestAllImpl into a TestSubject is a no-op",
    			  testSubjectImpl != testAllImpl.asSubject() );
    	assertTrue("turning a TestAllImpl into a TestSubject is a no-op",
    			  testSubjectImpl.asSubject() != testSubjectImpl.asSubject().asProperty().asSubject() );
    	          
    }
    
    public static void testBrokenBasic() {
    	try {
    		// Any of the tests ought to work up and til the point
    		// that they don't. At that point they need to detect the
    		// error and throw the PersonalityConfigException.
           basic("Broken: ",broken);
           fail("broken is a misconfigured personality, but it wasn't detected.");
    	} 
    	catch (PersonalityConfigException e ) {
    		
    	}
    }
    
    static class Example extends EnhNode implements RDFNode 
        {
        public Example( Node n, EnhGraph g )
            { super( n, g ); }

        static final Implementation factory = new Implementation()
            {
            @Override public EnhNode wrap( Node n, EnhGraph g ) 
                { return new EnhNode( n, g ); }
            
            @Override public boolean canWrap( Node n, EnhGraph g ) 
                { return n.isURI(); }
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
        public Object visitWith( RDFVisitor rv )
            { return null; }
        }
    
    public void testSimple()
        {
        Graph g = Factory.createGraphMem();
        Personality<RDFNode> ours = BuiltinPersonalities.model.copy().add( Example.class, Example.factory );
        EnhGraph eg = new EnhGraph( g, ours ); 
        Node n = NodeFactory.createURI( "spoo:bar" );
        EnhNode eNode = new EnhNode( NodeFactory.createURI( "spoo:bar" ), eg );
        EnhNode eBlank = new EnhNode( NodeFactory.createAnon(), eg );
        assertTrue( "URI node can be an Example", eNode.supports( Example.class ) );
        assertFalse( "Blank node cannot be an Example", eBlank.supports( Example.class ) );
        }
        
    static class AnotherExample 
        {
        static final Implementation factory = new Implementation()
            {
            @Override
            public EnhNode wrap( Node n, EnhGraph g ) { return new EnhNode( n, g ); }
            
            @Override
            public boolean canWrap( Node n, EnhGraph g ) { return n.isURI(); }
            };
        }
    
    public void testAlreadyLinkedViewException()
        {
         Graph g = Factory.createGraphMem();
         Personality<RDFNode> ours = BuiltinPersonalities.model.copy().add( Example.class, Example.factory );
         EnhGraph eg = new EnhGraph( g, ours ); 
         Node n = NodeCreateUtils.create( "spoo:bar" );
         EnhNode eNode = new Example( n, eg );
         EnhNode multiplexed = new Example( n, eg );
         multiplexed.as( Property.class );
         eNode.viewAs( Example.class );
         try
            { 
            eNode.addView( multiplexed ); 
            fail( "should raise an AlreadyLinkedViewException " );
            }
        catch (AlreadyLinkedViewException e)
            {}                
        }
        
    /**
        Test that an attempt to polymorph an enhanced node into a class that isn't
        supported by the enhanced graph generates an UnsupportedPolymorphism
        exception. 
    */
    public void testNullPointerTrap()
        {
        EnhGraph eg = new EnhGraph( Factory.createGraphMem(), new Personality<RDFNode>() ); 
        Node n = NodeCreateUtils.create( "eh:something" );
        EnhNode en = new EnhNode( n, eg );
        try 
            { 
            en.as( Property.class ); 
            fail( "oops" ); 
            }
        catch (UnsupportedPolymorphismException e) 
            {    	
            assertEquals( en, e.getBadNode() );
            assertTrue( "exception should have cuplprit graph", eg == ((EnhNode)e.getBadNode()).getGraph() );
            assertSame( "exception should have culprit class", Property.class, e.getBadClass() );
            }
        }
    
    public void testNullPointerTrapInCanSupport()
        {
        EnhGraph eg = new EnhGraph( Factory.createGraphMem(), new Personality<RDFNode>() );
        Node n = NodeCreateUtils.create( "eh:something" );
        EnhNode en = new EnhNode( n, eg );
        assertFalse( en.canAs( Property.class ) );        
        }
    
    public void testAsToOwnClassWithNoModel()
        {
        Resource r = ResourceFactory.createResource();
        assertEquals( null, r.getModel() );
        assertTrue( r.canAs( Resource.class ) );
        assertSame( r, r.as( Resource.class ) );
        }

    public void testCanAsReturnsFalseIfNoModel()
        {
        Resource r = ResourceFactory.createResource();
        assertEquals( false, r.canAs( Example.class ) ); 
        }
    
    public void testAsThrowsPolymorphismExceptionIfNoModel()
        {
        Resource r = ResourceFactory.createResource();
        try 
            { r.as( Example.class ); 
            fail( "should throw UnsupportedPolymorphismException" ); }
        catch (UnsupportedPolymorphismException e) 
            {
        	assertTrue( e.getBadNode() instanceof EnhNode );
            assertEquals( null, ((EnhNode)e.getBadNode()).getGraph() );
            assertEquals( Example.class, e.getBadClass() );
            }
        }

}
