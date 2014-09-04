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

import java.util.Collection ;

import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.query.text.EntityDefinition ;
import org.apache.jena.query.text.TextIndexException ;
import org.apache.jena.query.text.analyzer.LowerCaseKeywordAnalyzer ;
import org.apache.lucene.analysis.core.KeywordAnalyzer ;
import org.apache.lucene.analysis.core.SimpleAnalyzer ;
import org.apache.lucene.analysis.standard.StandardAnalyzer ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.assembler.Assembler ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.vocabulary.RDF ;
import com.hp.hpl.jena.vocabulary.RDFS ;

/**
 * Test assembler for EntityMap
 */
public class TestEntityMapAssembler {
    
    // Suppress warnings
    @BeforeClass public static void beforeClass() { LogCtl.setError(EntityDefinitionAssembler.class); }
    @AfterClass  public static void afterClass()  { LogCtl.setInfo(EntityDefinitionAssembler.class); }
	
	private static final String TESTBASE = "http://example.org/test/";
	private static final Resource spec1;
	private static final Resource spec2;
	private static final Resource spec3;
	private static final Resource spec4;
	private static final Resource spec5;
	private static final Resource spec6;
	private static final Resource specNoEntityField;
	private static final Resource specNoDefaultField;
	private static final Resource specNoMapProperty;
	private static final Resource specNoPrimaryFieldDef;
	
	@Test public void EntityHasPrimaryField() {
		EntityDefinitionAssembler entDefAssem = new EntityDefinitionAssembler();
		EntityDefinition entityDef = entDefAssem.open(null, spec1, null);
		assertEquals(SPEC1_DEFAULT_FIELD, entityDef.getPrimaryField());
	}
	
	@Test public void EntityHasEntityField() {
		EntityDefinitionAssembler entDefAssem = new EntityDefinitionAssembler();
		EntityDefinition entityDef = entDefAssem.open(null, spec1, null);
		assertEquals(SPEC1_ENTITY_FIELD, entityDef.getEntityField());		
	}
	
	@Test public void EntityHasMapEntries() {
		EntityDefinitionAssembler entDefAssem = new EntityDefinitionAssembler();
		EntityDefinition entityDef = entDefAssem.open(null, spec1, null);
		assertEquals(SPEC1_PREDICATE.asNode(), getOne(entityDef,SPEC1_DEFAULT_FIELD));
	}
	
	private Object getOne(EntityDefinition entityDef, String field) {
	    Collection<Node> x = entityDef.getPredicates(field) ;
	    if ( x == null || x.size() == 0 )
	        return null ;
	    if ( x.size() != 1 )
	        throw new InternalErrorException("Not unique: "+field) ;
        return x.iterator().next() ; 
    }

    @Test public void EntityHasMultipleMapEntries() {
		EntityDefinitionAssembler entDefAssem = new EntityDefinitionAssembler();
		EntityDefinition entityDef = entDefAssem.open(null, spec2, null);
		assertEquals(SPEC2_PREDICATE1.asNode(), getOne(entityDef,SPEC2_DEFAULT_FIELD));
		assertEquals(SPEC2_PREDICATE2.asNode(), getOne(entityDef, SPEC2_FIELD2));
	}
    
    @Test public void EntityHasMapEntryWithSimpleAnalyzer() {
    	EntityDefinitionAssembler entDefAssem = new EntityDefinitionAssembler();
    	EntityDefinition entityDef = entDefAssem.open(Assembler.general, spec3,  null);
    	assertEquals(SimpleAnalyzer.class, entityDef.getAnalyzer(SPEC1_DEFAULT_FIELD).getClass());
    }
    
    @Test public void EntityHasMapEntryWithStandardAnalyzerAndStopWords() {
    	EntityDefinitionAssembler entDefAssem = new EntityDefinitionAssembler();
    	EntityDefinition entityDef = entDefAssem.open(Assembler.general, spec4,  null);
    	assertEquals(StandardAnalyzer.class, entityDef.getAnalyzer(SPEC1_DEFAULT_FIELD).getClass());
    }
    
    @Test public void EntityHasMapEntryWithKeywordAnalyzer() {
    	EntityDefinitionAssembler entDefAssem = new EntityDefinitionAssembler();
    	EntityDefinition entityDef = entDefAssem.open(Assembler.general, spec5,  null);
    	assertEquals(KeywordAnalyzer.class, entityDef.getAnalyzer(SPEC1_DEFAULT_FIELD).getClass());
    }    
	
    @Test public void EntityHasMapEntryWithLowerCaseKeywordAnalyzer() {
    	EntityDefinitionAssembler entDefAssem = new EntityDefinitionAssembler();
    	EntityDefinition entityDef = entDefAssem.open(Assembler.general, spec6,  null);
    	assertEquals(LowerCaseKeywordAnalyzer.class, entityDef.getAnalyzer(SPEC1_DEFAULT_FIELD).getClass());
    }    
	
