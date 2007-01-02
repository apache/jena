/*
 	(c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: NewRegressionObjects.java,v 1.3 2007-01-02 11:49:22 andy_seaborne Exp $
*/

package com.hp.hpl.jena.regression;

import java.util.*;

import junit.framework.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.vocabulary.RDF;

public class NewRegressionObjects extends ModelTestBase
    {
    public NewRegressionObjects( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( NewRegressionObjects.class ); }

    protected Model getModel()
        { return ModelFactory.createDefaultModel(); }
    
    protected Model m;
    protected Resource S;
    protected Property P;
    
    public void setUp()
        { 
        m = getModel();
        S = m.createResource( "http://nowhere.man/subject" ); 
        P = m.createProperty( "http://nowhere.man/predicate" ); 
        }
    
    public void tearDown()
        { m = null; S = null; P = null; }
    
    protected static int numberSubjects = 7;
    protected static int numberPredicates = 3;

    protected static final String subjectPrefix = "http://aldabaran/test6/s";
    protected static final String predicatePrefix = "http://aldabaran/test6/";
    
    public void testListSubjects()
        {
        Set statements = fill( m );
        List L = iteratorToList( m.listSubjects() );
        assertEquals( numberSubjects, L.size() );
        Set wanted = subjectSet( numberSubjects );
        assertEquals( wanted, iteratorToSet( L.iterator() ) );
        }    
    
    public void testListNamespaces()
        {
        Set statements = fill( m );
        List L = iteratorToList( m.listNameSpaces() );
        assertEquals( numberPredicates, L.size() );
        Set wanted = predicateSet( numberPredicates );
        assertEquals( wanted, new HashSet( L ) );
        }
    
    public void testListStatements()
        {
        Set statements = fill( m );
        List L = iteratorToList( m.listStatements() );
        assertEquals( statements.size(), L.size() );
        assertEquals( statements, new HashSet( L ) );
        }
    
    public void testListObjectsOfPropertyByProperty()
        {
        Set statements = fill( m );
        List L = iteratorToList
            ( m.listObjectsOfProperty( property( predicatePrefix + "0/p" ) ) );
        assertEquals( numberSubjects, L.size() );
        Set wanted = literalsFor( 0 );
        assertEquals( wanted, new HashSet( L ) );
        }
    
    public void testListObejctsOfPropertyBySubject()
        {
        int size = 10;
        Resource s = m.createResource();
        for (int i = 0; i < size; i += 1) m.add( s, RDF.value, i );
        List L = iteratorToList( m.listObjectsOfProperty( s, RDF.value ) );
        assertEquals( size, L.size() );
        Set wanted = literalsUpto( size );
        assertEquals( wanted, new HashSet( L ) );
        }
    
    public void testListObjects()
        {
        fill( m );
        Set wanted = literalsUpto( numberSubjects * numberPredicates );
        assertEquals( wanted, iteratorToSet( m.listObjects() ) );
        }
    
    protected Set subjectSet( int limit )
        {
        Set result = new HashSet();
        for (int i = 0; i < limit; i += 1) result.add( resource( subjectPrefix + i ) );
        return result;
        }
    
    protected Set predicateSet( int limit )
        {
        Set result = new HashSet();
        for (int i = 0; i < limit; i += 1) result.add( predicatePrefix + i + "/" );
        return result;
        }
    
    protected Set literalsUpto( int limit )
        {
        Set result = new HashSet();
        for (int i = 0; i < limit; i += 1) result.add( m.createLiteral( i ) );
        return result;
        }
    
    protected Set literalsFor( int predicate )
        {
        Set result = new HashSet();
        for (int i = 0; i < numberSubjects; i += 1)
            result.add( m.createLiteral( i * numberPredicates + predicate ) );
        return result;
        }
    
    protected Set fill( Model m )
        {
        Set statements = new HashSet();
        for (int i = 0; i < numberSubjects; i += 1)
            for (int j = 0; j < numberPredicates; j += 1)
                {
                Statement s = m.createStatement
                    ( resource( subjectPrefix + i ), 
                    property( predicatePrefix + j + "/p" ), 
                    i * numberPredicates + j );
                m.add( s );
                statements.add( s );
                }
        assertEquals( numberSubjects * numberPredicates, m.size() );
        return statements;
        }
    }


/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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