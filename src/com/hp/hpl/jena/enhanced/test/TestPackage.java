/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestPackage.java,v 1.2 2003-01-28 16:21:41 chris-dollin Exp $
*/
/*
 * EnhancedTestSuite.java
 *
 * Created on 27 November 2002, 04:53
 */

package com.hp.hpl.jena.enhanced.test;
import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.graph.*;
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
 *(For Jena2.0 I am imagining that there will be ModelCom and DAMLModelImpl as
 *the only two implementations, and they can inherit one from the other).
 * @author  jjc
 */
public class TestPackage extends GraphTestBase implements SPO {
    
	static final private  GraphPersonality split = new GraphPersonality();
        
	static final private GraphPersonality combo = new GraphPersonality();
        
        
	static final private GraphPersonality bitOfBoth = new GraphPersonality();
	static final private GraphPersonality broken = new GraphPersonality();
	static {
            // Setting up the personalities, involves registering how
            // each interface is implemented by default.
            // Note this does not guarantee that the only implementations
            // of each interface will be the one specified.
            // See bitOfBoth.
        split.add( TestObject.type, TestObjectImpl.factory );
        split.add( TestSubject.type, TestSubjectImpl.factory );
        split.add( TestProperty.type, TestPropertyImpl.factory );
        
        combo.add( TestObject.type, TestAllImpl.factory );
        combo.add( TestSubject.type, TestAllImpl.factory );
        combo.add( TestProperty.type, TestAllImpl.factory );
        
        bitOfBoth.add( TestObject.type, TestObjectImpl.factory );
        bitOfBoth.add( TestSubject.type, TestSubjectImpl.factory );
        bitOfBoth.add( TestProperty.type, TestAllImpl.factory );
        
        // broken is misconfigured and must throw an exception.
        broken.add(TestObject.type, TestObjectImpl.factory );
        broken.add( TestSubject.type, TestSubjectImpl.factory );
        broken.add( TestProperty.type, TestObjectImpl.factory );
	}
    /** Creates a new instance of EnhancedTestSuite */
   	public TestPackage(String name)
		{
		super( name );
		};
		
    public static TestSuite suite()
        { TestSuite suite = new TestSuite( "Enhanced" ); 
          
  

        // add all the tests defined in this class to the suite
        /* */ suite.addTest( new TestPackage( "testSplitBasic" ) );    /* */
        /* */ suite.addTest( new TestPackage( "testComboBasic" ) );    /* */
        /* */ suite.addTest( new TestPackage( "testSplitFollow" ) );   /* */
        /* */ suite.addTest( new TestPackage( "testComboFollow" ) );   /* */
        /* */ suite.addTest( new TestPackage( "testSplitCache" ) );    /* */
        /* */ suite.addTest( new TestPackage( "testComboCache" ) );    /* */
        /* */ suite.addTest( new TestPackage( "testBitOfBothBasic" ) );/* */
        /* */ suite.addTest( new TestPackage( "testBitOfBothFollow" ) );  /* */
        /* */ suite.addTest( new TestPackage( "testBitOfBothCache" ) );   /* */
        /* */ suite.addTest( new TestPackage( "testBitOfBothSurprise" ) ); /* */
        /* */ suite.addTest( new TestPackage( "testBrokenBasic" ) );  /* */

        return suite;
        }
    /**
     * View n as intf. This is supported iff rslt.
     */
    private static void miniAsSupports(String title, TestNode n, Type intf, boolean rslt ) {
        assertTrue(title +":sanity",n instanceof Polymorphic);
        
        // It is always possible to view any node with any interface.
        TestNode as1 = (TestNode)((EnhNode)n).as(intf);
        TestNode as2 = (TestNode)((EnhNode)n).as(intf);
        
        // caching should ensure we get the same result both times.
        assertTrue( title + ":idempotency", as1==as2 );
        
        // Whether the interface is actually useable depends on the underlying
        // graph. This factoid is the rslt parameter.
        assertEquals( title +":support",rslt,intf.supportedBy((EnhNode)as1) );
    }
    
    private static void oneNodeAsSupports(String title, TestNode n, boolean rslts[] ) {
    	// Try n with all three interfaces.
        miniAsSupports(title+"/TestSubject",n,TestSubject.type,rslts[0]);
        miniAsSupports(title+"/TestProperty",n,TestProperty.type,rslts[1]);
        miniAsSupports(title+"/TestObject",n,TestObject.type,rslts[2]);
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
    private static void basic(String title, Personality p) {
        Graph g = new GraphMem();
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

    // This is like the earlier test: miniAsSupports (the last part of it).
    // However, this time instead of asking whether the interface will work
    // or not, we just try it.
    // Obviously sometimes it is broken, which should be reported using
    // an IllegalStateException.
	private static void canImplement(String title, TestNode n, int wh, boolean rslt ) {
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

	private static void canImplement(String title, TestNode n, boolean rslts[] ) {
		canImplement(title+"/TestSubject",n,S,rslts[0]);
		canImplement(title+"/TestProperty",n,P,rslts[1]);
		canImplement(title+"/TestObject",n,O,rslts[2]);
	}
	private static void canImplement(String title, TestNode n[], boolean rslts[][] ) {
		for (int i=0;i<n.length;i++){
		  canImplement(title+"["+i+"]",n[i],rslts[i]);
		}
	}
	
    private static void follow(String title, Personality p) {
        Graph g = new GraphMem();
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
    private static void cache(String title, Personality p) {
        Graph g = new GraphMem();
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
    public static void testSplitFollow() {
       follow("Split: ",split);
    }
    public static void testComboFollow() {
     follow("Combo: ",combo);
    }
    
    public static void testSplitCache() {
        cache("Split: ",split);
    }
    public static void testComboCache() {
     cache("Combo: ",combo);
    }
    
    public static void testBitOfBothBasic() {
       basic("bob: ",bitOfBoth);
    }
    public static void testBitOfBothFollow() {
       follow("bob: ",bitOfBoth);
    }
    
    public static void testBitOfBothCache() {
        cache("bob: ",bitOfBoth);
    }
    
    public static void testBitOfBothSurprise() {
    	// bitOfBoth is a surprising personality ...
    	// we can have two different java objects implementing the same interface.
    	
		Graph g = new GraphMem();
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

}

/*
    (c) Copyright Hewlett-Packard Company 2002
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
