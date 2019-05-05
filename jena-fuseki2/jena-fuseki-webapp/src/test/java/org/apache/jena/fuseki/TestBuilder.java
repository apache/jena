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

package org.apache.jena.fuseki;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.apache.jena.fuseki.build.DatasetDescriptionMap;
import org.apache.jena.fuseki.build.FusekiConfig;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.tdb.assembler.VocabTDB;
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;

public class TestBuilder {
	
	private static final Model dsModel;
	private static final Resource dsDesc1;
	private static final Resource dsDesc2;
	private DatasetDescriptionMap registry = new DatasetDescriptionMap();
	
	@Test
	public void testVerifySameDatasetObjectForSameDescription() {
		
		Dataset ds1 = FusekiConfig.getDataset(dsDesc1, registry);
		Dataset ds2 = FusekiConfig.getDataset(dsDesc1, registry);
		assertEquals(ds1, ds2);
	}
	
	@Test
	public void testVerifyDifferentDatasetObjectsForDifferentDescriptions() {
		
		Dataset ds1 = FusekiConfig.getDataset(dsDesc1, registry);
		Dataset ds2 = FusekiConfig.getDataset(dsDesc2, registry);
		assertNotEquals(ds1, ds2);		
	}
	
	static {
		dsModel = ModelFactory.createDefaultModel();
		dsDesc1 = dsModel.createResource()
		         .addProperty(RDF.type, VocabTDB.tDatasetTDB)
		         .addProperty(VocabTDB.pLocation, "--mem--")
		;
		dsDesc2 = dsModel.createResource()
		         .addProperty(RDF.type, VocabTDB.tDatasetTDB)
		         .addProperty(VocabTDB.pLocation, "--mem--")
		;
	}
}
