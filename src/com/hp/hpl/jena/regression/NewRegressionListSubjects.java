/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: NewRegressionListSubjects.java,v 1.1 2005-08-18 09:45:49 chris-dollin Exp $
*/

package com.hp.hpl.jena.regression;

import java.util.*;

import junit.framework.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.regression.Regression.*;
import com.hp.hpl.jena.shared.PropertyNotFoundException;
import com.hp.hpl.jena.vocabulary.RDF;

public class NewRegressionListSubjects extends ModelTestBase
    {
    public NewRegressionListSubjects( String name )
        { super( name ); }

    public static Test suite()
        { return new TestSuite( NewRegressionListSubjects.class ); }
    
    protected Model getModel()
        { return ModelFactory.createDefaultModel(); }
    
    protected Model m;
    
    public void setUp()
        { 
        m = getModel();
        fillModel();
        }
    
    public void tearDown()
        { m = null; }
    
    static final String subjectPrefix = "http://aldabaran/test8/s";
    
    static final String predicatePrefix = "http://aldabaran/test8/";
    
    Resource [] subject;
    Property [] predicate;  
    RDFNode []  object;
    Literal []  tvLitObj;
    Resource [] tvResObj;
    
    boolean [] tvBoolean = { false, true };
    long []    tvLong    = { 123, 321 };
    char []    tvChar    = { '@', ';' };
    float []   tvFloat   = { 456.789f, 789.456f };
    double []  tvDouble  = { 123.456, 456.123 };
    String []  tvString  = { "test8 testing string 1", "test8 testing string 2" };
    String []  lang      = { "en", "fr" };
    
    protected Set subjectsTo( String prefix, int limit )
        {
        Set result = new HashSet();
        for (int i = 0; i < limit; i += 1) result.add( resource( prefix + i ) );
        return result;
        }
    
    public void test8()  
        {
        assertEquiv( subjectsTo( subjectPrefix, 5 ), m.listSubjectsWithProperty( predicate[4] ) );

        assertEquiv( subjectsTo( subjectPrefix, 2 ), m.listSubjectsWithProperty( predicate[0] ) );
        
        assertEquiv( subjectsTo( subjectPrefix, 2 ), m.listSubjectsWithProperty( predicate[0], tvBoolean[0] ) );

        assertEquiv( subjectsTo( subjectPrefix, 0 ), m.listSubjectsWithProperty( predicate[0], tvBoolean[1] ) );

        assertEquiv( subjectsTo( subjectPrefix, 2 ), m.listSubjectsWithProperty( predicate[0], (byte) tvLong[0] ) );

        assertEquiv( subjectsTo( subjectPrefix, 0 ), m.listSubjectsWithProperty( predicate[0], (byte) tvLong[1] ) );

        assertEquiv( subjectsTo( subjectPrefix, 2 ), m.listSubjectsWithProperty( predicate[0], (short) tvLong[0] ) );

        assertEquiv( subjectsTo( subjectPrefix, 0 ), m.listSubjectsWithProperty( predicate[0], (short) tvLong[1] ) );

        assertEquiv( subjectsTo( subjectPrefix, 2 ), m.listSubjectsWithProperty( predicate[0], (int) tvLong[0] ) );

        assertEquiv( subjectsTo( subjectPrefix, 0 ), m.listSubjectsWithProperty( predicate[0], (int) tvLong[1] ) );

        assertEquiv( subjectsTo( subjectPrefix, 2 ), m.listSubjectsWithProperty( predicate[0], (long) tvLong[0] ) );

        assertEquiv( subjectsTo( subjectPrefix, 0 ), m.listSubjectsWithProperty( predicate[0], (long) tvLong[1] ) );

        assertEquiv( subjectsTo( subjectPrefix, 2 ), m.listSubjectsWithProperty( predicate[0], tvChar[0] ) );

        assertEquiv( subjectsTo( subjectPrefix, 0 ), m.listSubjectsWithProperty( predicate[0], tvChar[1] ) );

        assertEquiv( subjectsTo( subjectPrefix, 2 ), m.listSubjectsWithProperty( predicate[0], tvFloat[0] ) );

        assertEquiv( subjectsTo( subjectPrefix, 0 ), m.listSubjectsWithProperty( predicate[0], tvFloat[1] ) );

        assertEquiv( subjectsTo( subjectPrefix, 2 ), m.listSubjectsWithProperty( predicate[0], tvDouble[0] ) );

        assertEquiv( subjectsTo( subjectPrefix, 0 ), m.listSubjectsWithProperty( predicate[0], tvDouble[1] ) );

        assertEquiv( subjectsTo( subjectPrefix, 2 ), m.listSubjectsWithProperty( predicate[0], tvString[0] ) );

        assertEquiv( subjectsTo( subjectPrefix, 0 ), m.listSubjectsWithProperty( predicate[0], tvString[1] ) );

        assertEquiv( subjectsTo( subjectPrefix, 2 ), m.listSubjectsWithProperty( predicate[0], tvString[0], lang[0] ) );

        assertEquiv( subjectsTo( subjectPrefix, 0 ), m.listSubjectsWithProperty( predicate[0], tvString[1], lang[0] ) );

        assertEquiv( subjectsTo( subjectPrefix, 0 ), m.listSubjectsWithProperty( predicate[0], tvString[0], lang[1] ) );

        assertEquiv( subjectsTo( subjectPrefix, 0 ), m.listSubjectsWithProperty( predicate[0], tvString[1], lang[1] ) );

        assertEquiv( subjectsTo( subjectPrefix, 2 ), m.listSubjectsWithProperty( predicate[0], tvLitObj[0] ) );

        assertEquiv( subjectsTo( subjectPrefix, 0 ), m.listSubjectsWithProperty( predicate[0], tvLitObj[1] ) );

        assertEquiv( subjectsTo( subjectPrefix, 2 ), m.listSubjectsWithProperty( predicate[0], tvResObj[0] ) );

        assertEquiv( subjectsTo( subjectPrefix, 0 ), m.listSubjectsWithProperty( predicate[0], tvResObj[1] ) );

        assertEquiv( new HashSet( Arrays.asList( object ) ), m.listObjectsOfProperty( predicate[1] ) );
        }

    protected void assertEquiv( Set set, Iterator iterator )
        {
        List L = iteratorToList( iterator );
        assertEquals( set.size(), L.size() );
        assertEquals( set, new HashSet( L ) );
        }

    public void testGetRequiredProperty()
        {
        Statement s = m.getRequiredProperty( subject[1], predicate[1] );
        try { m.getRequiredProperty( subject[1], RDF.value ); 
            fail( "should not find absent property" ); } 
        catch (PropertyNotFoundException e) 
            { pass(); }
        }

    protected void fillModel(  )
        {
        final int num = 5;
        tvLitObj = new Literal[] 
            { m.createLiteral( new LitTestObjF() ),
            m.createLiteral( new LitTestObjF() ) };
        
        tvResObj  = new Resource[] 
            { m.createResource( new ResTestObjF() ),
            m.createResource( new ResTestObjF() ) };
        
        object = new RDFNode[]
            {
            m.createLiteral( tvBoolean[1] ),
            m.createLiteral( tvLong[1] ),
            m.createLiteral( tvChar[1] ),
            m.createLiteral( tvFloat[1] ),
            m.createLiteral( tvDouble[1] ),
            m.createLiteral( tvString[1] ),
            m.createLiteral( tvString[1], lang[1] ),
            tvLitObj[1],
            tvResObj[1]                  
            };

        subject = new Resource[num];
        predicate = new Property[num];
        
        for (int i = 0; i<num; i++) 
            {
            subject[i] = m.createResource( subjectPrefix + i );
            predicate[i] = m.createProperty( predicatePrefix + i, "p");
            }
        
        for (int i = 0; i < num; i += 1) 
            m.add(subject[i], predicate[4], false );
        
        for (int i = 0; i < 2 ; i += 1) 
            {
            for (int j = 0; j < 2; j += 1) 
                {
                m.add(subject[i], predicate[j], tvBoolean[j] );
                m.add(subject[i], predicate[j], tvLong[j] );
                m.add(subject[i], predicate[j], tvChar[j] );
                m.add(subject[i], predicate[j], tvFloat[j] );
                m.add(subject[i], predicate[j], tvDouble[j] );
                m.add(subject[i], predicate[j], tvString[j] );
                m.add(subject[i], predicate[j], tvString[j], lang[j] );
                m.add(subject[i], predicate[j], tvLitObj[j] );
                m.add(subject[i], predicate[j], tvResObj[j] );
                }
            }
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