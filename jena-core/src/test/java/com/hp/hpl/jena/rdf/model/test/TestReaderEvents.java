/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.graph.GraphEvents;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.test.helpers.RecordingModelListener;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;

import java.io.StringReader;

/**
 * TestReaderEvents - test that reader events are issued
 */
public class TestReaderEvents extends AbstractModelTestBase
{
	public TestReaderEvents( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}
	
	public TestReaderEvents()
	{
		this( new TestPackage.PlainModelFactory(), "TestReaderEvents"); 
	}

	public void testN3ReaderEvents()
	{
		testReaderEvent("N3", "");
	}

	public void testNTriplesReaderEvents()
	{
		testReaderEvent("N-TRIPLE", "");
	}

	public void testReaderEvent( final String language, final String emptyModel )
	{
		final RecordingModelListener L = new RecordingModelListener();
		model.register(L);
		final RDFReader r = model.getReader(language);
		final StringReader stringReader = new StringReader(emptyModel);
		r.read(model, stringReader, "");
		L.assertHasStart(new Object[] { "someEvent", model,
				GraphEvents.startRead });
		L.assertHasEnd(new Object[] { "someEvent", model,
				GraphEvents.finishRead });
	}

	public void testXMLReaderEvents()
	{
		final String emptyModel = "<rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'></rdf:RDF>";
		testReaderEvent("RDF/XML", emptyModel);
	}

}
