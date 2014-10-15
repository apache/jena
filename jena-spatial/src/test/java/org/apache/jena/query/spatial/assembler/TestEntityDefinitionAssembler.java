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

import static org.junit.Assert.assertEquals;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.spatial.EntityDefinition;
import org.apache.jena.query.spatial.SpatialIndexException;
import org.apache.jena.query.spatial.SpatialQuery;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Test assembler for EntityDefinition
 */
public class TestEntityDefinitionAssembler {

	private static final String TESTBASE = "http://example.org/test/";
	private static final Resource spec0;
	private static final Resource spec1;
	private static final Resource spec2;
	private static final Resource spec3;
	private static final Resource specNoEntityField;
	private static final Resource specNoGeoField;

	@Test
	public void EntityHasGeoield() {
		EntityDefinitionAssembler emAssembler = new EntityDefinitionAssembler();
		EntityDefinition entityDef = emAssembler.open(null, spec0, null);
		assertEquals(SPEC0_GEO_FIELD, entityDef.getGeoField());
	}

	@Test
	public void EntityHasEntityField() {
		EntityDefinitionAssembler emAssembler = new EntityDefinitionAssembler();
		EntityDefinition entityDef = emAssembler.open(null, spec0, null);
		assertEquals(SPEC0_ENTITY_FIELD, entityDef.getEntityField());
	}

	@Test
	public void EntityHasPair() {
		EntityDefinitionAssembler emAssembler = new EntityDefinitionAssembler();
		EntityDefinition entityDef = emAssembler.open(null, spec1, null);
		assertEquals(1, entityDef.getCustomSpatialPredicatePairCount());
		assertEquals(true, entityDef.hasSpatialPredicatePair(
				SPEC1_LATITUDE.asNode(), SPEC1_LONGITUDE.asNode()));

	}

	@Test
	public void EntityHasWKT() {
		EntityDefinitionAssembler emAssembler = new EntityDefinitionAssembler();
		EntityDefinition entityDef = emAssembler.open(null, spec1, null);
		assertEquals(1, entityDef.getCustomWKTPredicateCount());
		assertEquals(true, entityDef.isWKTPredicate(SPEC1_WKT.asNode()));

	}

	@Test
	public void EntityHasMultiplePairsAndWKTs() {
		EntityDefinitionAssembler emAssembler = new EntityDefinitionAssembler();
		EntityDefinition entityDef = emAssembler.open(null, spec2, null);
		assertEquals(2, entityDef.getCustomSpatialPredicatePairCount());
		assertEquals(true, entityDef.hasSpatialPredicatePair(
				SPEC2_LATITUDE_1.asNode(), SPEC2_LONGITUDE_1.asNode()));
		assertEquals(true, entityDef.hasSpatialPredicatePair(
				SPEC2_LATITUDE_2.asNode(), SPEC2_LONGITUDE_2.asNode()));
		assertEquals(false, entityDef.hasSpatialPredicatePair(
				SPEC2_LATITUDE_1.asNode(), SPEC2_LONGITUDE_2.asNode()));
		assertEquals(false, entityDef.hasSpatialPredicatePair(
				SPEC2_LATITUDE_2.asNode(), SPEC2_LONGITUDE_1.asNode()));
		assertEquals(2, entityDef.getCustomWKTPredicateCount());
		assertEquals(true, entityDef.isWKTPredicate(SPEC2_WKT_1.asNode()));
		assertEquals(true, entityDef.isWKTPredicate(SPEC2_WKT_2.asNode()));
		assertEquals(false, entityDef.isWKTPredicate(SPEC1_WKT.asNode()));
	}

	@Test
	public void EntityHasSpatialContextFactory() {
		boolean jts_lib_ready = false;
		try {
			Class.forName("com.vividsolutions.jts.geom.Geometry");
			jts_lib_ready = true;
		} catch (ClassNotFoundException e) {
			Log.warn(this,
					"JTS lib is not ready in classpath! An exception should be thrown later on!");
		}

		EntityDefinitionAssembler emAssembler = new EntityDefinitionAssembler();
		EntityDefinition entityDef = emAssembler.open(null, spec3, null);
		if (jts_lib_ready) {
			assertEquals("com.spatial4j.core.context.jts.JtsSpatialContext",
					SpatialQuery.ctx.getClass().getName());
		} else {
			assertEquals("com.spatial4j.core.context.SpatialContext",
					SpatialQuery.ctx.getClass().getName());
		}

	}

	@Test(expected = SpatialIndexException.class)
	public void errorOnNoEntityField() {
		EntityDefinitionAssembler emAssembler = new EntityDefinitionAssembler();
		emAssembler.open(null, specNoEntityField, null);
	}

	@Test(expected = SpatialIndexException.class)
	public void errorOnNoGeoField() {
		EntityDefinitionAssembler emAssembler = new EntityDefinitionAssembler();
		emAssembler.open(null, specNoGeoField, null);
	}

