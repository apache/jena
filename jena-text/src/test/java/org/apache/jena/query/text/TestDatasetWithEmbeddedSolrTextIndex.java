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

package org.apache.jena.query.text;

import java.io.IOException ;
import java.io.Reader ;
import java.io.StringReader ;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.query.text.assembler.TextAssembler ;
import org.apache.solr.client.solrj.SolrServer ;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer ;
import org.apache.solr.core.CoreContainer ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.assembler.Assembler ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.Resource ;

public class TestDatasetWithEmbeddedSolrTextIndex extends AbstractTestDatasetWithTextIndex {
	
	private static final String SPEC_BASE = "http://example.org/spec#";
	private static final String SPEC_ROOT_LOCAL = "solr_text_dataset";
	private static final String SPEC_ROOT_URI = SPEC_BASE + SPEC_ROOT_LOCAL;
	private static final String SPEC;
	static {
	    SPEC = StrUtils.strjoinNL(
					"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ",
					"prefix ja:   <http://jena.hpl.hp.com/2005/11/Assembler#> ",
					"prefix tdb:  <http://jena.hpl.hp.com/2008/tdb#>",
					"prefix text: <http://jena.apache.org/text#>",
					"prefix :     <" + SPEC_BASE + ">",
					"",
					"[] ja:loadClass    \"org.apache.jena.query.text.TextQuery\" .",
				    "text:TextDataset      rdfs:subClassOf   ja:RDFDataset .",
				    "text:TextIndexLucene  rdfs:subClassOf   text:TextIndex .",
				    
				    ":" + SPEC_ROOT_LOCAL,
				    "    a              text:TextDataset ;",
				    "    text:dataset   :dataset ;",
				    "    text:index     :indexEmbeddedSolr ;",
				    "    .",
				    "",
                    ":dataset",
                    "    a               ja:RDFDataset ;",
                    "    ja:defaultGraph :graph ;",
                    ".",
                    ":graph",
                    "    a               ja:MemoryModel ;",
                    ".",
                    "",
				    ":indexEmbeddedSolr",
                    // This is replaced during setup by an embedded Solr instance.
				    // <embedded:...> ends up with a huge dependency.
				    "    a text:TextIndexLucene ;",
                    "    text:directory <file:" + EmbeddedSolr.INDEX_PATH + "> ;",
                    "    text:entityMap :entMap ;",
				    
//                    "    a text:TextIndexSolr ;",
//                    "    text:server <embedded:SolrARQ> ;",
//                    "    text:entityMap :entMap ;",
                    "    .",
                    "",
				    ":entMap",
                    "    a text:EntityMap ;",
				    "    text:entityField      \"uri\" ;",
				    "    text:defaultField     \"label\" ;",
				    "    text:map (",
				    "         [ text:field \"label\" ; text:predicate rdfs:label ]",
				    "         [ text:field \"comment\" ; text:predicate rdfs:comment ]",
				    "         ) ."
				    );
	}
	
	
	
	@BeforeClass public static void beforeClass() throws IOException {
	    deleteOldFiles();
	    EmbeddedSolr.INDEX_DIR.mkdirs();
	    TextSearchUtil.createEmptyIndex(EmbeddedSolr.INDEX_DIR);
	    Reader reader = new StringReader(SPEC);
	    Model specModel = ModelFactory.createDefaultModel();
	    specModel.read(reader, "", "TURTLE");
	    TextAssembler.init();
	    Resource root = specModel.getResource(SPEC_ROOT_URI);
	    dataset = (Dataset) Assembler.general.open(root);
	    // Now replace the text indexc with embedded solr
	    TextIndex index = (TextIndex) dataset.getContext().get(TextQuery.textIndex) ;
	    EntityDefinition docDef = index.getDocDef() ;

	    String coreName = "SolrARQ" ;
	    CoreContainer.Initializer initializer = new CoreContainer.Initializer();
	    CoreContainer coreContainer = initializer.initialize();
	    SolrServer server = new EmbeddedSolrServer(coreContainer, coreName);

	    TextIndex textIndexSolrEmbedded = TextDatasetFactory.createSolrIndex(server, docDef) ;
	    dataset.getContext().set(TextQuery.textIndex, textIndexSolrEmbedded) ;
	}
	
	@AfterClass public static void afterClass() {
		TextIndexSolr index = (TextIndexSolr) dataset.getContext().get(TextQuery.textIndex) ;
		index.getServer().shutdown();
		deleteOldFiles();
	}

	@Override
    @Test public void testOneSimpleResult() {
		super.testOneSimpleResult();
	}

	@Override
    @Test public void testMultipleResults() {
		super.testMultipleResults();
	}

	@Override
    @Test public void testSearchCorrectField() {
		super.testSearchCorrectField();
	}

	@Override
    @Test public void testSearchDefaultField() {
		super.testSearchDefaultField();
	}

	@Override
    @Test public void testSearchLimitsResults() {
		super.testSearchLimitsResults(); 
	}
	
	public static void deleteOldFiles() {
		if (EmbeddedSolr.DATA_DIR.exists()) 
			TextSearchUtil.emptyAndDeleteDirectory(EmbeddedSolr.DATA_DIR);
	}
}
