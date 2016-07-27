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

package org.apache.jena.osgi.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;

import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.tdb.TDBFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 * Brief tests of the Jena modules covered by jena-osgi
 * 
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class JenaOSGITest {

	@Configuration
	public Option[] config() {
	    return options(
		// OSGi container configuration
				karafDistributionConfiguration().frameworkUrl(
						maven().groupId("org.apache.karaf")
								.artifactId("apache-karaf").type("zip")
								/*
								 * Version 4.0.4 does not work at the moment:
								 * Error: Could not find or load main class
								 * org.apache.karaf.main.Main (layout of the
								 * archive/file naming changed).
								 */
								.version("3.0.6")).useDeployFolder(false),
				// Install core Jena feature
				features(
						maven().groupId("org.apache.jena")
								.artifactId("jena-osgi-features").type("xml")
								.classifier("features")
								.version("3.1.0-SNAPSHOT"), "jena"));

	}

	private static final String EXAMPLE_COM_GRAPH = "http://example.com/graph";
	private Resource alice;
	private Property knows;
	private Resource bob;

	@Test
	public void testJenaCore() throws Exception {
		Model model = makeModel();
		Writer writer = new StringWriter();
		model.write(writer, "N-Triples");

		assertEquals(
				"<http://example.com/alice> <http://xmlns.com/foaf/0.1/knows> <http://example.com/bob> .",
				writer.toString().trim());

		OntModel ontModel = ModelFactory
				.createOntologyModel(OntModelSpec.OWL_DL_MEM_RULE_INF);
		ObjectProperty knowsObjProp = ontModel.createObjectProperty(knows
				.getURI());
		ObjectProperty hasFriend = ontModel
				.createObjectProperty("http://example.com/has_friend");
		hasFriend.addSuperProperty(knowsObjProp);

		Individual aliceIndividual = ontModel.createIndividual(alice);
		Individual bobIndividiual = ontModel.createIndividual(bob);
		ontModel.add(aliceIndividual, hasFriend, bobIndividiual);
		assertTrue(aliceIndividual.hasProperty(knowsObjProp, bobIndividiual));
	}

	@Test
	public void testJenaArq1() throws Exception {
	    Dataset dataset = DatasetFactory.create();
	    testJenaARQ(dataset) ;
	}
	
	@Test
	public void testJenaArq2() throws Exception {
	    Dataset dataset = DatasetFactory.createTxnMem();
	}
	
	public void testJenaARQ(Dataset dataset) throws Exception {
		dataset.addNamedModel(EXAMPLE_COM_GRAPH, makeModel());

		// We test JSON-LD as it involves multiple other bundles
		Path path = Files.createTempFile("example", ".jsonld");
		path.toFile().deleteOnExit();

		try (OutputStream output = Files.newOutputStream(path)) {
			RDFDataMgr.write(output, dataset, Lang.JSONLD);
		}
		Dataset dataset2 = RDFDataMgr.loadDataset(path.toUri().toString());
		assertTrue(dataset2.containsNamedModel(EXAMPLE_COM_GRAPH));

		runQuery(dataset2);

	}

	private void runQuery(Dataset dataset) {
		Query query = QueryFactory.create(""
				+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
				+ "SELECT ?bob WHERE { "
				+ "  GRAPH <http://example.com/graph> { "
				+ "      ?alice foaf:knows ?bob . " + "  }" + "}");
		try (QueryExecution qexec = QueryExecutionFactory
				.create(query, dataset)) {
			ResultSet results = qexec.execSelect();
			assertTrue(results.hasNext());
			QuerySolution r = results.next();
			assertEquals(bob, r.get("bob").asResource());
		}
	}

	@Test
	public void testJenaIRI() throws Exception {
		IRIFactory iriFactory = IRIFactory.iriImplementation() ;
		IRI iri = iriFactory.create("http://example.com/");
		assertEquals("http://example.com/", iri.toASCIIString());
	}

	@Test
	public void testJenaTdb() throws Exception {
		Path tdbDir = Files.createTempDirectory("jena-tdb-test");
		Dataset dataset = TDBFactory.createDataset(tdbDir.toString());

		dataset.begin(ReadWrite.WRITE);
		dataset.addNamedModel(EXAMPLE_COM_GRAPH, makeModel());
		dataset.commit();
		dataset.end();

		dataset.begin(ReadWrite.READ);
		runQuery(dataset);
		dataset.end();
	}

	@Test
	public void testMaking() {
		DatasetFactory.createTxnMem() ;
		DatasetFactory.create() ;
		ModelFactory.createDefaultModel();
	}
	
	private Model makeModel() {
		Model model = ModelFactory.createDefaultModel();
		alice = model.createResource("http://example.com/alice");
		knows = model.createProperty("http://xmlns.com/foaf/0.1/knows");
		bob = model.createResource("http://example.com/bob");
		model.add(model.createStatement(alice, knows, bob));
		return model;
	}
}