	@Test(expected=TextIndexException.class) public void errorOnNoEntityField() {
		EntityDefinitionAssembler entDefAssem = new EntityDefinitionAssembler();
		entDefAssem.open(null, specNoEntityField, null);
	}
	
	@Test(expected=TextIndexException.class) public void errorOnNoDefaultField() {
		EntityDefinitionAssembler entDefAssem = new EntityDefinitionAssembler();
		entDefAssem.open(null, specNoDefaultField, null);
	}
	
	@Test(expected=TextIndexException.class) public void errorOnNoMapProperty() {
		EntityDefinitionAssembler entDefAssem = new EntityDefinitionAssembler();
		entDefAssem.open(null, specNoMapProperty, null);
	}
	
	@Test(expected=TextIndexException.class) public void errorOnNoPrimaryFieldDef() {
		EntityDefinitionAssembler entDefAssem = new EntityDefinitionAssembler();
		try {
		    entDefAssem.open(null, specNoPrimaryFieldDef, null);
		} catch (TextIndexException e) {
			assertTrue(e.getMessage().contains(SPEC1_DEFAULT_FIELD));
			throw e ;
		}	
		
	}
	
	private static final String SPEC1_ENTITY_FIELD = "spec1EntityField";
	private static final String SPEC1_DEFAULT_FIELD = "spec1DefaultField";
	private static final Property SPEC1_PREDICATE = RDFS.label;
	
