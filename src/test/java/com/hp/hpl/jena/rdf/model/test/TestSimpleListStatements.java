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

package com.hp.hpl.jena.rdf.model.test;

/**
	@author bwm out of kers
*/

import java.util.List;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;


import junit.framework.*;

public class TestSimpleListStatements extends ModelTestBase
    {    
        
    public TestSimpleListStatements( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestSimpleListStatements.class ); }   
    
    Model model = null; 
    
    static boolean booleanValue = true;
    static char    charValue   = 'c';
    static long    longValue   = 456;
    static float   floatValue  = 5.67F;
    static double  doubleValue = 6.78;
    static String   stringValue ="stringValue";
    static String   langValue   = "en";   
        
    @Override
    protected void setUp() throws java.lang.Exception {
        
    	model = ModelFactory.createDefaultModel();
    	model.createResource("http://example.org/boolean")
    	     .addLiteral(RDF.value, booleanValue);             
        model.createResource("http://example.org/char")
             .addLiteral(RDF.value, charValue);             
        model.createResource("http://example.org/long")             
             .addLiteral(RDF.value, longValue);              
        model.createResource("http://example.org/float")
             .addLiteral(RDF.value, floatValue);            
        model.createResource("http://example.org/double")
             .addLiteral(RDF.value, doubleValue);             
        model.createResource("http://example.org/string")
             .addProperty(RDF.value, stringValue);             
        model.createResource("http://example.org/langString")
             .addProperty(RDF.value, stringValue, langValue);
    	
    }
    
    @Override
    protected void tearDown() throws java.lang.Exception {
    	model.close();
        model = null;
    }
    
    public void testBoolean() 
        {
        List<Statement> got = model.listLiteralStatements( null, null, booleanValue ).toList();
        assertEquals( 1, got.size() );
        Statement it = got.get( 0 );
        assertEquals( resource( "http://example.org/boolean" ), it.getSubject() );
        assertEquals( model.createTypedLiteral( booleanValue ), it.getObject() );
        }
    
    public void testChar() 
        {
        List<Statement> got = model.listLiteralStatements( null, null, charValue ).toList();
        assertEquals( 1, got.size() );
        Statement it = got.get( 0 );
        assertEquals( resource( "http://example.org/char" ), it.getSubject() );
        assertEquals( model.createTypedLiteral( charValue ), it.getObject() );
        }
    
    public void testLong() 
        {
        List<Statement> got = model.listLiteralStatements( null, null, longValue ).toList();
        assertEquals( 1, got.size() );
        Statement it = got.get( 0 );
        assertEquals( resource( "http://example.org/long" ), it.getSubject() );
        assertEquals( model.createTypedLiteral( longValue ), it.getObject() );
        }
    
    public void testFloat() 
        {
        List<Statement> got = model.listLiteralStatements( null, null, floatValue ).toList();
        assertEquals( 1, got.size() );
        Statement it = got.get( 0 );
        assertEquals( resource( "http://example.org/float" ), it.getSubject() );
        assertEquals( model.createTypedLiteral( floatValue ), it.getObject() );
        }
    
    public void testDouble() 
        {
        List<Statement> got = model.listLiteralStatements( null, null, doubleValue ).toList();
        assertEquals( 1, got.size() );
        Statement it = got.get( 0 );
        assertEquals( resource( "http://example.org/double" ), it.getSubject() );
        assertEquals( model.createTypedLiteral( doubleValue ), it.getObject() );
        }
    
    public void testString() {
        StmtIterator iter = model.listStatements(null, null, stringValue);
        int i =0;
        while (iter.hasNext()) {
            i++;
            assertEquals(iter.nextStatement().getSubject().getURI(),
                         "http://example.org/string");
        }
        assertEquals(1, i);
    }
    
    public void testLangString() {
        StmtIterator iter = model.listStatements(null, null,
                                                           stringValue, langValue);
        int i =0;
        while (iter.hasNext()) {
            i++;
            assertEquals(iter.nextStatement().getSubject().getURI(),
                         "http://example.org/langString");
        }
        assertEquals(1, i);
    }
        
    
    public void testAll() {
        StmtIterator iter = model.listStatements(null, null, (RDFNode) null);
        int i =0;
        while (iter.hasNext()) {
            i++;
            iter.next();
        }
        assertEquals(7, i);
    }
    
    public void testAllString() {
        StmtIterator iter = model.listStatements(null, null, (String) null);
        int i =0;
        while (iter.hasNext()) {
            i++;
            iter.next();
        }
        assertEquals(7, i);
    }

    public Model modelWithStatements( StmtIterator it )
        {
        Model m = ModelFactory.createDefaultModel();
        while (it.hasNext()) m.add( it.nextStatement() );
        return m;
        }
        
    public void checkReturns( String things, StmtIterator it )
        {
        Model wanted = modelWithStatements( things );
        Model got = modelWithStatements( it );
        if (wanted.isIsomorphicWith( got ) == false)
            fail( "wanted " + wanted + " got " + got );
        }
        
    public void testListStatementsSPO()
        {
        Model m = ModelFactory.createDefaultModel();
        Resource A = resource( m, "A" ), X = resource( m, "X" );
        Property P = property( m, "P" ), P1 = property( m, "P1" );
        RDFNode O = resource( m, "O" ), Y = resource( m, "Y" );
        String S1 = "S P O; S1 P O; S2 P O";
        String S2 = "A P1 B; A P1 B; A P1 C";
        String S3 = "X P1 Y; X P2 Y; X P3 Y";
        modelAdd( m, S1 );
        modelAdd( m,  S2 );
        modelAdd( m, S3 );
        checkReturns( S1, m.listStatements( null, P, O ) );
        checkReturns( S2, m.listStatements( A, P1, (RDFNode) null ) );
        checkReturns( S3, m.listStatements( X, null, Y ) );
        m.close();
        }
        
    public void testListStatementsClever()
        {
        Model m = ModelFactory.createDefaultModel();
        modelAdd( m, "S P O; S P O2; S P2 O; S2 P O" );
        Selector sel = new SimpleSelector( null, null, (RDFNode) null )
            {
            @Override
            public boolean test( Statement st )
                { return 
                        st.getSubject().toString().length() 
                        + st.getPredicate().toString().length()
                        + st.getObject().toString().length()
                        == 15; /* eh:/S + eh:/P + eh:/O */
                }
                
            @Override
            public boolean isSimple()
                { return false; }
            };
        checkReturns( "S P O", m.listStatements( sel ) );
        }
}
