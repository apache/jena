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

package jena;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.text.AbstractTestDatasetWithTextIndex;
import org.apache.jena.query.text.TextSearchUtil;
import org.apache.jena.query.text.assembler.TextAssembler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import arq.cmd.CmdException;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileUtils;

// THESE DO NOT WORK.
// And aren't part of the test suite.

public class TestTextIndexer {
	private static final String RESOURCE_BASE = "http://example.org/data/resource/";
	private static final String INDEX_PATH = "target/test/simpleLuceneIndex";
	private static final File indexDir = new File(INDEX_PATH);
	private static final String TDB_PATH = "target/test/tdb";
	private static final File tdbDir = new File(TDB_PATH);
	private static final String SPEC_BASE = "http://example.org/specbase/";
	private static final String SPEC_ROOT_LOCAL = "spec";
	private static final String SPEC_ROOT_URI = SPEC_BASE + SPEC_ROOT_LOCAL;
	protected static final String QUERY_PROLOG = 
			StrUtils.strjoinNL(
				"PREFIX text: <http://jena.apache.org/text#>",
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				);
	
	protected static final String TURTLE_PROLOG = 
				StrUtils.strjoinNL(
						"@prefix text: <http://jena.apache.org/text#> .",
						"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ."
						);

	private static final String SPEC_PATH = "test/asbl-tdbWithTextIndex.ttl";
	
	@Before public void before() {
		after();
		indexDir.mkdirs();
	}
	
	@After public void after() {
		TextSearchUtil.emptyAndDeleteDirectory(indexDir);
		TextSearchUtil.emptyAndDeleteDirectory(tdbDir);
	}
	
	@Test(expected=CmdException.class) public void testDetectsNoDataset() {
	    textindexer.testMain( new String[] {} );
	}
	
	@Test public void testDetectsNotTextIndexedDataset() {
		try {
			textindexer.testMain( new String[] 
		        {
				    "--desc",
				 	"test/asbl-memNoTextIndex.ttl"
			    } );
			fail("should have thrown an exception with no dataset");
		} catch (CmdException e) {
			assertTrue("wrong exception: " + e.getMessage(), e.getMessage().contains("no text index"));
		}		
	}
	
	@Test public void testIndexEmptyDataset() {
		textindexer.testMain( new String[] 
		    {
			    "--desc",
			 	"test/asbl-tdbWithTextIndex.ttl"
			} );
	}
	
	@Test public void testIndexAndQueryDataset() throws FileNotFoundException {
		String label = "testIndexAndQueryDataset";
		final String turtle = StrUtils.strjoinNL(
				TURTLE_PROLOG,
				"<" + RESOURCE_BASE + label + "1>",
				"  rdfs:label \"" + label + " label innnnnnnnnn\" ;", // in is not indexed
				"  rdfs:comment \"" + label + " comment out\" ;",
				".",
				"<" + RESOURCE_BASE + label + "2>",
				"  rdfs:label \"" + label + " label out\" ;",
				"  rdfs:comment \"" + label + " comment out\" ;",
				".",
				"<" + RESOURCE_BASE + label + "3>",
				"  rdfs:label \"" + label + " label out\" ;",
				"  rdfs:comment \"" + label + " comment innnnnnnnnn\" ;",
				".",
				"<" + RESOURCE_BASE + label + "4>",
				"  rdfs:label \"" + label + " label out\" ;",
				"  rdfs:comment \"" + label + " comment out\" ;",
				"."
				);
		String queryString = StrUtils.strjoinNL(
				QUERY_PROLOG,
				"SELECT ?s",
				"WHERE {",
				"    {",
				"        ?s text:query ( rdfs:label \"innnnnnnnnn\") .",
				"    } UNION {",
				"        ?s text:query ( rdfs:comment \"innnnnnnnnn\" ) .",
				"    }",
				"}"
				);
		Set<String> expectedURIs = (new HashSet<String>());
		expectedURIs.addAll( Arrays.asList((
				new String[]
				{
					    RESOURCE_BASE + label + "1",
					    RESOURCE_BASE + label + "3",
				}
		)));
		
		Dataset dataset = TDBFactory.createDataset(TDB_PATH) ;
		Model model = dataset.getDefaultModel();
		Reader reader = new StringReader(turtle);
		model.read(reader, "", "TURTLE");
		model.close() ;
		dataset.close();
		
		textindexer.testMain( new String[] 
		    {
			    "--desc",
			 	"test/asbl-tdbWithTextIndex.ttl"
			} );
		
		AbstractTestDatasetWithTextIndex.doTestQuery(getDataset(), label, queryString, expectedURIs, expectedURIs.size());
	}
	
	private Dataset getDataset() throws FileNotFoundException {
		Reader reader = new FileReader(SPEC_PATH);
		Model specModel = ModelFactory.createDefaultModel();
		specModel.read(reader, FileUtils.toURL(SPEC_PATH), "TURTLE");
		TextAssembler.init();
		Resource root = specModel.getResource(SPEC_ROOT_URI);
		return (Dataset) Assembler.general.open(root);
	}
}
