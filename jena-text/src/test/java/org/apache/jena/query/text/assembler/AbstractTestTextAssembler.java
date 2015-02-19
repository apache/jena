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

import java.io.File ;

import org.apache.jena.query.text.TextSearchUtil ;
import org.junit.After ;
import org.junit.Before ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.tdb.assembler.VocabTDB ;
import com.hp.hpl.jena.vocabulary.RDF ;
import com.hp.hpl.jena.vocabulary.RDFS ;

public abstract class AbstractTestTextAssembler {
    
	protected static final Model model = ModelFactory.createDefaultModel();
	private static final String TESTBASE = "http://example.org/abstractTestTextAssembler/";
	protected static final Resource SIMPLE_DATASET_SPEC;
	protected static final Resource SIMPLE_INDEX_SPEC;
	protected static final Resource SIMPLE_INDEX_SPEC2;
	protected static final Resource SIMPLE_INDEX_SPEC3;
	protected static final Resource SIMPLE_INDEX_SPEC4;
	protected static final Resource SIMPLE_ENTITY_MAP_SPEC;
	protected static final Resource SIMPLE_INDEX_SPEC_LITERAL_DIR;
	protected static final Resource SIMPLE_INDEX_SPEC_MEM_DIR;
	
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
				     .addProperty(VocabTDB.pLocation, "target/test/testasm/DB");
		
		SIMPLE_INDEX_SPEC =
				model.createResource(TESTBASE + "simpleIndexSpec")
				     .addProperty(RDF.type, TextVocab.textIndexLucene)
				     .addProperty(TextVocab.pDirectory, model.createResource("file:target/test/testasm/simpleIndexSpec"))
				     .addProperty(TextVocab.pEntityMap, SIMPLE_ENTITY_MAP_SPEC);
		
		SIMPLE_INDEX_SPEC2 =
                model.createResource(TESTBASE + "simpleIndexSpec2")
                     .addProperty(RDF.type, TextVocab.textIndexLucene)
                     .addProperty(TextVocab.pDirectory, model.createResource("file:target/test/testasm/simpleIndexSpec2"))
                     .addProperty(TextVocab.pEntityMap, SIMPLE_ENTITY_MAP_SPEC);
		
		SIMPLE_INDEX_SPEC4 =
                model.createResource(TESTBASE + "simpleIndexSpec3")
                     .addProperty(RDF.type, TextVocab.textIndexLucene)
                     .addProperty(TextVocab.pDirectory, model.createResource("file:target/test/testasm/simpleIndexSpec3"))
                     .addProperty(TextVocab.pEntityMap, SIMPLE_ENTITY_MAP_SPEC);
		
		SIMPLE_INDEX_SPEC3 =
                model.createResource(TESTBASE + "simpleIndexSpec4")
                     .addProperty(RDF.type, TextVocab.textIndexLucene)
                     .addProperty(TextVocab.pDirectory, model.createResource("file:target/test/testasm/simpleIndexSpec4"))
                     .addProperty(TextVocab.pEntityMap, SIMPLE_ENTITY_MAP_SPEC);

		SIMPLE_INDEX_SPEC_LITERAL_DIR =
				model.createResource(TESTBASE + "simpleIndexLiteralDirSpec")
				     .addProperty(RDF.type, TextVocab.textIndexLucene)
				     .addProperty(TextVocab.pDirectory, model.createLiteral("target/test/testasm/simpleIndexLiteralDir"))
				     .addProperty(TextVocab.pEntityMap, SIMPLE_ENTITY_MAP_SPEC);

		SIMPLE_INDEX_SPEC_MEM_DIR =
				model.createResource(TESTBASE + "simpleIndexMemDirSpec")
				     .addProperty(RDF.type, TextVocab.textIndexLucene)
				     .addProperty(TextVocab.pDirectory, model.createLiteral("mem"))
				     .addProperty(TextVocab.pEntityMap, SIMPLE_ENTITY_MAP_SPEC);
	}
	
	protected void deleteFiles() {
	    File indexDir;
	    indexDir = new File("target/test/testasm/DB"); if (indexDir.exists()) TextSearchUtil.emptyAndDeleteDirectory(indexDir);
	    indexDir = new File("target/test/testasm/simpleIndexSpec"); if (indexDir.exists()) TextSearchUtil.emptyAndDeleteDirectory(indexDir);
	    indexDir = new File("target/test/testasm/simpleIndexSpec2"); if (indexDir.exists()) TextSearchUtil.emptyAndDeleteDirectory(indexDir);
	    indexDir = new File("target/test/testasm/simpleIndexSpec3"); if (indexDir.exists()) TextSearchUtil.emptyAndDeleteDirectory(indexDir);
	    indexDir = new File("target/test/testasm/simpleIndexSpec4"); if (indexDir.exists()) TextSearchUtil.emptyAndDeleteDirectory(indexDir);
	    indexDir = new File("target/test/testasm/simpleIndexLiteralDir"); if (indexDir.exists()) TextSearchUtil.emptyAndDeleteDirectory(indexDir);
	}
	
	@Before
    public void before() {
        deleteFiles();
        
        TextSearchUtil.createEmptyIndex(new File("target/test/testasm/DB"));
    }
	
	@After
    public void after() {
	    deleteFiles();
    }

}
