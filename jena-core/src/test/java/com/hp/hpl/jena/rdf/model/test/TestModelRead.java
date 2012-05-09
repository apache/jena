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

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.n3.IRIResolver;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.ConfigException;
import com.hp.hpl.jena.shared.JenaException;

import junit.framework.TestSuite;

/**
     TestModelRead - test that the new model.read operation(s) exist.
     @author kers
 */
public class TestModelRead extends ModelTestBase
    {
    protected static Logger logger = LoggerFactory.getLogger( TestModelRead.class );

    public TestModelRead( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestModelRead.class ); }
    
    public void testReturnsSelf()
        {
        Model m = ModelFactory.createDefaultModel();
        assertSame( m, m.read( "file:testing/modelReading/empty.n3", "base", "N3" ) );
        assertTrue( m.isEmpty() );
        }
    
    public void testGRDDLConfigMessage() {
    	Model m = ModelFactory.createDefaultModel();
    	try {
    		m.read("http://www.w3.org/","GRDDL");
    		// ok.
    	}
    	catch (ConfigException e) {
    		// expected.
    	}
    }
    public void testLoadsSimpleModel()
        {
        Model expected = ModelFactory.createDefaultModel();
        Model m = ModelFactory.createDefaultModel();
        expected.read( "file:testing/modelReading/simple.n3", "N3" );
        assertSame( m, m.read( "file:testing/modelReading/simple.n3", "base", "N3" ) );
        assertIsoModels( expected, m );
        }    
    
    /*
         Suppressed, since the other Model::read(String url) operations apparently
         don't retry failing URLs as filenames. But the code text remains, so that
         when-and-if, we have a basis.
     */
//    public void testLoadsSimpleModelWithoutProtocol()
//        {
//        Model expected = ModelFactory.createDefaultModel();
//        Model m = ModelFactory.createDefaultModel();
//        expected.read( "testing/modelReading/simple.n3", "RDF/XML" );
//        assertSame( m, m.read( "testing/modelReading/simple.n3", "base", "N3" ) );
//        assertIsoModels( expected, m );
//        }    
    
    public void testSimpleLoadImplictBase()
        {
        Model mBasedImplicit = ModelFactory.createDefaultModel();
        String fn = IRIResolver.resolveFileURL("file:testing/modelReading/based.n3" );
        Model wanted = 
            ModelFactory.createDefaultModel()
            .add( resource( fn ), property( "ja:predicate" ), resource( "ja:object" ) );
        mBasedImplicit.read( fn, "N3" );
        assertIsoModels( wanted, mBasedImplicit );
        }
    
    public void testSimpleLoadExplicitBase()
        {
        Model mBasedExplicit = ModelFactory.createDefaultModel();
        mBasedExplicit.read( "file:testing/modelReading/based.n3", "http://example/", "N3" );
        assertIsoModels( modelWithStatements( "http://example/ ja:predicate ja:object" ), mBasedExplicit );
        }
    
    public void testDefaultLangXML()
        {
        Model m = ModelFactory.createDefaultModel();
        m.read( "file:testing/modelReading/plain.rdf", null, null );
        }
    
    public void testContentNegotiation() {
		Model m = ModelFactory.createDefaultModel();
//		Model m2 = ModelFactory.createDefaultModel();

		try {
			m.read("http://jena.sourceforge.net/test/mime/test1");
		    assertEquals(m.size(),1);
//		    m2.read("http://xmlns.com/foaf/0.1/");
		} catch (JenaException jx) {
			if (jx.getCause() instanceof NoRouteToHostException
					|| jx.getCause() instanceof UnknownHostException
					|| jx.getCause() instanceof ConnectException
					|| jx.getCause() instanceof IOException) {
				logger
						.warn("Cannot access public internet - content negotiation test not executed");
			} else
				throw jx;
		}
	}
    
    }
