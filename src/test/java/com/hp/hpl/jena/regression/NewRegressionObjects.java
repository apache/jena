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
    
    @Override
    public void setUp()
        { 
        m = getModel();
        S = m.createResource( "http://nowhere.man/subject" ); 
        P = m.createProperty( "http://nowhere.man/predicate" ); 
        }
    
    @Override
    public void tearDown()
        { m = null; S = null; P = null; }
    
    protected static int numberSubjects = 7;
    protected static int numberPredicates = 3;

    protected static final String subjectPrefix = "http://aldabaran/test6/s";
    protected static final String predicatePrefix = "http://aldabaran/test6/";
    
    public void testListSubjects()
        {
        Set<Statement> statements = fill( m );
        List<Resource> L = iteratorToList( m.listSubjects() );
        assertEquals( numberSubjects, L.size() );
        Set<Resource> wanted = subjectSet( numberSubjects );
        assertEquals( wanted, iteratorToSet( L.iterator() ) );
        }    
    
    public void testListNamespaces()
        {
        Set<Statement> statements = fill( m );
        List<String> L = iteratorToList( m.listNameSpaces() );
        assertEquals( numberPredicates, L.size() );
        Set<String> wanted = predicateSet( numberPredicates );
        assertEquals( wanted, new HashSet<String>( L ) );
        }
    
    public void testListStatements()
        {
        Set<Statement> statements = fill( m );
        List<Statement> L = iteratorToList( m.listStatements() );
        assertEquals( statements.size(), L.size() );
        assertEquals( statements, new HashSet<Statement>( L ) );
        }
    
    public void testListObjectsOfPropertyByProperty()
        {
        Set<Statement> statements = fill( m );
        List<RDFNode> L = iteratorToList
            ( m.listObjectsOfProperty( property( predicatePrefix + "0/p" ) ) );
        assertEquals( numberSubjects, L.size() );
        Set<Literal> wanted = literalsFor( 0 );
        assertEquals( wanted, new HashSet<RDFNode>( L ) );
        }
    
    public void testListObjectsOfPropertyBySubject()
        {
        int size = 10;
        Resource s = m.createResource();
        for (int i = 0; i < size; i += 1) m.addLiteral( s, RDF.value, i );
        List<RDFNode> L = iteratorToList( m.listObjectsOfProperty( s, RDF.value ) );
        assertEquals( size, L.size() );
        Set<Literal> wanted = literalsUpto( size );
        assertEquals( wanted, new HashSet<RDFNode>( L ) );
        }
    
    public void testListObjects()
        {
        fill( m );
        Set<Literal> wanted = literalsUpto( numberSubjects * numberPredicates );
        assertEquals( wanted, iteratorToSet( m.listObjects() ) );
        }
    
    protected Set<Resource> subjectSet( int limit )
        {
        Set<Resource> result = new HashSet<Resource>();
        for (int i = 0; i < limit; i += 1) result.add( resource( subjectPrefix + i ) );
        return result;
        }
    
    protected Set<String> predicateSet( int limit )
        {
        Set<String> result = new HashSet<String>();
        for (int i = 0; i < limit; i += 1) result.add( predicatePrefix + i + "/" );
        return result;
        }
    
    protected Set<Literal> literalsUpto( int limit )
        {
        Set<Literal> result = new HashSet<Literal>();
        for (int i = 0; i < limit; i += 1) result.add( m.createTypedLiteral( i ) );
        return result;
        }
    
    protected Set<Literal> literalsFor( int predicate )
        {
        Set<Literal> result = new HashSet<Literal>();
        for (int i = 0; i < numberSubjects; i += 1)
            result.add( m.createTypedLiteral( i * numberPredicates + predicate ) );
        return result;
        }
    
    protected Set<Statement> fill( Model m )
        {
        Set<Statement> statements = new HashSet<Statement>();
        for (int i = 0; i < numberSubjects; i += 1)
            for (int j = 0; j < numberPredicates; j += 1)
                {
                Statement s = m.createLiteralStatement
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
