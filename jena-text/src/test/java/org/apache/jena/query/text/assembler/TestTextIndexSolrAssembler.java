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

import static org.junit.Assert.assertEquals ;
import static org.junit.Assert.assertTrue ;
import org.apache.jena.query.text.TextIndexSolr ;
import org.apache.solr.client.solrj.impl.HttpSolrServer ;
import org.junit.Test ;

import com.hp.hpl.jena.assembler.Assembler ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.vocabulary.RDF ;
import com.hp.hpl.jena.vocabulary.RDFS ;

public class TestTextIndexSolrAssembler extends AbstractTestTextAssembler {
	
	private static final String TESTBASE = "http://example.org/solrAssembler/";
	private static final Resource EMBEDDED_SOLR_INDEX_SPEC;
	private static final Resource HTTP_SOLR_INDEX_SPEC;
	
//	@Test public void testIndexUsesEmbeddedServer() throws IOException {
//		if (EMBEDDED_SOLR.DATA_DIR.exists()) 
//			TextSearchUtil.emptyAndDeleteDirectory(EMBEDDED_SOLR.DATA_DIR);
//		EMBEDDED_SOLR.INDEX_DIR.mkdirs();
//		TextSearchUtil.createEmptyIndex(EMBEDDED_SOLR.INDEX_DIR);
//		TextIndexSolr indexSolr = (TextIndexSolr) Assembler.general.open(EMBEDDED_SOLR_INDEX_SPEC);
//		assertEquals("org.apache.solr.client.solrj.embedded.EmbeddedSolrServer", indexSolr.getServer().getClass().getName());
//		assertEquals(RDFS.label.asNode(), indexSolr.getDocDef().getPrimaryPredicate());	
//		indexSolr.getServer().shutdown();
//	}
	
	@Test public void testIndexUsesHttpServer() {
		TextIndexSolr indexSolr = (TextIndexSolr) Assembler.general.open(HTTP_SOLR_INDEX_SPEC);
		assertTrue(indexSolr.getServer() instanceof HttpSolrServer);
		assertEquals(indexSolr.getDocDef().getPrimaryPredicate(), RDFS.label.asNode());	
		indexSolr.getServer().shutdown();			
	}
	
	static {
		TextAssembler.init();
		EMBEDDED_SOLR_INDEX_SPEC = 
				model.createResource(TESTBASE + "embeddedSolrIndexSpec")
				     .addProperty(RDF.type, TextVocab.textIndexSolr)
				     .addProperty(TextVocab.pServer, model.createResource("embedded:solr"))
				     .addProperty(TextVocab.pEntityMap, SIMPLE_ENTITY_MAP_SPEC);			
		HTTP_SOLR_INDEX_SPEC = 
				model.createResource(TESTBASE + "httpSolrIndexSpec")
				     .addProperty(RDF.type, TextVocab.textIndexSolr)
				     .addProperty(TextVocab.pServer, model.createResource("http://example.org/solr/index"))
				     .addProperty(TextVocab.pEntityMap, SIMPLE_ENTITY_MAP_SPEC);				
	}

}