	private static final String SPEC0_ENTITY_FIELD = "spec0EntityField";
	private static final String SPEC0_GEO_FIELD = "spec0GeoField";

	private static final String SPEC1_ENTITY_FIELD = "spec1EntityField";
	private static final String SPEC1_GEO_FIELD = "spec1GeoField";
	private static final Resource SPEC1_LATITUDE = ResourceFactory
			.createResource(TESTBASE + "latitude");
	private static final Resource SPEC1_LONGITUDE = ResourceFactory
			.createResource(TESTBASE + "longitude");
	private static final Resource SPEC1_WKT = ResourceFactory
			.createResource(TESTBASE + "wkt");

	private static final String SPEC2_ENTITY_FIELD = "spec2EntityField";
	private static final String SPEC2_GEO_FIELD = "spec2DefaultField";
	private static final Resource SPEC2_LATITUDE_1 = ResourceFactory
			.createResource(TESTBASE + "latitude_1");
	private static final Resource SPEC2_LONGITUDE_1 = ResourceFactory
			.createResource(TESTBASE + "longitude_1");
	private static final Resource SPEC2_LATITUDE_2 = ResourceFactory
			.createResource(TESTBASE + "latitude_2");
	private static final Resource SPEC2_LONGITUDE_2 = ResourceFactory
			.createResource(TESTBASE + "longitude_2");
	private static final Resource SPEC2_WKT_1 = ResourceFactory
			.createResource(TESTBASE + "wkt_1");
	private static final Resource SPEC2_WKT_2 = ResourceFactory
			.createResource(TESTBASE + "wkt_2");

	static {

		// create a mininal specification
		Model model = ModelFactory.createDefaultModel();
		model = ModelFactory.createDefaultModel();
		spec0 = model.createResource(TESTBASE + "spec0")
				.addProperty(SpatialVocab.pEntityField, SPEC0_ENTITY_FIELD)
				.addProperty(SpatialVocab.pGeoField, SPEC0_GEO_FIELD);

		// create a simple pair specification
		model = ModelFactory.createDefaultModel();
		spec1 = model
				.createResource(TESTBASE + "spec1")
				.addProperty(SpatialVocab.pEntityField, SPEC1_ENTITY_FIELD)
				.addProperty(SpatialVocab.pGeoField, SPEC1_GEO_FIELD)
				.addProperty(
						SpatialVocab.pHasSpatialPredicatePairs,
						model.createList(new RDFNode[] { model
								.createResource()
								.addProperty(SpatialVocab.pLatitude,
										SPEC1_LATITUDE)
								.addProperty(SpatialVocab.pLongitude,
										SPEC1_LONGITUDE) }))
				.addProperty(SpatialVocab.pHasWKTPredicates,
						model.createList(new RDFNode[] { SPEC1_WKT }));

		// create an entity definition specification with multiple pairs and
		// wkts
		model = ModelFactory.createDefaultModel();
		spec2 = model
				.createResource(TESTBASE + "spec2")
				.addProperty(SpatialVocab.pEntityField, SPEC2_ENTITY_FIELD)
				.addProperty(SpatialVocab.pGeoField, SPEC2_GEO_FIELD)
				.addProperty(
						SpatialVocab.pHasSpatialPredicatePairs,
						model.createList(new RDFNode[] {
								model.createResource()
										.addProperty(SpatialVocab.pLatitude,
												SPEC2_LATITUDE_1)
										.addProperty(SpatialVocab.pLongitude,
												SPEC2_LONGITUDE_1),
								model.createResource()
										.addProperty(SpatialVocab.pLatitude,
												SPEC2_LATITUDE_2)
										.addProperty(SpatialVocab.pLongitude,
												SPEC2_LONGITUDE_2) }))
				.addProperty(
						SpatialVocab.pHasWKTPredicates,
						model.createList(new RDFNode[] { SPEC2_WKT_1,
								SPEC2_WKT_2 }));

		// create an entity definition specification with spatialContextFactory
		model = ModelFactory.createDefaultModel();
		spec3 = model
				.createResource(TESTBASE + "spec0")
				.addProperty(SpatialVocab.pEntityField, SPEC0_ENTITY_FIELD)
				.addProperty(SpatialVocab.pGeoField, SPEC0_GEO_FIELD)
				.addProperty(SpatialVocab.pSpatialContextFactory,
						SpatialQuery.JTS_SPATIAL_CONTEXT_FACTORY_CLASS);

		// bad assembler spec
		model = ModelFactory.createDefaultModel();
		specNoEntityField = model
				.createResource(TESTBASE + "specNoEntityField").addProperty(
						SpatialVocab.pGeoField, SPEC0_GEO_FIELD);

		// bad assembler spec
		model = ModelFactory.createDefaultModel();
		specNoGeoField = model.createResource(TESTBASE + "specNoGeoField")
				.addProperty(SpatialVocab.pEntityField, SPEC0_ENTITY_FIELD);

	}
}
