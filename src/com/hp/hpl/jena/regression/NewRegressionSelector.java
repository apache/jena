/*
 	(c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: NewRegressionSelector.java,v 1.6 2007-11-15 15:43:06 chris-dollin Exp $
*/

package com.hp.hpl.jena.regression;

import java.util.List;

import junit.framework.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.regression.Regression.*;

public class NewRegressionSelector extends ModelTestBase
    {
    public NewRegressionSelector( String name )
        { super( name ); }

    public static Test suite()
        { return new TestSuite( NewRegressionSelector.class ); }
    
    protected Model getModel()
        { return ModelFactory.createDefaultModel(); }

    protected Model m;
    
    public void setUp()
        { 
        m = getModel();
        }
    
    public void tearDown()
        { m = null; }
    
    public void test9() 
        {
        Model m = getModel();
        final int num = 2;

        Resource  subject[] = new Resource[num];
        Property  predicate[] = new Property[num];

        String suri = "http://aldabaran/test9/s";
        String puri = "http://aldabaran/test9/";

        boolean    tvBooleans[] = { false, true };
        long       tvLongs[]    = { 123, 321 };
        char       tvChars[]    = { '@', ';' };
        double     tvDoubles[]  = { 123.456, 456.123 };
        String     tvStrings[]  = { "test8 testing string 1",
                                   "test8 testing string 2" };
        String     langs[]      = { "en", "fr" };

        Literal     tvLitObjs[]  = { m.createTypedLiteral(new LitTestObjF()),
                                    m.createTypedLiteral(new LitTestObjF()) };
        Resource    tvResObjs[]  = { m.createResource(new ResTestObjF()),
                                    m.createResource(new ResTestObjF()) };

        for (int i = 0; i < num; i += 1) 
            {
            subject[i] = m.createResource( suri + i );
            predicate[i] = m.createProperty( puri + i, "p" );
            }

        for (int i = 0; i < num; i++) 
            {
            for (int j = 0; j < num; j++) 
                {
                m.addLiteral( subject[i], predicate[j], tvBooleans[j] );
                m.addLiteral( subject[i], predicate[j], tvLongs[j] );
                m.addLiteral( subject[i], predicate[j], tvChars[j] );
                m.addLiteral( subject[i], predicate[j], tvDoubles[j] );
                m.add( subject[i], predicate[j], tvStrings[j] );
                m.add( subject[i], predicate[j], tvStrings[j], langs[j] );
                m.add( subject[i], predicate[j], tvLitObjs[j] );
                m.add( subject[i], predicate[j], tvResObjs[j] );
                }
            }
        
        StmtIterator it1 = m.listStatements( new SimpleSelector( null, null, (RDFNode) null) );
        List L1 = iteratorToList( it1 );
        assertEquals( num * num * 8, L1.size() );

        StmtIterator it2 = m.listStatements( new SimpleSelector( subject[0], null, (RDFNode) null) );
        List L2 = iteratorToList( it2 );
        for (int i = 0; i < L2.size(); i += 1) 
            assertEquals( subject[0], ((Statement) L2.get(i)).getSubject() );
        assertEquals( num * 8, L2.size() );
        
        StmtIterator it3 = m.listStatements( new SimpleSelector( null, predicate[1], (RDFNode) null) );
        List L3 = iteratorToList( it3 );
        for (int i = 0; i < L3.size(); i += 1) 
            assertEquals( predicate[1], ((Statement) L3.get(i)).getPredicate() );
        assertEquals( num * 8, L3.size() );
        
        StmtIterator it4 = m.listStatements( new SimpleSelector( null, null, tvResObjs[1] ) );
        List L4 = iteratorToList( it4 );
        for (int i = 0; i < L4.size(); i += 1) 
            assertEquals( tvResObjs[1], ((Statement) L4.get(i)).getObject() );
        assertEquals( 2, L4.size() );
        
        StmtIterator it5 = m.listStatements( new SimpleSelector( null, null, m.createTypedLiteral( false ) ) );
        List L5 = iteratorToList( it5 );
        for (int i = 0; i < L5.size(); i += 1) 
            assertEquals( false, ((Statement) L5.get(i)).getBoolean() );
        assertEquals( 2, L5.size() );

        StmtIterator it6 = m.listStatements( new SimpleSelector( null, null, tvStrings[1], langs[1] ) );
        List L6 = iteratorToList( it6 );
        for (int i = 0; i < L6.size(); i += 1) 
            assertEquals( langs[1], ((Statement) L6.get(i)).getLanguage() );
        assertEquals( 2, L6.size() );
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