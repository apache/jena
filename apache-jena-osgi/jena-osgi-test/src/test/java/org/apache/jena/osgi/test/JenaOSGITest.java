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
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
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
								.version("3.0.6")).useDeployFolder(false),
				
				//mavenBundle("org.apache.aries.spifly","org.apache.aries.spifly.dynamic.bundle", "1.0.8"),

				mavenBundle("org.apache.jena", "jena-osgi", "3.1.0-SNAPSHOT"),
				mavenBundle("com.github.andrewoma.dexx", "collection", "0.6.0-SNAPSHOT"),
				mavenBundle("com.github.jsonld-java", "jsonld-java", "0.8.0"),
				mavenBundle("org.apache.httpcomponents", "httpcore-osgi","4.4.4"),
				mavenBundle("org.apache.httpcomponents", "httpclient-osgi","4.5.1"),
				mavenBundle("commons-cli", "commons-cli", "1.3.1"),
				mavenBundle("org.apache.commons", "commons-csv", "1.2"),
				mavenBundle("org.apache.commons", "commons-lang3", "3.4"),
				mavenBundle("org.apache.thrift", "libthrift", "0.9.3"),
				mavenBundle("com.fasterxml.jackson.core", "jackson-core","2.6.3"),
				mavenBundle("com.fasterxml.jackson.core", "jackson-databind","2.6.3"),
				mavenBundle("com.fasterxml.jackson.core","jackson-annotations", "2.6.3")

		);

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
	public void testJenaArq() throws Exception {
		Dataset dataset = DatasetFactory.createMem();
		dataset.addNamedModel(EXAMPLE_COM_GRAPH, makeModel());

		Path path = Files.createTempFile("example", ".jsonld");
		// System.out.println(path);
		path.toFile().deleteOnExit();

		try (OutputStream output = Files.newOutputStream(path)) {
			RDFDataMgr.write(output, dataset, Lang.JSONLD);
		}
		// We test JSON-LD as it involves multiple other bundles

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
		IRIFactory iriFactory = IRIFactory.jenaImplementation();
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

	private Model makeModel() {
		Model model = ModelFactory.createDefaultModel();
		alice = model.createResource("http://example.com/alice");
		knows = model.createProperty("http://xmlns.com/foaf/0.1/knows");
		bob = model.createResource("http://example.com/bob");
		model.add(model.createStatement(alice, knows, bob));
		return model;
	}
}
