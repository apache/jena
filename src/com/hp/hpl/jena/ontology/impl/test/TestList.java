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
 * Last modified on   $Date: 2003-03-26 12:13:45 $
 *               by   $Author: chris-dollin $
 *
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl.test;


// Imports
///////////////
import java.util.Arrays;

import junit.framework.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.impl.*;
import com.hp.hpl.jena.vocabulary.*;



/**
 * <p>
 * A collection of unit tests for the standard implementation of {@link
 * OntList}.
 * </p>
 * 
 * @author Ian Dickinson, HP Labs 
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: TestList.java,v 1.5 2003-03-26 12:13:45 chris-dollin Exp $
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
        s.addTest( new ListEqualsTest() );
        
        return s;
    }
    
    
    
    // Internal implementation methods
    //////////////////////////////////

    
    //==============================================================================
    // Inner class definitions
    //==============================================================================

    protected static class ListTest extends TestCase {
        public ListTest( String n ) {super(n);}
        
        protected  void checkValid( String testName, OntList l, boolean validExpected ) {
            l.setStrict( true );
            boolean valid = l.isValid();
            // for debugging ... String s = l.getValidityErrorMessage();
            assertEquals( "Validity test " + testName + " returned wrong isValid() result", validExpected, valid );
        }

        protected OntList getListRoot( Model m ) {
            Resource root = m.getResource( NS + "root" );
            assertNotNull( "Root resource should not be null", root );
        
            Resource listHead = root.getProperty( m.getProperty( NS + "p" ) ).getResource();
        
            // @todo make this cast go away once the proper connection from model layer -> enhanced layer is defined
            OntList l = (OntList) listHead.as( OntList.type );
            assertNotNull( "as(OntList) should not return null for root", l );
        
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
        
            OntList l0 = getListRoot( m );
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
            Resource nil = OntListImpl.RDF_LIST_VOCAB.getNil();
            m.add( root, p, nil );
            OntList l0 = getListRoot( m );
            checkValid( "valid1", l0, true );
            
            // add another node to the head of the list
            Resource badList = m.createResource();
            m.getProperty( root, p ).remove();
            m.add( root, p, badList );
            
            OntList l1 = getListRoot( m );
            checkValid( "valid2", l1, false );
            
            m.add( badList, RDF.type, RDF.List );
            checkValid( "valid3", l1, false );
            
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
        
            OntList l0 = getListRoot( m );
            
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
        
            OntList l0 = getListRoot( m );
            
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
            Resource nil = OntListImpl.RDF_LIST_VOCAB.getNil();
            m.add( nil, RDF.type, OntListImpl.RDF_LIST_VOCAB.getCellType() );

            Resource list = m.createResource();
            m.add( list, RDF.type, RDF.List );
            m.add( list, RDF.first, "fred" );
            m.add( list, RDF.rest, nil );
            
            m.add( root, p, list );
            OntList l1 = getListRoot( m );
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
            
            Resource nil = OntListImpl.RDF_LIST_VOCAB.getNil();
            m.add( nil, RDF.type, OntListImpl.RDF_LIST_VOCAB.getCellType() );

            Resource list0 = m.createResource();
            m.add( list0, RDF.type, RDF.List );
            m.add( list0, RDF.first, "fred" );
            m.add( list0, RDF.rest, nil );
            
            m.add( root, p, list0 );
            OntList l1 = getListRoot( m );
            checkValid( "settail1", l1, true );
            
            Resource list1 = m.createResource();
            m.add( list1, RDF.type, RDF.List );
            m.add( list1, RDF.first, "george" );
            m.add( list1, RDF.rest, nil );
            
            OntList l2 = (OntList) list1.as( OntList.type );
            assertNotNull( "as(OntList) should not return null for root", l2 );
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
            
            Resource nil = m.getResource( OntListImpl.RDF_LIST_VOCAB.getNil().getURI() );
            OntList list = (OntList) nil.as( OntList.type );
            
            Resource[] toAdd = new Resource[] {
                                    m.createResource( NS + "e" ),
                                    m.createResource( NS + "d" ),
                                    m.createResource( NS + "c" ),
                                    m.createResource( NS + "b" ),
                                    m.createResource( NS + "a" ),
                               };
            
            // cons each of these resources onto the front of the list
            for (int i = 0;  i < toAdd.length;  i++) {
                OntList list0 = list.cons( toAdd[i] );
                
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
            
            Resource nil = m.getResource( OntListImpl.RDF_LIST_VOCAB.getNil().getURI() );
            OntList list = (OntList) nil.as( OntList.type );
            
            Resource[] toAdd = new Resource[] {
                                    m.createResource( NS + "a" ),
                                    m.createResource( NS + "b" ),
                                    m.createResource( NS + "c" ),
                                    m.createResource( NS + "d" ),
                                    m.createResource( NS + "e" ),
                               };
            
            // cons each of these resources onto the front of the list
            for (int i = 0;  i < toAdd.length;  i++) {
                OntList list0 = list.add( toAdd[i] );
                
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
            
            OntList l1 = getListRoot( m );

            // test normal gets
            for (int i = 0;  i < toGet.length;  i++) {
                assertEquals( "list element " + i + " is not correct", toGet[i], l1.get( i ) );
            }
            
            // now test we get an exception for going beyong the end of the list
            boolean gotEx = false;
            try {
                RDFNode n = l1.get( toGet.length + 1 );
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
            
            OntList l1 = getListRoot( m );

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
                RDFNode n = l1.replace( toSet.length + 1, toSet[0] );
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
            
            OntList l1 = getListRoot( m );

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
            
            Resource nil = m.getResource( OntListImpl.RDF_LIST_VOCAB.getNil().getURI() );
            OntList list = (OntList) nil.as( OntList.type );
            
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
           
            Resource nil = m.getResource( OntListImpl.RDF_LIST_VOCAB.getNil().getURI() );
            OntList list = (OntList) nil.as( OntList.type );
            
            Resource r = m.createResource( NS + "foo" );
            
            // create a list of foos
            for (int i = 0;  i < 5;  i++) {
                list = list.cons( r );
            }
            
            int listLen = list.size();
            
            // now append foos to the root list
            OntList root = getListRoot( m );
            int rootLen = root.size();
            OntList appended = root.append( list );
            
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
           
            Resource nil = m.getResource( OntListImpl.RDF_LIST_VOCAB.getNil().getURI() );
            OntList list = (OntList) nil.as( OntList.type );
            
            Resource r = m.createResource( NS + "foo" );
            
            // create a list of foos
            for (int i = 0;  i < 5;  i++) {
                list = list.cons( r );
            }
            
            int listLen = list.size();
            
            // now append foos to the root list
            OntList root = getListRoot( m );
            int rootLen = root.size();
            OntList concatted = root.concatenate( list );
            
            // original list should be unchanged
            checkValid( "concatTest0", list, true );
            assertEquals( "Original list should be unchanged", listLen, list.size() );
            
            // but lhs list has changed
            checkValid( "concatTest1", root, true );
            assertEquals( "Root list should be new length", rootLen + listLen, root.size() );
            
            // new list should be length of combined 
            checkValid( "appendTest2", concatted, true );
            assertEquals( "Concatted list not correct length", rootLen + listLen, concatted.size() );
            
            // concat and lhs list are the same
            assertEquals( "Result list and subject list should be the same", root, concatted );
       }
    }
    
    
    protected static class ConcatenateTest2 extends ListTest {
        public ConcatenateTest2() {super("ConcatenateTest2");}
        
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            m.read( "file:testing/ontology/list5.rdf" );
           
            Resource nil = m.getResource( OntListImpl.RDF_LIST_VOCAB.getNil().getURI() );
            OntList nilList = (OntList) nil.as( OntList.type );
            
            // create a list of foos
            Resource[] rs = new Resource[] {
                m.createResource( NS + "a" ),
                m.createResource( NS + "b" ),
                m.createResource( NS + "c" ),
                m.createResource( NS + "d" ),
                m.createResource( NS + "e" )
            };
            
            // concatenate the above resources onto the empty list
            OntList list = nilList.concatenate( Arrays.asList( rs ).iterator() );
            checkValid( "concatTest3", list, true );
            
            OntList root = getListRoot( m );
            assertTrue( "Constructed and loaded lists should be the same", list.sameListAs( root ) );
       }
    }
    
    
    protected static class ApplyTest extends ListTest {
        public ApplyTest() {super("ApplyTest");}
        
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            m.read( "file:testing/ontology/list5.rdf" );
           
            OntList root = getListRoot( m );
            
            class MyApply implements OntList.ApplyFn {
               String collect = "";
               public void apply( RDFNode n ) {
                   collect = collect + ((Resource) n).getLocalName();  
               } 
            };
            
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
           
            OntList root = getListRoot( m );
            
            OntList.ReduceFn f = new OntList.ReduceFn() {
               public Object reduce( RDFNode n, Object acc ) {
                   return ((String) acc) + ((Resource) n).getLocalName();  
               } 
            };
            
            assertEquals( "Result of reduce should be concatentation of local names", "abcde", root.reduce( f, "" ) );
       }
    }
    
    
    protected static class RemoveTest extends ListTest {
        public RemoveTest() {super( "RemoveTest" );}
        
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
            
            Resource nil = m.getResource( OntListImpl.RDF_LIST_VOCAB.getNil().getURI() );
            OntList list0 = (OntList) nil.as( OntList.type );
            OntList list1 = (OntList) nil.as( OntList.type );
            
            Resource r0 = m.createResource( NS + "x" );
            Resource r1 = m.createResource( NS + "y" );
            
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
        }
    }
    
    
    protected static class ListEqualsTest extends ListTest {
        public ListEqualsTest() {super("ListEqualsTest");}
        
        public void runTest() {
            Model m = ModelFactory.createDefaultModel();
           
            Resource nil = m.getResource( OntListImpl.RDF_LIST_VOCAB.getNil().getURI() );
            OntList nilList = (OntList) nil.as( OntList.type );
            
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
                OntList l0 = nilList.concatenate( Arrays.asList( (Object[]) testSpec[i][0] ).iterator() );
                OntList l1 = nilList.concatenate( Arrays.asList( (Object[]) testSpec[i][1] ).iterator() );
                boolean expected = ((Boolean) testSpec[i][2]).booleanValue();
                
                assertEquals( "sameListAs testSpec[" + i + "] incorrect", expected, l0.sameListAs( l1 ) );
                assertEquals( "sameListAs testSpec[" + i + "] (swapped) incorrect", expected, l1.sameListAs( l0 ) );
            }
       }
    }
    
    
}


/*
    (c) Copyright Hewlett-Packard Company 2003
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
