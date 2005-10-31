/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: NewRegressionSeq.java,v 1.2 2005-10-31 15:13:13 chris-dollin Exp $
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
    
    public void setUp()
        { m = getModel(); }
    
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
        assertFalse( seq.contains( (int) -101 ) );
    //
        seq.add( tvLong );
        assertTrue( seq.contains( tvLong ) );
        assertFalse( seq.contains( (long) -102 ) );
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
        Resource present = m.createResource( new ResTestObjF() );
        Resource absent = m.createResource( new ResTestObjF() );
        seq.add( present );
        assertTrue( seq.contains( present ) );
        assertFalse( seq.contains( absent ) );
    //    
        assertEquals( 12, seq.size() );
        }
    
    public void testSeqAddInts()
        {
        final int num = 10;
        Seq seq = m.createSeq();
        for (int i = 0; i < num; i += 1) seq.add( i );
        assertEquals( num, seq.size() );
        List L = iteratorToList( seq.iterator() );
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
        List retained = new ArrayList();
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
        Resource   tvResObj = m.createResource(new ResTestObjF());
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
        seq.add( (int) -1 );
        assertEquals( (int) -1, seq.getInt( 4 ) );
        seq.add( (long) -2 );
        assertEquals( (long) -2, seq.getLong( 5 ) );
        seq.add( '!' );
        assertEquals( '!', seq.getChar( 6 ) );
        seq.add( 123.456f );
        assertEquals( 123.456f, seq.getFloat( 7 ), 0.00005 );
        seq.add( 12345.67890 );
        assertEquals( 12345.67890, seq.getDouble( 8 ), 0.00000005 );
        seq.add( "some string" );
        assertEquals( "some string", seq.getString( 9 ) );
        seq.add( tvLitObj );
        assertEquals( tvLitObj, seq.getObject( 10, new LitTestObjF() ) );
        seq.add( tvResource );
        assertEquals( tvResource, seq.getResource( 11 ) );
        seq.add( tvResObj );
        assertEquals( tvResObj, seq.getResource( 12, new ResTestObjF() ) );
        seq.add( tvLiteral );
        assertEquals( tvLiteral, seq.getLiteral( 13 ) );
        seq.add( tvBag );
        assertEquals( tvBag, seq.getBag( 14 ) );
        seq.add( tvAlt );
        assertEquals( tvAlt, seq.getAlt( 15 ) );
        seq.add( tvSeq );
        assertEquals( tvSeq, seq.getSeq( 16 ) );
    //
        try { seq.getInt( 17 ); fail( "there is no element 17" ); }
        catch (SeqIndexBoundsException e) { pass(); }
        try { seq.getInt( 0 ); fail( "there is no element 0" ); }
        catch (SeqIndexBoundsException e) { pass(); }
        }
    
    public void testSeqInsertByIndexing()
        {
        LitTestObj tvObject = new LitTestObj(12345);
        Literal    tvLiteral = m.createLiteral("test 12 string 2");
        Resource   tvResource = m.createResource();
        Resource   tvResObj = m.createResource(new ResTestObjF());
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
        seq.add( 1, (int) -1 );
        assertEquals( (int) -1, seq.getInt( 1 ) );
        seq.add( 1, (long) -2 );
        assertEquals( (long) -2, seq.getLong( 1 ) );
        seq.add( 1, '!' );
        assertEquals( '!', seq.getChar( 1 ) );
        seq.add( 1, 123.456f );
        assertEquals( 123.456f, seq.getFloat( 1 ), 0.00005 );
        seq.add( 1, 12345.67890 );
        assertEquals( 12345.67890, seq.getDouble( 1 ), 0.00000005 );
        seq.add( 1, "some string" );
        assertEquals( "some string", seq.getString( 1 ) );
        seq.add( 1, tvLitObj );
        assertEquals( tvLitObj, seq.getObject( 1, new LitTestObjF() ) );
        seq.add( 1, tvResource );
        assertEquals( tvResource, seq.getResource( 1 ) );
        seq.add( 1, tvResObj );
        assertEquals( tvResObj, seq.getResource( 1, new ResTestObjF() ) );
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
        assertEquals( 6, seq.indexOf( tvResource ) );
        assertEquals( 7, seq.indexOf( tvLitObj ) );
        assertEquals( 8, seq.indexOf( "some string" ) );
        assertEquals( 9, seq.indexOf( 12345.67890 ) );
        assertEquals( 10, seq.indexOf( 123.456f ) );
        assertEquals( 11, seq.indexOf( '!' ) );
        assertEquals( 12, seq.indexOf( (long) -2 ) );
        assertEquals( 13, seq.indexOf( (int) -1 ) );
        assertEquals( 14, seq.indexOf( (short) 2 ) );
        assertEquals( 15, seq.indexOf( (byte) 1 ) );
        assertEquals( 16, seq.indexOf( true ) );
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
        Resource   tvResObj = m.createResource(new ResTestObjF());
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
        assertEquals( tvLitObj, seq.getObject( 5, new LitTestObjF() ) );
        assertEquals( 3, seq.getInt( 4 ) );
        assertEquals( 5, seq.getInt( 6 ) );
        assertEquals( num, seq.size() );
        
        seq.set( 5, tvResObj );
        assertEquals( tvResObj, seq.getResource( 5, new ResTestObjF() ) );
        assertEquals( 3, seq.getInt( 4 ) );
        assertEquals( 5, seq.getInt( 6 ) );
        assertEquals( num, seq.size() );
        }
    }


/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/