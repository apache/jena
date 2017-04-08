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

package org.apache.jena.query.spatial.assembler;

import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.rdf.model.RDFNode ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.tdb.assembler.VocabTDB ;
import org.apache.jena.vocabulary.RDF ;

public abstract class AbstractTestSpatialAssembler {
	protected static final Model model = ModelFactory.createDefaultModel();
	private static final String TESTBASE = "http://example.org/abstractTestSpatialAssembler/";
	protected static final Resource SIMPLE_DATASET_SPEC;
	protected static final Resource SIMPLE_LUCENE_INDEX_SPEC;
	protected static final Resource SIMPLE_ENTITY_DEFINITION_SPEC;
	
	static {
		SIMPLE_ENTITY_DEFINITION_SPEC = 
				model.createResource(TESTBASE + "simpleEntityDefinitionSpec")
				     .addProperty(RDF.type, SpatialVocab.definition)
				     .addProperty(SpatialVocab.pEntityField, "uri")
				     .addProperty(SpatialVocab.pGeoField, "geo")
				     .addProperty(SpatialVocab.pHasSpatialPredicatePairs,
				    		      model.createList(
				    		    		  new RDFNode[] {
				    		    				model.createResource()
				    		    				     .addProperty(SpatialVocab.pLatitude, model.createResource(TESTBASE+"latitude_1"))
				    		    				     .addProperty(SpatialVocab.pLongitude, model.createResource(TESTBASE+"longitude_1")),
				    		    				model.createResource()
				    		    				     .addProperty(SpatialVocab.pLatitude, model.createResource(TESTBASE+"latitude_2"))
				    		    				     .addProperty(SpatialVocab.pLongitude, model.createResource(TESTBASE+"longitude_2")),
				    		    		  }))
				     .addProperty(SpatialVocab.pHasWKTPredicates,
				    		      model.createList(
				    		    		  new RDFNode[] {
				    		    				  model.createResource(TESTBASE+"wkt_1"),
				    		    				  model.createResource(TESTBASE+"wkt_2")
				    		    		  }))
				     ;
		SIMPLE_DATASET_SPEC =
				model.createResource(TESTBASE + "simpleDatasetSpec")
				     .addProperty(RDF.type, VocabTDB.tDatasetTDB)
				     .addProperty(VocabTDB.pLocation, "target/test/simpleDB");
		
		SIMPLE_LUCENE_INDEX_SPEC =
				model.createResource(TESTBASE + "simpleLuceneIndexSpec")
				     .addProperty(RDF.type, SpatialVocab.spatialIndexLucene)
				     .addProperty(SpatialVocab.pDirectory, model.createResource("file:target/test/simpleLuceneIndex"))
				     .addProperty(SpatialVocab.pDefinition, SIMPLE_ENTITY_DEFINITION_SPEC);
		}

}
