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

package com.hp.hpl.jena.regression;

import java.util.*;

import junit.framework.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.regression.Regression.*;
import com.hp.hpl.jena.vocabulary.RDF;

public class NewRegressionSeq extends NewRegressionBase
    {
    public NewRegressionSeq( String name )
        { super( name ); }

    public static Test suite()
        { return new TestSuite( NewRegressionSeq.class ); }

    protected Model getModel()
        { return ModelFactory.createDefaultModel(); }

    protected Model m;
    
    @Override
    public void setUp()
        { m = getModel(); }
    
    @Override
    public void tearDown()
        { m = null; }
    
    public void testSeqAdd()
        {
        Seq seq = m.createSeq();
        assertEquals( 0, seq.size() );
        assertTrue( m.contains( seq, RDF.type, RDF.Seq ) );
    //    
        seq.add( tvBoolean );
        assertTrue( seq.contains( tvBoolean ) );
        assertFalse( seq.contains( !tvBoolean ) );
    //  
        seq.add( tvByte );
        assertTrue( seq.contains( tvByte ) );
        assertFalse( seq.contains( (byte) 101 ) );
    //
        seq.add( tvShort );
        assertTrue( seq.contains( tvShort ) );
        assertFalse( seq.contains( (short) 102 ) );
    //
        seq.add( tvInt );
        assertTrue( seq.contains( tvInt ) );
        assertFalse( seq.contains( -101 ) );
    //
        seq.add( tvLong );
        assertTrue( seq.contains( tvLong ) );
        assertFalse( seq.contains( -102 ) );
   //     
        seq.add( tvChar );
        assertTrue( seq.contains( tvChar ) );
        assertFalse( seq.contains( '?' ) );
   //     
        seq.add( 123.456f );
        assertTrue( seq.contains( 123.456f ) );
        assertFalse( seq.contains( 456.123f ) );
   //     
        seq.add( -123.456d );
        assertTrue( seq.contains( -123.456d ) );
        assertFalse( seq.contains( -456.123d ) );
   //     
        seq.add( "a string" );
        assertTrue( seq.contains( "a string" ) );
        assertFalse( seq.contains( "a necklace" ) );
   //     
        seq.add( m.createLiteral( "another string" ) );
        assertTrue( seq.contains( "another string" ) );
        assertFalse( seq.contains( "another necklace" ) );
   //     
        seq.add( new LitTestObj( 12345 ) );
        assertTrue( seq.contains( new LitTestObj( 12345 ) ) );
        assertFalse( seq.contains( new LitTestObj( 54321 ) ) );
   //     
//        Resource present = m.createResource( new ResTestObjF() );
//        Resource absent = m.createResource( new ResTestObjF() );
//        seq.add( present );
//        assertTrue( seq.contains( present ) );
//        assertFalse( seq.contains( absent ) );
    //    
        assertEquals( 11, seq.size() );
        }
    
    public void testSeqAddInts()
        {
        final int num = 10;
        Seq seq = m.createSeq();
        for (int i = 0; i < num; i += 1) seq.add( i );
        assertEquals( num, seq.size() );
        List<RDFNode> L = iteratorToList( seq.iterator() );
        assertEquals( num, L.size() );
        for (int i = 0; i < num; i +=1 ) 
            assertEquals( i, ((Literal) L.get(i)).getInt() );
        }
    
    public void testRemoveA()
        { testRemove( bools( "tttffffftt" ) ); }
    
    public void testRemoveB()
        { testRemove( bools( "ftftttttft" ) ); }    
    
    public void testRemoveC()
        { testRemove( bools( "ffffffffff" ) ); }
    
    protected boolean [] bools( String s )
        {
        boolean [] result = new boolean [s.length()];
        for (int i = 0; i < s.length(); i += 1) result[i] = s.charAt(i) == 't';
        return result;
        }
    
    protected void testRemove( boolean[] retain )
        {
        final int num = retain.length;
        Seq seq = m.createSeq();
        for (int i = 0; i < num; i += 1) seq.add( i );
    //                         
        List<RDFNode> retained = new ArrayList<RDFNode>();
    //    
        NodeIterator nIter = seq.iterator();
        for (int i = 0; i < num; i += 1) 
            {
            RDFNode x = nIter.nextNode();
            if (retain[i]) retained.add( x ); else nIter.remove();
            }
    //    
        assertFalse( nIter.hasNext() );                
        assertEquals( retained, iteratorToList( seq.iterator() ) );
        }

    public void testSeqAccessByIndexing()
        {
        LitTestObj tvObject = new LitTestObj(12345);
        Literal    tvLiteral = m.createLiteral("test 12 string 2");
        Resource   tvResource = m.createResource();
//        Resource   tvResObj = m.createResource(new ResTestObjF());
        Object     tvLitObj = new LitTestObj(1234);
        Bag        tvBag    = m.createBag();
        Alt        tvAlt    = m.createAlt();
        Seq        tvSeq    = m.createSeq();
    //
        Seq seq = m.createSeq();
        seq.add( true );
        assertEquals( true, seq.getBoolean( 1 ) );
        seq.add( (byte) 1 );
        assertEquals( (byte) 1, seq.getByte( 2 ) );
        seq.add( (short) 2 );
        assertEquals( (short) 2, seq.getShort( 3 ) );
        seq.add( -1 );
        assertEquals( -1, seq.getInt( 4 ) );
        seq.add( -2 );
        assertEquals( -2, seq.getLong( 5 ) );
        seq.add( '!' );
        assertEquals( '!', seq.getChar( 6 ) );
        seq.add( 123.456f );
        assertEquals( 123.456f, seq.getFloat( 7 ), 0.00005 );
        seq.add( 12345.67890 );
        assertEquals( 12345.67890, seq.getDouble( 8 ), 0.00000005 );
        seq.add( "some string" );
        assertEquals( "some string", seq.getString( 9 ) );
        seq.add( tvLitObj );
//        assertEquals( tvLitObj, seq.getObject( 10, new LitTestObjF() ) );
        seq.add( tvResource );
        assertEquals( tvResource, seq.getResource( 11 ) );
//        seq.add( tvResObj );
//        assertEquals( tvResObj, seq.getResource( 12, new ResTestObjF() ) );
        seq.add( tvLiteral );
        assertEquals( tvLiteral, seq.getLiteral( 12 ) );
        seq.add( tvBag );
        assertEquals( tvBag, seq.getBag( 13 ) );
        seq.add( tvAlt );
        assertEquals( tvAlt, seq.getAlt( 14 ) );
        seq.add( tvSeq );
        assertEquals( tvSeq, seq.getSeq( 15 ) );
    //
        try { seq.getInt( 16 ); fail( "there is no element 16" ); }
        catch (SeqIndexBoundsException e) { pass(); }
        try { seq.getInt( 0 ); fail( "there is no element 0" ); }
        catch (SeqIndexBoundsException e) { pass(); }
        }
    
    public void testSeqInsertByIndexing()
        {
        LitTestObj tvObject = new LitTestObj(12345);
        Literal    tvLiteral = m.createLiteral("test 12 string 2");
        Resource   tvResource = m.createResource();
//        Resource   tvResObj = m.createResource(new ResTestObjF());
        Object     tvLitObj = new LitTestObj(1234);
        Bag        tvBag    = m.createBag();
        Alt        tvAlt    = m.createAlt();
        Seq        tvSeq    = m.createSeq();
        
        Seq seq = m.createSeq();
        seq.add( m.createResource() );
        seq.add( 1, true );
        assertEquals( true, seq.getBoolean( 1 ) );
        seq.add( 1, (byte) 1 );
        assertEquals( (byte) 1, seq.getByte( 1 ) );
        seq.add( 1, (short) 2 );
        assertEquals( (short) 2, seq.getShort( 1 ) );
        seq.add( 1, -1 );
        assertEquals( -1, seq.getInt( 1 ) );
        seq.add( 1, -2 );
        assertEquals( -2, seq.getLong( 1 ) );
        seq.add( 1, '!' );
        assertEquals( '!', seq.getChar( 1 ) );
        seq.add( 1, 123.456f );
        assertEquals( 123.456f, seq.getFloat( 1 ), 0.00005 );
        seq.add( 1, 12345.67890 );
        assertEquals( 12345.67890, seq.getDouble( 1 ), 0.00000005 );
        seq.add( 1, "some string" );
        assertEquals( "some string", seq.getString( 1 ) );
        seq.add( 1, tvLitObj );
//        assertEquals( tvLitObj, seq.getObject( 1, new LitTestObjF() ) );
        seq.add( 1, tvResource );
        assertEquals( tvResource, seq.getResource( 1 ) );
//        seq.add( 1, tvResObj );
//        assertEquals( tvResObj, seq.getResource( 1, new ResTestObjF() ) );
        seq.add( 1, tvLiteral );
        assertEquals( tvLiteral, seq.getLiteral( 1 ) );
        seq.add( 1, tvBag );
        assertEquals( tvBag, seq.getBag( 1 ) );
        seq.add( 1, tvAlt );
        assertEquals( tvAlt, seq.getAlt( 1 ) );
        seq.add( 1, tvSeq );
        assertEquals( tvSeq, seq.getSeq( 1 ) );
    //
        assertEquals( 0, seq.indexOf( 1234543 ) );
        assertEquals( 1, seq.indexOf( tvSeq ) );
        assertEquals( 2, seq.indexOf( tvAlt ) );
        assertEquals( 3, seq.indexOf( tvBag ) );
        assertEquals( 4, seq.indexOf( tvLiteral ) );
        assertEquals( 5, seq.indexOf( tvResource ) );
        assertEquals( 6, seq.indexOf( tvLitObj ) );
        assertEquals( 7, seq.indexOf( "some string" ) );
        assertEquals( 8, seq.indexOf( 12345.67890 ) );
        assertEquals( 9, seq.indexOf( 123.456f ) );
        assertEquals( 10, seq.indexOf( '!' ) );
        assertEquals( 11, seq.indexOf( -2 ) );
        assertEquals( 12, seq.indexOf( -1 ) );
        assertEquals( 13, seq.indexOf( (short) 2 ) );
        assertEquals( 14, seq.indexOf( (byte) 1 ) );
        assertEquals( 15, seq.indexOf( true ) );
        }

    public void testMoreIndexing()
        {
        final int num = 10;
        Seq seq = m.createSeq();
        for (int i = 0; i < num; i += 1) seq.add( i );
        
        try { seq.add( 0, false ); fail( "cannot at at position 0" ); } 
        catch (SeqIndexBoundsException e) { pass(); }
        
        seq.add( num + 1, false );
        assertEquals( num + 1, seq.size() );
        
        seq.remove( num + 1 );
        try { seq.add( num + 2, false); fail( "cannot add past the end" ); } 
        catch (SeqIndexBoundsException e) { pass(); }

        int size = seq.size();
        for (int i = 1; i <= num - 1; i += 1) 
            {
            seq.add( i, 1000 + i );
            assertEquals( 1000 + i, seq.getInt( i ) );
            assertEquals( 0, seq.getInt( i + 1 ) ); 
            assertEquals( size + i, seq.size() ); 
            assertEquals( num - i - 1, seq.getInt( size ) );
            }
        }
    
    public void testSet() 
        {
        Model m = getModel();
        
        String  test = "Test16";
        int     n = 0;

        NodeIterator nIter;
        StmtIterator sIter;
        Literal    tvLiteral = m.createLiteral("test 12 string 2");
        Resource   tvResource = m.createResource();
//        Resource   tvResObj = m.createResource(new ResTestObjF());
        Bag        tvBag    = m.createBag();
        Alt        tvAlt    = m.createAlt();
        Seq        tvSeq    = m.createSeq();
        int        num=10;

        Seq seq = m.createSeq();
        for (int i = 0; i < num; i++) seq.add( i );
        
        seq.set( 5, tvBoolean );
        assertEquals( tvBoolean, seq.getBoolean( 5 ) );
        assertEquals( 3, seq.getInt( 4 ) );
        assertEquals( 5, seq.getInt( 6 ) );
        assertEquals( num, seq.size() );
        
        seq.set( 5, tvByte );
        assertEquals( tvByte, seq.getByte( 5 ) );
        assertEquals( 3, seq.getInt( 4 ) );
        assertEquals( 5, seq.getInt( 6 ) );
        assertEquals( num, seq.size() );
        
        seq.set( 5, tvShort );
        assertEquals( tvShort, seq.getShort( 5 ) );
        assertEquals( 3, seq.getInt( 4 ) );
        assertEquals( 5, seq.getInt( 6 ) );
        assertEquals( num, seq.size() );
        
        seq.set( 5, tvInt );
        assertEquals( tvInt, seq.getInt( 5 ) );
        assertEquals( 3, seq.getInt( 4 ) );
        assertEquals( 5, seq.getInt( 6 ) );
        assertEquals( num, seq.size() );
        
        seq.set( 5, tvLong );
        assertEquals( tvLong, seq.getLong( 5 ) );
        assertEquals( 3, seq.getInt( 4 ) );
        assertEquals( 5, seq.getInt( 6 ) );
        assertEquals( num, seq.size() );
        
        seq.set( 5, tvString );
        assertEquals( tvString, seq.getString( 5 ) );
        assertEquals( 3, seq.getInt( 4 ) );
        assertEquals( 5, seq.getInt( 6 ) );
        assertEquals( num, seq.size() );
        
        seq.set( 5, tvBoolean );
        assertEquals( tvBoolean, seq.getBoolean( 5 ) );
        assertEquals( 3, seq.getInt( 4 ) );
        assertEquals( 5, seq.getInt( 6 ) );
        assertEquals( num, seq.size() );
        
        seq.set( 5, tvFloat );
        assertEquals( tvFloat, seq.getFloat( 5 ), 0.00005 );
        assertEquals( 3, seq.getInt( 4 ) );
        assertEquals( 5, seq.getInt( 6 ) );
        assertEquals( num, seq.size() );
        
        seq.set( 5, tvDouble );
        assertEquals( tvDouble, seq.getDouble( 5 ), 0.000000005 );
        assertEquals( 3, seq.getInt( 4 ) );
        assertEquals( 5, seq.getInt( 6 ) );
        assertEquals( num, seq.size() );
        
        seq.set( 5, tvLiteral );
        assertEquals( tvLiteral, seq.getLiteral( 5 ) );
        assertEquals( 3, seq.getInt( 4 ) );
        assertEquals( 5, seq.getInt( 6 ) );
        assertEquals( num, seq.size() );
        
        seq.set( 5, tvResource );
        assertEquals( tvResource, seq.getResource( 5 ) );
        assertEquals( 3, seq.getInt( 4 ) );
        assertEquals( 5, seq.getInt( 6 ) );
        assertEquals( num, seq.size() );
        
        seq.set( 5, tvLitObj );
//        assertEquals( tvLitObj, seq.getObject( 5, new LitTestObjF() ) );
        assertEquals( 3, seq.getInt( 4 ) );
        assertEquals( 5, seq.getInt( 6 ) );
        assertEquals( num, seq.size() );
        
//        seq.set( 5, tvResObj );
//        assertEquals( tvResObj, seq.getResource( 5, new ResTestObjF() ) );
//        assertEquals( 3, seq.getInt( 4 ) );
//        assertEquals( 5, seq.getInt( 6 ) );
//        assertEquals( num, seq.size() );
        }
    }