	private static final String SPEC2_ENTITY_FIELD = "spec2EntityField";
	private static final String SPEC2_DEFAULT_FIELD = "spec2DefaultField";
	private static final String SPEC2_FIELD2 = "spec2Field2";
	private static final Property SPEC2_PREDICATE1 = RDFS.label;
	private static final Property SPEC2_PREDICATE2 = RDFS.comment;
	static {
		TextAssembler.init();
		Model model = ModelFactory.createDefaultModel();
		
		// create a simple entity map specification
		
		spec1 = model.createResource(TESTBASE + "spec1")
				     .addProperty(TextVocab.pEntityField, SPEC1_ENTITY_FIELD)
				     .addProperty(TextVocab.pDefaultField, SPEC1_DEFAULT_FIELD)
				     .addProperty(TextVocab.pMap,
				    		      model.createList(
				    		    		  new RDFNode[] {
				    		    				model.createResource()
				    		    				     .addProperty(TextVocab.pField, SPEC1_DEFAULT_FIELD)
				    		    				     .addProperty(TextVocab.pPredicate, SPEC1_PREDICATE)
				    		    		  }))
				     ;
		
		// create an entity map specification with multiple map entries
		
		spec2 = model.createResource(TESTBASE + "spec2")
				     .addProperty(TextVocab.pEntityField, SPEC2_ENTITY_FIELD)
				     .addProperty(TextVocab.pDefaultField, SPEC2_DEFAULT_FIELD)
				     .addProperty(TextVocab.pMap,
				    		      model.createList(
				    		    		  new RDFNode[] {
				    		    				model.createResource()
				    		    				     .addProperty(TextVocab.pField, SPEC2_DEFAULT_FIELD)
				    		    				     .addProperty(TextVocab.pPredicate, SPEC2_PREDICATE1),
							    		    	model.createResource()
							    		    	     .addProperty(TextVocab.pField, SPEC2_FIELD2)
							    		    		 .addProperty(TextVocab.pPredicate, SPEC2_PREDICATE2),
				    		    				    
				    		    		  }))
				     ;
		
		// create a simple entity map specification using a keyword analyzer
		
				spec3 = model.createResource(TESTBASE + "spec3")
						     .addProperty(TextVocab.pEntityField, SPEC1_ENTITY_FIELD)
						     .addProperty(TextVocab.pDefaultField, SPEC1_DEFAULT_FIELD)
						     .addProperty(TextVocab.pMap,
						    		      model.createList(
						    		    		  new RDFNode[] {
						    		    				model.createResource()
						    		    				     .addProperty(TextVocab.pField, SPEC1_DEFAULT_FIELD)
						    		    				     .addProperty(TextVocab.pPredicate, SPEC1_PREDICATE)
						    		    				     .addProperty(TextVocab.pAnalyzer, 
						    		    				    		      model.createResource()
						    		    				    		           .addProperty(RDF.type, TextVocab.simpleAnalyzer))
						    		    		  }))
						     ;
				// create a simple entity map specification using a standard analyzer with stop words
				
				spec4 = model.createResource(TESTBASE + "spec4")
						     .addProperty(TextVocab.pEntityField, SPEC1_ENTITY_FIELD)
						     .addProperty(TextVocab.pDefaultField, SPEC1_DEFAULT_FIELD)
						     .addProperty(TextVocab.pMap,
						    		      model.createList(
						    		    		  new RDFNode[] {
						    		    				model.createResource()
						    		    				     .addProperty(TextVocab.pField, SPEC1_DEFAULT_FIELD)
						    		    				     .addProperty(TextVocab.pPredicate, SPEC1_PREDICATE)
						    		    				     .addProperty(TextVocab.pAnalyzer, 
						    		    				    		      model.createResource()
						    		    				    		           .addProperty(RDF.type, TextVocab.standardAnalyzer))
						    		    				    		           .addProperty(TextVocab.pStopWords, toList(model, "and the but"))
						    		    		  }));		
		// create a simple entity map specification using a keyword analyzer
		
				spec5 = model.createResource(TESTBASE + "spec5")
						     .addProperty(TextVocab.pEntityField, SPEC1_ENTITY_FIELD)
						     .addProperty(TextVocab.pDefaultField, SPEC1_DEFAULT_FIELD)
						     .addProperty(TextVocab.pMap,
						    		      model.createList(
						    		    		  new RDFNode[] {
						    		    				model.createResource()
						    		    				     .addProperty(TextVocab.pField, SPEC1_DEFAULT_FIELD)
						    		    				     .addProperty(TextVocab.pPredicate, SPEC1_PREDICATE)
						    		    				     .addProperty(TextVocab.pAnalyzer, 
						    		    				    		      model.createResource()
						    		    				    		           .addProperty(RDF.type, TextVocab.keywordAnalyzer))
						    		    		  }));
				
		// create a simple entity map specification using a lowercase keyword analyzer
		
				spec6 = model.createResource(TESTBASE + "spec6")
						     .addProperty(TextVocab.pEntityField, SPEC1_ENTITY_FIELD)
						     .addProperty(TextVocab.pDefaultField, SPEC1_DEFAULT_FIELD)
						     .addProperty(TextVocab.pMap,
						    		      model.createList(
						    		    		  new RDFNode[] {
						    		    				model.createResource()
						    		    				     .addProperty(TextVocab.pField, SPEC1_DEFAULT_FIELD)
						    		    				     .addProperty(TextVocab.pPredicate, SPEC1_PREDICATE)
						    		    				     .addProperty(TextVocab.pAnalyzer, 
						    		    				    		      model.createResource()
						    		    				    		           .addProperty(RDF.type, TextVocab.lowerCaseKeywordAnalyzer))
						    		    		  }));
				
		// bad assembler spec
				
		specNoEntityField = 
				model.createResource(TESTBASE + "specNoEntityField")
				     .addProperty(TextVocab.pDefaultField, SPEC1_DEFAULT_FIELD)
				     .addProperty(TextVocab.pMap,
				    		      model.createList(
				    		    		  new RDFNode[] {
				    		    				model.createResource()
				    		    				     .addProperty(TextVocab.pField, SPEC1_DEFAULT_FIELD)
				    		    				     .addProperty(TextVocab.pPredicate, SPEC1_PREDICATE)
				    		    		  }))
				     ;
		
		// bad assembler spec
		
		specNoDefaultField = 
				model.createResource(TESTBASE + "specNoDefaultField")
				     .addProperty(TextVocab.pDefaultField, SPEC1_DEFAULT_FIELD)
				     .addProperty(TextVocab.pMap,
				    		      model.createList(
				    		    		  new RDFNode[] {
				    		    				model.createResource()
				    		    				     .addProperty(TextVocab.pField, SPEC1_DEFAULT_FIELD)
				    		    				     .addProperty(TextVocab.pPredicate, SPEC1_PREDICATE)
				    		    		  }))
				     ;
		// bad assembler spec
		specNoMapProperty = 
				model.createResource(TESTBASE + "specNoMapProperty")
				     .addProperty(TextVocab.pEntityField, SPEC1_ENTITY_FIELD)
				     .addProperty(TextVocab.pDefaultField, SPEC1_DEFAULT_FIELD)
				     ;
		
		// bad assembler spec		
		specNoPrimaryFieldDef =
				model.createResource(TESTBASE + "noPrimaryFieldDef")
				     .addProperty(TextVocab.pEntityField, SPEC1_ENTITY_FIELD)
				     .addProperty(TextVocab.pDefaultField, SPEC1_DEFAULT_FIELD)
				     .addProperty(TextVocab.pMap,
				    		      model.createList(
				    		    		  new RDFNode[] {
				    		    				model.createResource()
				    		    				     .addProperty(TextVocab.pField, SPEC1_ENTITY_FIELD)
				    		    				     .addProperty(TextVocab.pPredicate, SPEC1_PREDICATE)
				    		    		  }))
				     ;
	}
	
	private static Resource toList(Model model, String string) {
		String[] members = string.split("\\s");
		Resource current = RDF.nil;
		for (int i = members.length-1; i>=0; i--) {
			Resource previous = current;
			current = model.createResource();
			current.addProperty(RDF.rest, previous);
			current.addProperty(RDF.first, members[i]);			
		}
		return current;	
	}
}
