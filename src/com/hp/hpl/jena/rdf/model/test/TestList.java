/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            24 Jan 2003
 * Filename           $RCSfile: TestList.java,v $
 * Revision           $Revision: 1.5 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-08-27 13:05:52 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2003, Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.rdf.model.test;


// Imports
///////////////
import java.util.*;

import org.apache.log4j.Logger;

import junit.framework.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.Map1;
import com.hp.hpl.jena.vocabulary.*;



/**
 * <p>
 * A collection of unit tests for the standard implementation of 
 * {@link RDFList}.
 * </p>
 * 
 * @author Ian Dickinson, HP Labs 
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: TestList.java,v 1.5 2003-08-27 13:05:52 andy_seaborne Exp $
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
        
        return s;
    }
    
    
    
    // Internal implementation methods
    //////////////////////////////////

    /** Test that an iterator delivers the expected values */
    protected static void iteratorTest( Iterator i, Object[] expected ) {
        Logger logger = Logger.getLogger( TestList.class );
        List expList = new ArrayList();
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
            for (Iterator j = expList.iterator(); j.hasNext(); ) {
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
        
            RDFList l = (RDFList) listHead.as( RDFList.class );
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
            
            l1.setHead( m.createLiteral( 42 ) );
            checkValid( "sethead2", l1, true );
            assertEquals( "List head should be '42'", 42, ((Literal) l1.getHead()).getInt() );
            
        }
    }
    
    
    protected static class SetTailTest extends ListTest {
        public SetTailTest() {super( "SetTailTest");}
        
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
            
            RDFList l2 = (RDFList) list1.as( RDFList.class );
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
        
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            
            Resource root = m.createResource( NS + "root" );
            Property p = m.createProperty( NS, "p");
            
            Resource nil = m.getResource( RDF.nil.getURI() );
            RDFList list = (RDFList) nil.as( RDFList.class );
            
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
        
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            
            Resource root = m.createResource( NS + "root" );
            Property p = m.createProperty( NS, "p");
            
            Resource nil = m.getResource( RDF.nil.getURI() );
            RDFList list = (RDFList) nil.as( RDFList.class );
            
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
        
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            
            Resource nil = m.getResource( RDF.nil.getURI() );
            RDFList list = (RDFList) nil.as( RDFList.class );
            
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
        
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            m.read( "file:testing/ontology/list5.rdf" );
           
            Resource nil = m.getResource( RDF.nil.getURI() );
            RDFList list = (RDFList) nil.as( RDFList.class );
            
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
        
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            m.read( "file:testing/ontology/list5.rdf" );
           
            Resource nil = m.getResource( RDF.nil.getURI() );
            RDFList list = (RDFList) nil.as( RDFList.class );
            
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
    
    
    protected static class ApplyTest extends ListTest {
        public ApplyTest() {super("ApplyTest");}
        
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            m.read( "file:testing/ontology/list5.rdf" );
           
            RDFList root = getListRoot( m );
            
            class MyApply implements RDFList.ApplyFn {
               String collect = "";
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
        
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            m.read( "file:testing/ontology/list5.rdf" );
           
            RDFList root = getListRoot( m );
            
            RDFList.ReduceFn f = new RDFList.ReduceFn() {
               public Object reduce( RDFNode n, Object acc ) {
                   return ((String) acc) + ((Resource) n).getLocalName();  
               } 
            };
            
            assertEquals( "Result of reduce should be concatentation of local names", "abcde", root.reduce( f, "" ) );
       }
    }
    
    protected static class Map1Test extends ListTest {
        public Map1Test() {super("Map1Test");}
        
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            m.read( "file:testing/ontology/list5.rdf" );
           
            RDFList root = getListRoot( m );
            iteratorTest( root.mapWith( new Map1() {public Object map1(Object x){return ((Resource) x).getLocalName();} } ), 
                          new Object[] {"a","b","c","d","e"} );            
        }
    }
    
    protected static class RemoveTest extends ListTest {
        public RemoveTest() {super( "RemoveTest" );}
        
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            
            Resource nil = m.getResource( RDF.nil.getURI() );
            RDFList list0 = (RDFList) nil.as( RDFList.class );
            RDFList list1 = (RDFList) nil.as( RDFList.class );
            
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
            list1.removeAll();
            
            // model should now be empty
            assertEquals( "Model should be empty after deleting two lists", 0, m.size() );
            
            // selective remove
            RDFList list2 = ((RDFList) nil.as( RDFList.class ))
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
        
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
           
            Resource nil = m.getResource( RDF.nil.getURI() );
            RDFList nilList = (RDFList) nil.as( RDFList.class );
            
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
                RDFList l0 = nilList.append( Arrays.asList( (Object[]) testSpec[i][0] ).iterator() );
                RDFList l1 = nilList.append( Arrays.asList( (Object[]) testSpec[i][1] ).iterator() );
                boolean expected = ((Boolean) testSpec[i][2]).booleanValue();
                
                assertEquals( "sameListAs testSpec[" + i + "] incorrect", expected, l0.sameListAs( l1 ) );
                assertEquals( "sameListAs testSpec[" + i + "] (swapped) incorrect", expected, l1.sameListAs( l0 ) );
            }
       }
    }
    
    /*
        // Lists
        new CreateTestCase( "OWL empty list", ProfileRegistry.OWL_LANG, OWL.nil.getURI() ) {
            public OntResource doCreate( Model m )   { return m.createList(); }
            public boolean test( OntResource r )        { return r instanceof RDFList && ((RDFList) r).size() == 0;}
        },
        new CreateTestCase( "OWL list from iterator", ProfileRegistry.OWL_LANG, null ) {
            public OntResource doCreate( Model m )   {
                OntClass a = m.createClass( NS + "A" ); 
                OntClass b = m.createClass( NS + "B" ); 
                OntClass c = m.createClass( NS + "C" );
                List l = new ArrayList();
                l.add( a );  l.add( b );  l.add( c );
                
                return m.createList( l.iterator() ); 
            }
            public boolean test( OntResource r )        { return r instanceof RDFList && ((RDFList) r).size() == 3;}
        },
        new CreateTestCase( "OWL list from array", ProfileRegistry.OWL_LANG, null ) {
            public OntResource doCreate( Model m )   {
                OntClass a = m.createClass( NS + "A" ); 
                OntClass b = m.createClass( NS + "B" ); 
                OntClass c = m.createClass( NS + "C" );
                
                return m.createList( new Resource[] {a, b, c} ); 
            }
            public boolean test( OntResource r )        { return r instanceof RDFList && ((RDFList) r).size() == 3;}
        },
        
        new CreateTestCase( "DAML empty list", ProfileRegistry.DAML_LANG, DAML_OIL.nil.getURI() ) {
            public OntResource doCreate( Model m )   { return m.createList(); }
            public boolean test( OntResource r )        { return r instanceof RDFList && ((RDFList) r).size() == 0;}
        },
        new CreateTestCase( "DAML list from iterator", ProfileRegistry.DAML_LANG, null ) {
            public OntResource doCreate( Model m )   {
                OntClass a = m.createClass( NS + "A" ); 
                OntClass b = m.createClass( NS + "B" ); 
                OntClass c = m.createClass( NS + "C" );
                List l = new ArrayList();
                l.add( a );  l.add( b );  l.add( c );
                
                return m.createList( l.iterator() ); 
            }
            public boolean test( OntResource r )        { return r instanceof RDFList && ((RDFList) r).size() == 3;}
        },
        new CreateTestCase( "DAML list from array", ProfileRegistry.DAML_LANG, null ) {
            public OntResource doCreate( Model m )   {
                OntClass a = m.createClass( NS + "A" ); 
                OntClass b = m.createClass( NS + "B" ); 
                OntClass c = m.createClass( NS + "C" );
                
                return m.createList( new Resource[] {a, b, c} ); 
            }
            public boolean test( OntResource r )        { return r instanceof RDFList && ((RDFList) r).size() == 3;}
        }
     */
}


/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
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
