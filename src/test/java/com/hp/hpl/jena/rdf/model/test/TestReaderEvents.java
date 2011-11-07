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

import java.io.StringReader;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;

import junit.framework.TestSuite;

/**
 	TestReaderEvents - test that reader events are issued
 	@author kers
*/
public class TestReaderEvents extends ModelTestBase
	{
    public TestReaderEvents( String name )
        { super(name); }

    public static TestSuite suite()
        { return new TestSuite( TestReaderEvents.class ); }
    
    public void testXMLReaderEvents()
        {
        String emptyModel = "<rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'></rdf:RDF>";
        testReaderEvent( "RDF/XML", emptyModel );
        }

    public void testN3ReaderEvents()
        {
        testReaderEvent( "N3", "" );
        }

    public void testNTriplesReaderEvents()
        {
        testReaderEvent( "N-TRIPLE", "" );
        }
    
    public void testReaderEvent( String language, String emptyModel )
        {
        Model m = ModelFactory.createDefaultModel();
        RecordingModelListener L = new RecordingModelListener();
        m.register( L );
        RDFReader r = m.getReader( language );
        StringReader stringReader = new StringReader( emptyModel );
        r.read( m, stringReader, "" );
        L.assertHasStart( new Object[] {"someEvent", m, GraphEvents.startRead} );
        L.assertHasEnd( new Object[] {"someEvent", m, GraphEvents.finishRead} );
        }
    
    }
