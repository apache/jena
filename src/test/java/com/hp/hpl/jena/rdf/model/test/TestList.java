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

// Package
///////////////
package com.hp.hpl.jena.rdf.model.test;


// Imports
///////////////
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.*;

import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.RDFListImpl;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.iterator.Map1;
import com.hp.hpl.jena.vocabulary.*;



/**
 * <p>
 * A collection of unit tests for the standard implementation of 
 * {@link RDFList}.
 * </p>
 * 
 * @author Ian Dickinson, HP Labs 
 *         (<a  href="mailto:ian_dickinson@users.sourceforge.net" >email</a>)
 * @version CVS $Id: TestList.java,v 1.3 2010-01-11 09:17:05 chris-dollin Exp $
 */
public class TestList
    extends TestCase
{
    // Constants
    //////////////////////////////////

    public static final String NS = "uri:urn:x-rdf:test#";
    

    // Static variables
    //////////////////////////////////

    
    // Instance variables
    //////////////////////////////////


    // Constructors
    //////////////////////////////////

    public TestList( String name ) {
        super( name ); 
    }

    // External signature methods
    //////////////////////////////////

    public static TestSuite suite() {
        TestSuite s = new TestSuite( "TestList" );
        
        for (int i = 0;  i <= 5;  i++) {
            s.addTest( new CountTest( i ) );
            s.addTest( new TailTest( i ) );
        }
        
        s.addTest( new ValidityTest() );
        s.addTest( new HeadTest() );
        s.addTest( new SetHeadTest() );
        s.addTest( new SetTailTest() );
        s.addTest( new ConsTest() );
        s.addTest( new AddTest() );
        s.addTest( new TestListGet() );
        s.addTest( new ReplaceTest() );
        s.addTest( new IndexTest1() );
        s.addTest( new IndexTest2() );
        s.addTest( new AppendTest() );
        s.addTest( new ConcatenateTest() );
        s.addTest( new ConcatenateTest2() );
        s.addTest( new ApplyTest() );
        s.addTest( new ReduceTest() );
        s.addTest( new RemoveTest() );
        s.addTest( new Map1Test() );
        s.addTest( new ListEqualsTest() );
        s.addTest( new ListSubclassTest() );
        s.addTest( new UserDefinedListTest() );
        s.addTest( new CopyTest() );
        return s;
    }
    
    
    
    // Internal implementation methods
    //////////////////////////////////

    /** Test that an iterator delivers the expected values */
    protected static void iteratorTest( Iterator<?> i, Object[] expected ) {
        Logger logger = LoggerFactory.getLogger( TestList.class );
        List<Object> expList = new ArrayList<Object>();
        for (int j = 0; j < expected.length; j++) {
            expList.add( expected[j] );
        }
        
        while (i.hasNext()) {
            Object next = i.next();
                
            // debugging
            if (!expList.contains( next )) {
                logger.debug( "TestList - Unexpected iterator result: " + next );
            }
                
            assertTrue( "Value " + next + " was not expected as a result from this iterator ", expList.contains( next ) );
            assertTrue( "Value " + next + " was not removed from the list ", expList.remove( next ) );
        }
        
        if (!(expList.size() == 0)) {
            logger.debug( "TestList - Expected iterator results not found" );
            for (Iterator<Object> j = expList.iterator(); j.hasNext(); ) {
                logger.debug( "TestList - missing: " + j.next() );
            }
        }
        assertEquals( "There were expected elements from the iterator that were not found", 0, expList.size() );
    }
    
    
    //==============================================================================
    // Inner class definitions
    //==============================================================================

    protected static class ListTest extends TestCase {
        public ListTest( String n ) {super(n);}
        
        protected  void checkValid( String testName, RDFList l, boolean validExpected ) {
            l.setStrict( true );
            boolean valid = l.isValid();
            // for debugging ... String s = l.getValidityErrorMessage();
            assertEquals( "Validity test " + testName + " returned wrong isValid() result", validExpected, valid );
        }

        protected RDFList getListRoot( Model m ) {
            Resource root = m.getResource( NS + "root" );
            assertNotNull( "Root resource should not be null", root );
        
            Resource listHead = root.getRequiredProperty( m.getProperty( NS + "p" ) ).getResource();
        
            RDFList l = listHead.as( RDFList.class );
            assertNotNull( "as(RDFList) should not return null for root", l );
        
            return l;
        }
    }

    protected static class CountTest extends ListTest {
        protected int i;
        
        public CountTest( int i ) { 
            super( "CountTest" );
            this.i = i;
        }
        
        @Override
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            m.read( "file:testing/ontology/list" + i + ".rdf" );
        
            RDFList l0 = getListRoot( m );
            assertEquals( "List size should be " + i, i, l0.size() );
        }

    }
    
    
    protected static class ValidityTest extends ListTest {
        public ValidityTest() {
            super( "ValidityTest");
        }
        
        @Override
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            
            Resource root = m.createResource( NS + "root" );
            Property p = m.createProperty( NS, "p");
            
            // a list of the nil object, but not typed
            Resource nil = RDF.nil;
            m.add( root, p, nil );
            RDFList l0 = getListRoot( m );
            checkValid( "valid1", l0, true );
            
            // add another node to the head of the list
            Resource badList = m.createResource();
            m.getRequiredProperty( root, p ).remove();
            m.add( root, p, badList );
            m.add( badList, RDF.type, RDF.List );
            
            RDFList l1 = getListRoot( m );
            checkValid( "valid2", l1, false );
            
            //checkValid( "valid3", l1, false );
            
            m.add( badList, RDF.first, "fred" );
            checkValid( "valid4", l1, false );
            
            m.add( badList, RDF.rest, nil );
            checkValid( "valid5", l1, true );
        }
        
    }
    
    
    protected static class HeadTest extends ListTest {
        public HeadTest() {super( "HeadTest");}
        
        @Override
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            m.read( "file:testing/ontology/list5.rdf" );
        
            RDFList l0 = getListRoot( m );
            
            String[] names = {"a", "b", "c", "d", "e"};
            for (int i = 0;  i < names.length;  i++) {
                assertEquals( "head of list has incorrect URI", NS + names[i], ((Resource) l0.getHead()).getURI() );
                l0 = l0.getTail();
            }
        }
    }
    
    
    protected static class TailTest extends ListTest {
        protected int i;
        
        public TailTest( int i ) { 
            super( "TailTest" );
            this.i = i;
        }
        
        @Override
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            m.read( "file:testing/ontology/list" + i + ".rdf" );
        
            RDFList l0 = getListRoot( m );
            
            // get the tail n times, should be nil at the end
            for (int j = 0;  j < i;  j++) {
                l0 = l0.getTail();
            }
            
            assertTrue( "Should have reached the end of the list after " + i + " getTail()'s", l0.isEmpty() );
        }
    }
    
    
    protected static class SetHeadTest extends ListTest {
        public SetHeadTest() {super( "SetHeadTest");}
        
        @Override
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            
            Resource root = m.createResource( NS + "root" );
            Property p = m.createProperty( NS, "p");
            
            // a list of the nil object, but not typed
            Resource nil = RDF.nil;
            m.add( nil, RDF.type, RDF.List );

            Resource list = m.createResource();
            m.add( list, RDF.type, RDF.List );
            m.add( list, RDF.first, "fred" );
            m.add( list, RDF.rest, nil );
            
            m.add( root, p, list );
            RDFList l1 = getListRoot( m );
            checkValid( "sethead1", l1, true );
            
            assertEquals( "List head should be 'fred'", "fred", ((Literal) l1.getHead()).getString() );
            
            l1.setHead( m.createTypedLiteral( 42 ) );
            checkValid( "sethead2", l1, true );
            assertEquals( "List head should be '42'", 42, ((Literal) l1.getHead()).getInt() );
            
        }
    }
    
    
    protected static class SetTailTest extends ListTest {
        public SetTailTest() {super( "SetTailTest");}
        
        @Override
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            
            Resource root = m.createResource( NS + "root" );
            Property p = m.createProperty( NS, "p");
            
            Resource nil = RDF.nil;
            m.add( nil, RDF.type, RDF.List );

            Resource list0 = m.createResource();
            m.add( list0, RDF.type, RDF.List );
            m.add( list0, RDF.first, "fred" );
            m.add( list0, RDF.rest, nil );
            
            m.add( root, p, list0 );
            RDFList l1 = getListRoot( m );
            checkValid( "settail1", l1, true );
            
            Resource list1 = m.createResource();
            m.add( list1, RDF.type, RDF.List );
            m.add( list1, RDF.first, "george" );
            m.add( list1, RDF.rest, nil );
            
            RDFList l2 = list1.as( RDFList.class );
            assertNotNull( "as(RDFList) should not return null for root", l2 );
            checkValid( "settail2", l2, true );
            
            assertEquals( "l1 should have length 1", 1, l1.size() );
            assertEquals( "l2 should have length 1", 1, l2.size() );
            
            // use set tail to join the lists together
            l1.setTail( l2 );

            checkValid( "settail3", l1, true );
            checkValid( "settail4", l2, true );

            assertEquals( "l1 should have length 2", 2, l1.size() );
            assertEquals( "l2 should have length 1", 1, l2.size() );
            
        }
    }
    
    
    protected static class ConsTest extends ListTest {
        public ConsTest() {super( "ConsTest" );}
        
        @Override
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            
            Resource root = m.createResource( NS + "root" );
            Property p = m.createProperty( NS, "p");
            
            Resource nil = m.getResource( RDF.nil.getURI() );
            RDFList list = nil.as( RDFList.class );
            
            Resource[] toAdd = new Resource[] {
                                    m.createResource( NS + "e" ),
                                    m.createResource( NS + "d" ),
                                    m.createResource( NS + "c" ),
                                    m.createResource( NS + "b" ),
                                    m.createResource( NS + "a" ),
                               };
            
            // cons each of these resources onto the front of the list
            for (int i = 0;  i < toAdd.length;  i++) {
                RDFList list0 = list.cons( toAdd[i] );
                
                checkValid( "constest1", list0, true );
                assertTrue( "cons'ed lists should not be equal", !list0.equals( list ) );
                
                list = list0;
            }
            
            // relate the root to the list
            m.add( root, p, list );

            // should be isomorphic with list 5
            Model m0 = ModelFactory.createDefaultModel();
            m0.read( "file:testing/ontology/list5.rdf" );
            
            assertTrue( "Cons'ed and read models should be the same", m0.isIsomorphicWith( m ) );   
        }
    }
    
    
    protected static class AddTest extends ListTest {
        public AddTest() {super( "AddTest" );}
        
        @Override
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            
            Resource root = m.createResource( NS + "root" );
            Property p = m.createProperty( NS, "p");
            
            Resource nil = m.getResource( RDF.nil.getURI() );
            RDFList list = nil.as( RDFList.class );
            
            Resource[] toAdd = new Resource[] {
                                    m.createResource( NS + "a" ),
                                    m.createResource( NS + "b" ),
                                    m.createResource( NS + "c" ),
                                    m.createResource( NS + "d" ),
                                    m.createResource( NS + "e" ),
                               };
            
            // add each of these resources onto the end of the list
            for (int i = 0;  i < toAdd.length;  i++) {
                RDFList list0 = list.with( toAdd[i] );
                
                checkValid( "addTest0", list0, true );
                assertTrue( "added'ed lists should be equal", list.equals( nil ) || list0.equals( list ) );
                
                list = list0;
            }
            
            // relate the root to the list
            m.add( root, p, list );

            // should be isomorphic with list 5
            Model m0 = ModelFactory.createDefaultModel();
            m0.read( "file:testing/ontology/list5.rdf" );
            
            assertTrue( "Add'ed and read models should be the same", m0.isIsomorphicWith( m ) );   
        }
    }
    
    
    protected static class TestListGet extends ListTest {
        public TestListGet() {super("TestListGet");}
        
        @Override
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            m.read( "file:testing/ontology/list5.rdf" );
            
            Resource[] toGet = new Resource[] {
                                    m.createResource( NS + "a" ),
                                    m.createResource( NS + "b" ),
                                    m.createResource( NS + "c" ),
                                    m.createResource( NS + "d" ),
                                    m.createResource( NS + "e" ),
                               };
            
            RDFList l1 = getListRoot( m );

            // test normal gets
            for (int i = 0;  i < toGet.length;  i++) {
                assertEquals( "list element " + i + " is not correct", toGet[i], l1.get( i ) );
            }
            
            // now test we get an exception for going beyong the end of the list
            boolean gotEx = false;
            try {
                l1.get( toGet.length + 1 );
            }
            catch (ListIndexException e) {
                gotEx = true;
            }
            
            assertTrue( "Should see exception raised by accessing beyond end of list", gotEx );
        }
    }
    
    
    protected static class ReplaceTest extends ListTest {
        public ReplaceTest() {super("ReplaceTest");}
        
        @Override
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            m.read( "file:testing/ontology/list5.rdf" );
            
            Literal[] toSet = new Literal[] {
                                    m.createLiteral( "a" ),
                                    m.createLiteral( "b" ),
                                    m.createLiteral( "c" ),
                                    m.createLiteral( "d" ),
                                    m.createLiteral( "e" ),
                               };
            
            RDFList l1 = getListRoot( m );

            // change all the values 
            for (int i = 0;  i < toSet.length;  i++) {
                l1.replace( i, toSet[i] );
            }
            
            // then check them
            for (int i = 0;  i < toSet.length;  i++) {
                assertEquals( "list element " + i + " is not correct", toSet[i], l1.get( i ) );
            }

            // now test we get an exception for going beyong the end of the list
            boolean gotEx = false;
            try {
                l1.replace( toSet.length + 1, toSet[0] );
            }
            catch (ListIndexException e) {
                gotEx = true;
            }
            
            assertTrue( "Should see exception raised by accessing beyond end of list", gotEx );
        }
    }
    
    
    protected static class IndexTest1 extends ListTest {
        public IndexTest1() {super("IndexTest1");}
        
        @Override
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            m.read( "file:testing/ontology/list5.rdf" );
            
            Resource[] toGet = new Resource[] {
                                    m.createResource( NS + "a" ),
                                    m.createResource( NS + "b" ),
                                    m.createResource( NS + "c" ),
                                    m.createResource( NS + "d" ),
                                    m.createResource( NS + "e" ),
                               };
            
            RDFList l1 = getListRoot( m );

            // check the indexes are correct
            for (int i = 0;  i < toGet.length;  i++) {
                assertTrue( "list should contain element " + i, l1.contains( toGet[i] ) );
                assertEquals( "list element " + i + " is not correct", i, l1.indexOf( toGet[i] ) );
            }
        }
    }
    
    
    protected static class IndexTest2 extends ListTest {
        public IndexTest2() {super("IndexTest2");}
        
        @Override
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            
            Resource nil = m.getResource( RDF.nil.getURI() );
            RDFList list = nil.as( RDFList.class );
            
            Resource r = m.createResource( NS + "a" );
            
            // cons each a's onto the front of the list
            for (int i = 0;  i < 10;  i++) {
                list = list.cons( r );
            }
            
            // now index them back again
            for (int j = 0;  j < 10;  j++) {
                assertEquals( "index of j'th item should be j", j, list.indexOf( r, j ) );
            }
       }
    }
    
    
    protected static class AppendTest extends ListTest {
        public AppendTest() {super("AppendTest");}
        
        @Override
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            m.read( "file:testing/ontology/list5.rdf" );
           
            Resource nil = m.getResource( RDF.nil.getURI() );
            RDFList list = nil.as( RDFList.class );
            
            Resource r = m.createResource( NS + "foo" );
            
            // create a list of foos
            for (int i = 0;  i < 5;  i++) {
                list = list.cons( r );
            }
            
            int listLen = list.size();
            
            // now append foos to the root list
            RDFList root = getListRoot( m );
            int rootLen = root.size();
            RDFList appended = root.append( list );
            
            // original list should be unchanged
            checkValid( "appendTest0", root, true );
            assertEquals( "Original list should be unchanged", rootLen, root.size() );
            
            checkValid( "appendTest1", list, true );
            assertEquals( "Original list should be unchanged", listLen, list.size() );
            
            // new list should be length of combined 
            checkValid( "appendTest2", appended, true );
            assertEquals( "Appended list not correct length", rootLen + listLen, appended.size() );
       }
    }
    
    
    protected static class ConcatenateTest extends ListTest {
        public ConcatenateTest() {super("ConcatenateTest");}
        
        @Override
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            m.read( "file:testing/ontology/list5.rdf" );
           
            Resource nil = m.getResource( RDF.nil.getURI() );
            RDFList list = nil.as( RDFList.class );
            
            Resource r = m.createResource( NS + "foo" );
            
            // create a list of foos
            for (int i = 0;  i < 5;  i++) {
                list = list.cons( r );
            }
            
            int listLen = list.size();
            
            // now append foos to the root list
            RDFList root = getListRoot( m );
            int rootLen = root.size();
            root.concatenate( list );
            
            // original list should be unchanged
            checkValid( "concatTest0", list, true );
            assertEquals( "Original list should be unchanged", listLen, list.size() );
            
            // but lhs list has changed
            checkValid( "concatTest1", root, true );
            assertEquals( "Root list should be new length", rootLen + listLen, root.size() );
       }
    }
    
    
    protected static class ConcatenateTest2 extends ListTest {
        public ConcatenateTest2() {super("ConcatenateTest2");}
        
        @Override
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            m.read( "file:testing/ontology/list5.rdf" );
           
            Resource a = m.createResource( NS + "a" );
                        
            // create a list of foos
            Resource[] rs = new Resource[] {
                m.createResource( NS + "b" ),
                m.createResource( NS + "c" ),
                m.createResource( NS + "d" ),
                m.createResource( NS + "e" )
            };
            
            RDFList aList = m.createList().cons( a );
            RDFList rsList = m.createList( rs );

            // concatenate the above resources onto the empty list
            aList.concatenate( rsList );
            checkValid( "concatTest3", aList, true );
            
            RDFList root = getListRoot( m );
            assertTrue( "Constructed and loaded lists should be the same", aList.sameListAs( root ) );
       }
    }
    
    protected static class CopyTest extends ListTest {
        public CopyTest() {super("CopyTest");}
        
        @Override
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            // check for empty copy error (JENA-360)
            RDFList list = m.createList().copy();
            assertEquals( "Should be a 0 length list", 0, list.size() );
  
       }
    }
    
    
    protected static class ApplyTest extends ListTest {
        public ApplyTest() {super("ApplyTest");}
        
        @Override
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            m.read( "file:testing/ontology/list5.rdf" );
           
            RDFList root = getListRoot( m );
            
            class MyApply implements RDFList.ApplyFn {
               String collect = "";
               @Override
            public void apply( RDFNode n ) {
                   collect = collect + ((Resource) n).getLocalName();  
               } 
            }
            
            MyApply f = new MyApply();
            root.apply( f );
            
            assertEquals( "Result of apply should be concatentation of local names", "abcde", f.collect );
       }
    }
    
    
    protected static class ReduceTest extends ListTest {
        public ReduceTest() {super("ReduceTest");}
        
        @Override
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            m.read( "file:testing/ontology/list5.rdf" );
           
            RDFList root = getListRoot( m );
            
            RDFList.ReduceFn f = new RDFList.ReduceFn() {
               @Override
            public Object reduce( RDFNode n, Object acc ) {
                   return ((String) acc) + ((Resource) n).getLocalName();  
               } 
            };
            
            assertEquals( "Result of reduce should be concatentation of local names", "abcde", root.reduce( f, "" ) );
       }
    }
    
    protected static class Map1Test extends ListTest {
        public Map1Test() {super("Map1Test");}
        
        @Override
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            m.read( "file:testing/ontology/list5.rdf" );
           
            RDFList root = getListRoot( m );
            iteratorTest( root.mapWith( new Map1<RDFNode, String>() {@Override
            public String map1( RDFNode x ){return ((Resource) x).getLocalName();} } ), 
                          new Object[] {"a","b","c","d","e"} );            
        }
    }
    
    protected static class RemoveTest extends ListTest {
        public RemoveTest() {super( "RemoveTest" );}
        
        @Override
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            
            Resource nil = m.getResource( RDF.nil.getURI() );
            RDFList list0 = nil.as( RDFList.class );
            RDFList list1 = nil.as( RDFList.class );
            
            Resource r0 = m.createResource( NS + "x" );
            Resource r1 = m.createResource( NS + "y" );
            Resource r2 = m.createResource( NS + "z" );
            
            for (int i = 0;  i < 10;  i++) {
                list0 = list0.cons( r0 );
                list1 = list1.cons( r1 );
            }
            
            // delete the elements of list0 one at a time
            while (!list0.isEmpty()) {
                list0 = list0.removeHead();
                checkValid( "removeTest0", list0, true );
            }
            
            
            // delete all of list1 in one go
            list1.removeList();
            
            // model should now be empty
            assertEquals( "Model should be empty after deleting two lists", 0, m.size() );
            
            // selective remove
            RDFList list2 = (nil.as( RDFList.class ))
                            .cons( r2 )
                            .cons( r1 )
                            .cons( r0 );
           
            assertTrue( "list should contain x ", list2.contains( r0 ));
            assertTrue( "list should contain y ", list2.contains( r1 ));
            assertTrue( "list should contain z ", list2.contains( r2 ));
            
            list2 = list2.remove( r1 );
            assertTrue( "list should contain x ", list2.contains( r0 ));
            assertTrue( "list should contain y ", !list2.contains( r1 ));
            assertTrue( "list should contain z ", list2.contains( r2 ));
            
            list2 = list2.remove( r0 );
            assertTrue( "list should contain x ", !list2.contains( r0 ));
            assertTrue( "list should contain y ", !list2.contains( r1 ));
            assertTrue( "list should contain z ", list2.contains( r2 ));
            
            list2 = list2.remove( r2 );
            assertTrue( "list should contain x ", !list2.contains( r0 ));
            assertTrue( "list should contain y ", !list2.contains( r1 ));
            assertTrue( "list should contain z ", !list2.contains( r2 ));
            assertTrue( "list should be empty", list2.isEmpty() );
        }
    }
    
    
    protected static class ListEqualsTest extends ListTest {
        public ListEqualsTest() {super("ListEqualsTest");}
        
        @Override
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
           
            Resource nil = m.getResource( RDF.nil.getURI() );
            RDFList nilList = nil.as( RDFList.class );
            
            // create a list of foos
            Resource[] r0 = new Resource[] {
                m.createResource( NS + "a" ),   // canonical
                m.createResource( NS + "b" ),
                m.createResource( NS + "c" ),
                m.createResource( NS + "d" ),
                m.createResource( NS + "e" )
            };
            Resource[] r1 = new Resource[] {
                m.createResource( NS + "a" ),   // same
                m.createResource( NS + "b" ),
                m.createResource( NS + "c" ),
                m.createResource( NS + "d" ),
                m.createResource( NS + "e" )
            };
            Resource[] r2 = new Resource[] {
                m.createResource( NS + "a" ),   // one shorter
                m.createResource( NS + "b" ),
                m.createResource( NS + "c" ),
                m.createResource( NS + "d" )
            };
            Resource[] r3 = new Resource[] {
                m.createResource( NS + "a" ),   // elements swapped
                m.createResource( NS + "b" ),
                m.createResource( NS + "d" ),
                m.createResource( NS + "c" ),
                m.createResource( NS + "e" )
            };
            Resource[] r4 = new Resource[] {
                m.createResource( NS + "a" ),   // different name
                m.createResource( NS + "b" ),
                m.createResource( NS + "c" ),
                m.createResource( NS + "D" ),
                m.createResource( NS + "e" )
            };
            
            Object[][] testSpec = new Object[][] {
                {r0, r1, Boolean.TRUE},
                {r0, r2, Boolean.FALSE},
                {r0, r3, Boolean.FALSE},
                {r0, r4, Boolean.FALSE},
                {r1, r2, Boolean.FALSE},
                {r1, r3, Boolean.FALSE},
                {r1, r4, Boolean.FALSE},
                {r2, r3, Boolean.FALSE},
                {r2, r4, Boolean.FALSE},
            };
            
            for (int i = 0;  i < testSpec.length;  i++) {
                RDFList l0 = nilList.append( Arrays.asList( (Resource[]) testSpec[i][0] ).iterator() );
                RDFList l1 = nilList.append( Arrays.asList( (Resource[]) testSpec[i][1] ).iterator() );
                boolean expected = ((Boolean) testSpec[i][2]).booleanValue();
                
                assertEquals( "sameListAs testSpec[" + i + "] incorrect", expected, l0.sameListAs( l1 ) );
                assertEquals( "sameListAs testSpec[" + i + "] (swapped) incorrect", expected, l1.sameListAs( l0 ) );
            }
       }
    }
    
    protected static class ListSubclassTest extends ListTest {
        public ListSubclassTest() {
            super( "ListSubClassTest");
        }
        
        @Override
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            
            String NS = "http://example.org/test#";
            Resource a = m.createResource( NS + "a" );
            Resource b = m.createResource( NS + "b" );
            
            Resource cell0 = m.createResource();
            Resource cell1 = m.createResource();
            cell0.addProperty( RDF.first, a );
            cell0.addProperty( RDF.rest, cell1 );
            cell1.addProperty( RDF.first, b );
            cell1.addProperty( RDF.rest, RDF.nil );
            
            UserList ul = new UserListImpl( cell0.asNode(), (EnhGraph) m );
            
            assertEquals( "User list length ", 2, ul.size() );
            assertEquals( "head of user list ", a, ul.getHead() );
            
            RDFList l = ul.as( RDFList.class );
            assertNotNull( "RDFList facet of user-defined list subclass", l );
        }
    }
    
    /** A simple extension to RDFList to test user-subclassing of RDFList */
    protected static interface UserList extends RDFList {
    }
    
    /** Impl of a simple extension to RDFList to test user-subclassing of RDFList */
    protected static class UserListImpl extends RDFListImpl implements UserList {
        public UserListImpl( Node n, EnhGraph g ) {
            super( n, g );
        }
    }

    
    protected static class UserDefinedListTest extends ListTest {
        public UserDefinedListTest() {
            super( "UserDefinedListTest");
        }
        
        @Override
        public void runTest() {
            BuiltinPersonalities.model.add( UserDefList.class, UserDefListImpl.factoryForTests );
            
            Model m = ModelFactory.createDefaultModel();
            
            String NS = "http://example.org/test#";
            Resource a = m.createResource( NS + "a" );
            Resource b = m.createResource( NS + "b" );
            
            Resource empty = m.createResource( UserDefListImpl.NIL.getURI() );
            UserDefList ul = empty.as( UserDefList.class );
            assertNotNull( "UserList facet of empty list", ul );
            
            UserDefList ul0 = (UserDefList) ul.cons( b );
            ul0 = (UserDefList) ul0.cons( a );
            assertEquals( "should be length 2", 2, ul0.size() );
            assertTrue( "first statement", m.contains( ul0, UserDefListImpl.FIRST, a ) );
        }
    }
    
    protected static interface UserDefList extends RDFList {}
    
    protected static class UserDefListImpl extends RDFListImpl implements UserDefList {
        @SuppressWarnings("hiding") public static final String NS = "http://example.org/testlist#";
        public static final Property FIRST = ResourceFactory.createProperty( NS+"first" );
        public static final Property REST = ResourceFactory.createProperty( NS+"rest" );
        public static final Resource NIL = ResourceFactory.createResource( NS+"nil" );
        public static final Resource LIST = ResourceFactory.createResource( NS+"List" );
        
        /**
         * A factory for generating UserDefList facets from nodes in enhanced graphs.
         */
        public static Implementation factoryForTests = new Implementation() {
            @Override public EnhNode wrap( Node n, EnhGraph eg ) { 
                if (canWrap( n, eg )) {
                    UserDefListImpl impl = new UserDefListImpl( n, eg );
                    
                    Model m = impl.getModel();
                    impl.m_listFirst = FIRST.inModel( m );
                    impl.m_listRest = REST.inModel( m );
                    impl.m_listNil = NIL.inModel( m );
                    impl.m_listType = LIST.inModel( m );
                    
                    return impl;
                }
                else {
                    throw new JenaException( "Cannot convert node " + n + " to UserDefList");
                } 
            }
                
            @Override public boolean canWrap( Node node, EnhGraph eg ) {
                Graph g = eg.asGraph();
                
                return  node.equals( NIL.asNode() ) || 
                        g.contains( node, FIRST.asNode(), Node.ANY ) ||
                        g.contains( node, REST.asNode(), Node.ANY ) ||
                        g.contains( node, RDF.type.asNode(), LIST.asNode() );
            }
        };

        /** This method returns the Java class object that defines which abstraction facet is presented */
        @Override public Class<? extends RDFList> listAbstractionClass() { 
            return UserDefList.class; 
        }

        public UserDefListImpl( Node n, EnhGraph g ) {
            super( n, g );
        }
        
    }

}
