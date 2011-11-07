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

import java.util.List;

import junit.framework.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
//import com.hp.hpl.jena.regression.Regression.*;

public class NewRegressionSelector extends ModelTestBase
    {
    public NewRegressionSelector( String name )
        { super( name ); }

    public static Test suite()
        { return new TestSuite( NewRegressionSelector.class ); }
    
    protected Model getModel()
        { return ModelFactory.createDefaultModel(); }

    protected Model m;
    
    @Override
    public void setUp()
        { 
        m = getModel();
        }
    
    @Override
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

//        Literal     tvLitObjs[]  = { m.createTypedLiteral(new LitTestObjF()),
//                                    m.createTypedLiteral(new LitTestObjF()) };
//        Resource    tvResObjs[]  = { m.createResource(new ResTestObjF()),
//                                    m.createResource(new ResTestObjF()) };

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
//                m.add( subject[i], predicate[j], tvLitObjs[j] );
//                m.add( subject[i], predicate[j], tvResObjs[j] );
                }
            }
        
        StmtIterator it1 = m.listStatements( new SimpleSelector( null, null, (RDFNode) null) );
        List<Statement> L1 = iteratorToList( it1 );
        assertEquals( num * num * 6, L1.size() );

        StmtIterator it2 = m.listStatements( new SimpleSelector( subject[0], null, (RDFNode) null) );
        List<Statement> L2 = iteratorToList( it2 );
        for (int i = 0; i < L2.size(); i += 1) 
            assertEquals( subject[0], L2.get(i).getSubject() );
        assertEquals( num * 6, L2.size() );
        
        StmtIterator it3 = m.listStatements( new SimpleSelector( null, predicate[1], (RDFNode) null) );
        List<Statement> L3 = iteratorToList( it3 );
        for (int i = 0; i < L3.size(); i += 1) 
            assertEquals( predicate[1], L3.get(i).getPredicate() );
        assertEquals( num * 6, L3.size() );
        
//        StmtIterator it4 = m.listStatements( new SimpleSelector( null, null, tvResObjs[1] ) );
//        List<Statement> L4 = iteratorToList( it4 );
//        for (int i = 0; i < L4.size(); i += 1) 
//            assertEquals( tvResObjs[1], L4.get(i).getObject() );
//        assertEquals( 2, L4.size() );
        
        StmtIterator it5 = m.listStatements( new SimpleSelector( null, null, m.createTypedLiteral( false ) ) );
        List<Statement> L5 = iteratorToList( it5 );
        for (int i = 0; i < L5.size(); i += 1) 
            assertEquals( false, L5.get(i).getBoolean() );
        assertEquals( 2, L5.size() );

        StmtIterator it6 = m.listStatements( new SimpleSelector( null, null, tvStrings[1], langs[1] ) );
        List<Statement> L6 = iteratorToList( it6 );
        for (int i = 0; i < L6.size(); i += 1) 
            assertEquals( langs[1], L6.get(i).getLanguage() );
        assertEquals( 2, L6.size() );
        }
    }
