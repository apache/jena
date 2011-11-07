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

import junit.framework.TestSuite;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.regression.Regression.*;
import com.hp.hpl.jena.vocabulary.RDF;

public class NewRegressionSeqMethods extends NewRegressionContainerMethods
    {
    public NewRegressionSeqMethods( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( NewRegressionSeqMethods.class ); }
    
    @Override
    protected Container createContainer()
        { return m.createSeq(); }

    @Override
    protected Resource getContainerType()
        { return RDF.Seq; }
    
    public void error( String test, int n )
        { fail( test + " -- " + n ); }
        
    protected LitTestObj aLitTestObj;
    protected Literal tvLiteral;
    protected Resource tvResource;
//    protected Resource tvResObj;
    protected Object anObject;
    protected Bag tvBag;
    protected Alt tvAlt;
    protected Seq tvSeq;
    
    protected static final String lang = "fr";
    protected static final int num = 10;
    
    @Override
    public void setUp()
        {
        super.setUp();
        aLitTestObj = new LitTestObj( 12345 );
        tvLiteral = m.createLiteral( "test 12 string 2" );
        tvResource = m.createResource();
//        tvResObj = m.createResource( new ResTestObjF() );
        anObject = new LitTestObj( 1234 );
        tvBag = m.createBag();
        tvAlt = m.createAlt();
        tvSeq = m.createSeq();
        }
    
    public void testSeq7()
        {
        Seq seq7 = m.createSeq();
        String test = "seq7";
        int n = 0;
        n = (n / 100) * 100 + 100;
        for (int i = 0; i < num; i++)
            {
            seq7.add( i );
            }
        n = (n / 100) * 100 + 100;
        seq7.set( 5, tvBoolean );
        n++;
        if (!(seq7.getBoolean( 5 ) == tvBoolean)) error( test, n );
        n++;
        if (!(seq7.getInt( 4 ) == 3)) error( test, n );
        n++;
        if (!(seq7.getInt( 6 ) == 5)) error( test, n );
        n++;
        if (!(seq7.size() == num)) error( test, n );
        n = (n / 100) * 100 + 100;
        seq7.set( 5, tvByte );
        n++;
        if (!(seq7.getByte( 5 ) == tvByte)) error( test, n );
        n++;
        if (!(seq7.getInt( 4 ) == 3)) error( test, n );
        n++;
        if (!(seq7.getInt( 6 ) == 5)) error( test, n );
        n++;
        if (!(seq7.size() == num)) error( test, n );
        n = (n / 100) * 100 + 100;
        seq7.set( 5, tvShort );
        n++;
        if (!(seq7.getShort( 5 ) == tvShort)) error( test, n );
        n++;
        if (!(seq7.getInt( 4 ) == 3)) error( test, n );
        n++;
        if (!(seq7.getInt( 6 ) == 5)) error( test, n );
        n++;
        if (!(seq7.size() == num)) error( test, n );
        n = (n / 100) * 100 + 100;
        seq7.set( 5, tvInt );
        n++;
        if (!(seq7.getInt( 5 ) == tvInt)) error( test, n );
        n++;
        if (!(seq7.getInt( 4 ) == 3)) error( test, n );
        n++;
        if (!(seq7.getInt( 6 ) == 5)) error( test, n );
        n++;
        if (!(seq7.size() == num)) error( test, n );
        n = (n / 100) * 100 + 100;
        seq7.set( 5, tvLong );
        n++;
        if (!(seq7.getLong( 5 ) == tvLong)) error( test, n );
        n++;
        if (!(seq7.getInt( 4 ) == 3)) error( test, n );
        n++;
        if (!(seq7.getInt( 6 ) == 5)) error( test, n );
        n++;
        if (!(seq7.size() == num)) error( test, n );
        n = (n / 100) * 100 + 100;
        seq7.set( 5, tvChar );
        n++;
        if (!(seq7.getChar( 5 ) == tvChar)) error( test, n );
        n++;
        if (!(seq7.getInt( 4 ) == 3)) error( test, n );
        n++;
        if (!(seq7.getInt( 6 ) == 5)) error( test, n );
        n++;
        if (!(seq7.size() == num)) error( test, n );
        n = (n / 100) * 100 + 100;
        seq7.set( 5, tvFloat );
        n++;
        if (!(seq7.getFloat( 5 ) == tvFloat)) error( test, n );
        n++;
        if (!(seq7.getInt( 4 ) == 3)) error( test, n );
        n++;
        if (!(seq7.getInt( 6 ) == 5)) error( test, n );
        n++;
        if (!(seq7.size() == num)) error( test, n );
        n = (n / 100) * 100 + 100;
        seq7.set( 5, tvDouble );
        n++;
        if (!(seq7.getDouble( 5 ) == tvDouble)) error( test, n );
        n++;
        if (!(seq7.getInt( 4 ) == 3)) error( test, n );
        n++;
        if (!(seq7.getInt( 6 ) == 5)) error( test, n );
        n++;
        if (!(seq7.size() == num)) error( test, n );
        n = (n / 100) * 100 + 100;
        seq7.set( 5, tvString );
        n++;
        if (!(seq7.getString( 5 ).equals( tvString ))) error( test, n );
        n++;
        if (!(seq7.getLanguage( 5 ).equals( "" ))) error( test, n );
        n++;
        if (!(seq7.getInt( 4 ) == 3)) error( test, n );
        n++;
        if (!(seq7.getInt( 6 ) == 5)) error( test, n );
        n++;
        if (!(seq7.size() == num)) error( test, n );
        seq7.set( 5, tvString, lang );
        n++;
        if (!(seq7.getString( 5 ).equals( tvString ))) error( test, n );
        n++;
        if (!(seq7.getLanguage( 5 ).equals( lang ))) error( test, n );
        n++;
        if (!(seq7.getInt( 4 ) == 3)) error( test, n );
        n++;
        if (!(seq7.getInt( 6 ) == 5)) error( test, n );
        n++;
        if (!(seq7.size() == num)) error( test, n );
        n = (n / 100) * 100 + 100;
        seq7.set( 5, tvLiteral );
        n++;
        if (!(seq7.getLiteral( 5 ).equals( tvLiteral ))) error( test, n );
        n++;
        if (!(seq7.getInt( 4 ) == 3)) error( test, n );
        n++;
        if (!(seq7.getInt( 6 ) == 5)) error( test, n );
        n++;
        if (!(seq7.size() == num)) error( test, n );
        n = (n / 100) * 100 + 100;
        seq7.set( 5, tvResource );
        n++;
        if (!(seq7.getResource( 5 ).equals( tvResource ))) error( test, n );
        n++;
        if (!(seq7.getInt( 4 ) == 3)) error( test, n );
        n++;
        if (!(seq7.getInt( 6 ) == 5)) error( test, n );
        n++;
        if (!(seq7.size() == num)) error( test, n );
        n = (n / 100) * 100 + 100;
        seq7.set( 5, anObject );
        n++;
//        if (!(seq7.getObject( 5, new LitTestObjF() )).equals( anObject )) error(
//                test, n );
        n++;
        if (!(seq7.getInt( 4 ) == 3)) error( test, n );
        n++;
        if (!(seq7.getInt( 6 ) == 5)) error( test, n );
        n++;
        if (!(seq7.size() == num)) error( test, n );
        n = (n / 100) * 100 + 100;
//        seq7.set( 5, tvResObj );
//        n++;
//        if (!(seq7.getResource( 5, new ResTestObjF() ).equals( tvResObj ))) error(
//                test, n );
        n++;
        if (!(seq7.getInt( 4 ) == 3)) error( test, n );
        n++;
        if (!(seq7.getInt( 6 ) == 5)) error( test, n );
        n++;
        if (!(seq7.size() == num)) error( test, n );
        }

    public void testSeq6()
        {
        String test = "seq6";
        int n = 0;
        Seq seq6 = m.createSeq();
        seq6.add( m.createResource() );
        seq6.add( 1, tvBoolean );
        n++;
        if (!(seq6.getBoolean( 1 ) == tvBoolean)) error( test, n );
        seq6.add( 1, tvByte );
        n++;
        if (!(seq6.getByte( 1 ) == tvByte)) error( test, n );
        seq6.add( 1, tvShort );
        n++;
        if (!(seq6.getShort( 1 ) == tvShort)) error( test, n );
        seq6.add( 1, tvInt );
        n++;
        if (!(seq6.getInt( 1 ) == tvInt)) error( test, n );
        seq6.add( 1, tvLong );
        n++;
        if (!(seq6.getLong( 1 ) == tvLong)) error( test, n );
        seq6.add( 1, tvChar );
        n++;
        if (!(seq6.getChar( 1 ) == tvChar)) error( test, n );
        seq6.add( 1, tvFloat );
        n++;
        if (!(seq6.getFloat( 1 ) == tvFloat)) error( test, n );
        seq6.add( 1, tvDouble );
        n++;
        if (!(seq6.getDouble( 1 ) == tvDouble)) error( test, n );
        seq6.add( 1, tvString );
        n++;
        if (!(seq6.getString( 1 ).equals( tvString ))) error( test, n );
        seq6.add( 1, tvString, lang );
        n++;
        if (!(seq6.getString( 1 ).equals( tvString ))) error( test, n );
        seq6.add( 1, tvResource );
        n++;
        if (!(seq6.getResource( 1 ).equals( tvResource ))) error( test, n );
        seq6.add( 1, tvLiteral );
        n++;
        if (!(seq6.getLiteral( 1 ).equals( tvLiteral ))) error( test, n );
        seq6.add( 1, anObject );
        n++;
//        if (!(seq6.getObject( 1, new LitTestObjF() ).equals( anObject ))) error(
//                test, n );

        n = (n / 100) * 100 + 100;
        n++;
        if (!(seq6.indexOf( anObject ) == 1)) error( test, n );
        n++;
        if (!(seq6.indexOf( tvLiteral ) == 2)) error( test, n );
        n++;
        if (!(seq6.indexOf( tvResource ) == 3)) error( test, n );
        n++;
        if (!(seq6.indexOf( tvString, lang ) == 4)) error( test, n );
        n++;
        if (!(seq6.indexOf( tvString ) == 5)) error( test, n );
        n++;
        if (!(seq6.indexOf( tvDouble ) == 6)) error( test, n );
        n++;
        if (!(seq6.indexOf( tvFloat ) == 7)) error( test, n );
        n++;
        if (!(seq6.indexOf( tvChar ) == 8)) error( test, n );
        n++;
        if (!(seq6.indexOf( tvLong ) == 9)) error( test, n );
        n++;
        if (!(seq6.indexOf( tvInt ) == 10)) error( test, n );
        n++;
        if (!(seq6.indexOf( tvShort ) == 11)) error( test, n );
        n++;
        if (!(seq6.indexOf( tvByte ) == 12)) error( test, n );
        n++;
        if (!(seq6.indexOf( tvBoolean ) == 13)) error( test, n );
        n++;
        if (!(seq6.indexOf( 1234543 ) == 0)) error( test, n );
        }

    public void testSeq5()
        {
        Seq seq5 = m.createSeq();
        String test = "seq5";
        int n = 0;
        for (int i = 0; i < num; i++)
            {
            seq5.add( i );
            }

        try
            {
            n++;
            seq5.add( 0, false );
            error( test, n );
            }
        catch (SeqIndexBoundsException e)
            {
            // as required
            }
        seq5.add( num + 1, false );
        if (seq5.size() != num + 1) error( test, n );
        seq5.remove( num + 1 );
        try
            {
            n++;
            seq5.add( num + 2, false );
            error( test, n );
            }
        catch (SeqIndexBoundsException e)
            {
            // as required
            }

        n = (n / 100) * 100 + 100;
        int size = seq5.size();
        for (int i = 1; i <= num - 1; i++)
            {
            n++;
            seq5.add( i, 1000 + i );
            n++;
            if (!(seq5.getInt( i ) == 1000 + i)) error( test, n );
            n++;
            if (!(seq5.getInt( i + 1 ) == 0)) error( test, n );
            n++;
            if (!(seq5.size() == (size + i))) error( test, n );
            n++;
            if (!(seq5.getInt( size ) == (num - i - 1))) error( test, n );
            }
        }

    public void testSeq4()
        {
        String test = "temp";
        int n = 58305;
        Seq seq4 = m.createSeq();
        n = (n / 100) * 100 + 100;
        n++;
        seq4.add( tvBoolean );
        n++;
        if (!(seq4.getBoolean( 1 ) == tvBoolean)) error( test, n );
        n++;
        seq4.add( tvByte );
        n++;
        if (!(seq4.getByte( 2 ) == tvByte)) error( test, n );
        n++;
        seq4.add( tvShort );
        n++;
        if (!(seq4.getShort( 3 ) == tvShort)) error( test, n );
        n++;
        seq4.add( tvInt );
        n++;
        if (!(seq4.getInt( 4 ) == tvInt)) error( test, n );
        n++;
        seq4.add( tvLong );
        n++;
        if (!(seq4.getLong( 5 ) == tvLong)) error( test, n );
        n++;
        seq4.add( tvChar );
        n++;
        if (!(seq4.getChar( 6 ) == tvChar)) error( test, n );
        n++;
        seq4.add( tvFloat );
        n++;
        if (!(seq4.getFloat( 7 ) == tvFloat)) error( test, n );
        n++;
        seq4.add( tvDouble );
        n++;
        if (!(seq4.getDouble( 8 ) == tvDouble)) error( test, n );
        n++;
        seq4.add( tvString );
        n++;
        if (!(seq4.getString( 9 ).equals( tvString ))) error( test, n );
        n++;
        if (!(seq4.getLanguage( 9 ).equals( "" ))) error( test, n );
        n++;
        seq4.add( tvString, lang );
        n++;
        if (!(seq4.getString( 10 ).equals( tvString ))) error( test, n );
        n++;
        if (!(seq4.getLanguage( 10 ).equals( lang ))) error( test, n );
        n++;
        seq4.add( anObject );
        n++;
//        if (!(seq4.getObject( 11, new LitTestObjF() ).equals( anObject ))) error(
//                test, n );
        n++;
        seq4.add( tvResource );
        n++;
        if (!(seq4.getResource( 12 ).equals( tvResource ))) error( test, n );
        n++;
        seq4.add( tvLiteral );
        n++;
        if (!(seq4.getLiteral( 13 ).equals( tvLiteral ))) error( test, n );
        n++;
//        seq4.add( tvResObj );
//        n++;
//        if (!(seq4.getResource( 14, new ResTestObjF() ).equals( tvResObj ))) error(
//                test, n );
        n++;
        seq4.add( tvBag );
        n++;
        if (!(seq4.getBag( 14 ).equals( tvBag ))) error( test, n );
        n++;
        seq4.add( tvAlt );
        n++;
        if (!(seq4.getAlt( 15 ).equals( tvAlt ))) error( test, n );
        n++;
        seq4.add( tvSeq );
        n++;
        if (!(seq4.getSeq( 16 ).equals( tvSeq ))) error( test, n );
        n++;
        try
            {
            seq4.getInt( 17 );
            error( test, n );
            }
        catch (SeqIndexBoundsException e)
            {
            // as required
            }
        n++;
        try
            {
            seq4.getInt( 0 );
            error( test, n );
            }
        catch (SeqIndexBoundsException e)
            {
            // as required
            }
        }
    }
