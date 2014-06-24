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

package com.hp.hpl.jena.util.iterator.test;

import java.util.List ;

import junit.framework.TestSuite ;

import com.hp.hpl.jena.rdf.model.test.ModelTestBase ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.NiceIterator ;
import com.hp.hpl.jena.util.iterator.WrappedIterator ;

public class TestAndThen extends ModelTestBase
    {
    public TestAndThen( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestAndThen.class ); }

    public void testAndThen()
        { 
        ExtendedIterator<String> L = iteratorOfStrings( "a b c" );
        ExtendedIterator<String> R = iteratorOfStrings( "d e f" );
        assertInstanceOf( NiceIterator.class, L );
        assertInstanceOf( NiceIterator.class, R );
        assertEquals( listOfStrings( "a b c d e f" ), iteratorToList( L.andThen( R ) ) );
        }
    
    public void testAndThenExtension()
        {
        ExtendedIterator<String> L = iteratorOfStrings( "a b c" );
        ExtendedIterator<String> R = iteratorOfStrings( "d e f" );
        ExtendedIterator<String> X = iteratorOfStrings( "g h i" );
        ExtendedIterator<String> LR = L.andThen( R );
        ExtendedIterator<String> LRX = LR.andThen( X );
        assertSame( LR, LRX );
        List<String> aToI = listOfStrings( "a b c d e f g h i" );
        assertEquals( aToI, iteratorToList( LRX ) );
        }
    
    public void testClosingConcatenationClosesRemainingIterators()
        {
        LoggingClosableIterator<String> L = new LoggingClosableIterator<>( iteratorOfStrings( "only" ) );
        LoggingClosableIterator<String> M = new LoggingClosableIterator<>( iteratorOfStrings( "single" ) );
        LoggingClosableIterator<String> R = new LoggingClosableIterator<>( iteratorOfStrings( "it" ) );
        ExtendedIterator<String> cat = L.andThen( M ).andThen( R );
        cat.next();
        cat.close();
        assertTrue( "middle iterator should have been closed", M.isClosed() );
        assertTrue( "final iterator should have been closed", R.isClosed() );
        }
    
    public void testRemove1()
    {
        List<String> L = listOfStrings("a b c");
        List<String> R = listOfStrings("d e f");
        
        ExtendedIterator<String> Lit = WrappedIterator.create(L.iterator());
        ExtendedIterator<String> Rit = WrappedIterator.create(R.iterator());
        
        ExtendedIterator<String> LR = Lit.andThen( Rit ) ;
        
        while (LR.hasNext())
        {
            String s = LR.next();
            
            if ("c".equals(s))
            {
                LR.hasNext();  // test for JENA-60
                LR.remove();
            }
        }
        
        assertEquals("ab", concatAsString(L));
        assertEquals("def", concatAsString(R));
    }
    
    public void testRemove2()
    {
        List<String> L = listOfStrings("a b c");
        List<String> R = listOfStrings("d e f");
        
        ExtendedIterator<String> Lit = WrappedIterator.create(L.iterator());
        ExtendedIterator<String> Rit = WrappedIterator.create(R.iterator());
        
        ExtendedIterator<String> LR = Lit.andThen( Rit ) ;
        
        while (LR.hasNext())
        {
            String s = LR.next();
            
            if ("d".equals(s))
            {
                LR.hasNext();  // test for JENA-60
                LR.remove();
            }
        }
        
        assertEquals("abc", concatAsString(L));
        assertEquals("ef", concatAsString(R));
    }
    
    public void testRemove3()
    {
        List<String> L = listOfStrings("a b c");
        List<String> R = listOfStrings("d e f");
        
        ExtendedIterator<String> Lit = WrappedIterator.create(L.iterator());
        ExtendedIterator<String> Rit = WrappedIterator.create(R.iterator());
        
        ExtendedIterator<String> LR = Lit.andThen( Rit ) ;
        
        while (LR.hasNext())
        {
            LR.next();
        }
        LR.remove();
        
        assertEquals("abc", concatAsString(L));
        assertEquals("de", concatAsString(R));
    }
    
    private String concatAsString(List<String> strings)
    {
        String toReturn = "";
        for(String s : strings)
        {
            toReturn += s;
        }
        return toReturn;
    }
    
    }
