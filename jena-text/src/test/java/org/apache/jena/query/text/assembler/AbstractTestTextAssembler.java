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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.tdb.assembler.VocabTDB;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public abstract class AbstractTestTextAssembler {
	protected static final Model model = ModelFactory.createDefaultModel();
	private static final String TESTBASE = "http://example.org/abstractTestTextAssembler/";
	protected static final Resource SIMPLE_DATASET_SPEC;
	protected static final Resource SIMPLE_INDEX_SPEC;
	protected static final Resource SIMPLE_ENTITY_MAP_SPEC;
	
	static {
		SIMPLE_ENTITY_MAP_SPEC = 
				model.createResource(TESTBASE + "simpleEntityMapSpec")
				     .addProperty(RDF.type, TextVocab.entityMap)
				     .addProperty(TextVocab.pEntityField, "entityField")
				     .addProperty(TextVocab.pDefaultField, "defaultField")
				     .addProperty(TextVocab.pMap,
				    		      model.createList(
				    		    		  new RDFNode[] {
				    		    				model.createResource()
				    		    				     .addProperty(TextVocab.pField, "defaultField")
				    		    				     .addProperty(TextVocab.pPredicate, RDFS.label)
				    		    		  }))
				     ;
		SIMPLE_DATASET_SPEC =
				model.createResource(TESTBASE + "simpleDatasetSpec")
				     .addProperty(RDF.type, VocabTDB.tDatasetTDB)
				     .addProperty(VocabTDB.pLocation, "target/test/DB");
		
		SIMPLE_INDEX_SPEC =
				model.createResource(TESTBASE + "simpleIndexSpec")
				     .addProperty(RDF.type, TextVocab.textIndexLucene)
				     .addProperty(TextVocab.pDirectory, model.createResource("file:target/test/simpleLuceneIndex"))
				     .addProperty(TextVocab.pEntityMap, SIMPLE_ENTITY_MAP_SPEC);
	}

}
