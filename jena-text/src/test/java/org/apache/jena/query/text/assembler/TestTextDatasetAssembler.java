/**
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

package org.apache.jena.query.text.assembler;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.jena.query.text.TextIndexLucene;
import org.apache.jena.query.text.TextQuery;
import org.junit.Test;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.tdb.assembler.AssemblerTDB;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Test the text dataset assembler.
 */
public class TestTextDatasetAssembler extends AbstractTestTextAssembler {
	
	private static final String TESTBASE = "http://example.org/testDatasetAssembler/";
	
	private static final Resource spec1;
	private static final Resource noDatasetPropertySpec;
	private static final Resource noIndexPropertySpec;
	
	@Test public void testSimpleDatasetAssembler() {
		Dataset dataset = (Dataset) Assembler.general.open(spec1);
		assertTrue(dataset.getContext().get(TextQuery.textIndex) instanceof TextIndexLucene);
	}
	
	@Test public void testErrorOnNoDataset() {
		try {
		    Assembler.general.open(noDatasetPropertySpec);
		    fail("should have thrown an exception");
		} catch (Exception e) {}
	}
	
	@Test public void testErrorOnNoIndex() {
		try {
		    Assembler.general.open(noIndexPropertySpec);
		    fail("should have thrown an exception");
		} catch (Exception e) {}
	}
	
	static {
		TextAssembler.init();
		AssemblerTDB.init();
		spec1 =
				model.createResource(TESTBASE + "spec1")
				     .addProperty(RDF.type, TextVocab.textDataset)
				     .addProperty(TextVocab.pDataset, SIMPLE_DATASET_SPEC)
				     .addProperty(TextVocab.pIndex, SIMPLE_INDEX_SPEC);
		noDatasetPropertySpec =
				model.createResource(TESTBASE + "noDatasetPropertySpec")
				     .addProperty(RDF.type, TextVocab.textDataset)
				     .addProperty(TextVocab.pIndex, SIMPLE_INDEX_SPEC);
		noIndexPropertySpec =
				model.createResource(TESTBASE + "noIndexPropertySpec")
				     .addProperty(RDF.type, TextVocab.textDataset)
				     .addProperty(TextVocab.pDataset, SIMPLE_DATASET_SPEC);
		
	}

}
