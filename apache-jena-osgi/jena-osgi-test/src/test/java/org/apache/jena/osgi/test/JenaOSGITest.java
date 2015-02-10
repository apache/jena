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
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.linkBundle;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.inject.Inject;

import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.BundleContext;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.tdb.TDBFactory;

/**
 * Brief tests of the Jena modules covered by jena-osgi
 * 
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class JenaOSGITest {

	@Inject
	private BundleContext bc;

	@Configuration
	public Option[] config() {
		return options(
        // bundle with org.slf4j implementation
				linkBundle("org.ops4j.pax.logging.pax-logging-log4j2"),
				linkBundle("org.ops4j.pax.logging.pax-logging-api"),

        // jena-osgi
				mavenBundle("org.apache.jena", "jena-osgi", 
          System.getProperty("jena-osgi.version", "LATEST")),

        // dependencies of jena-osgi
				linkBundle("org.apache.httpcomponents.httpclient"),
				linkBundle("org.apache.httpcomponents.httpcore"),
				linkBundle("com.github.jsonld-java"),
				linkBundle("org.apache.commons.csv"),
				linkBundle("org.apache.thrift"),
				linkBundle("jcl.over.slf4j"),
				
				linkBundle("com.fasterxml.jackson.core.jackson-core"),
				linkBundle("com.fasterxml.jackson.core.jackson-databind"),
				linkBundle("com.fasterxml.jackson.core.jackson-annotations"),
				linkBundle("org.apache.commons.lang3"),
				
				junitBundles()
				);
	}

	private static final String EXAMPLE_COM_GRAPH = "http://example.com/graph";
	private Resource alice;
	private Property knows;
	private Resource bob;

	@Test
	public void testJenaCore() throws Exception {
		Model model = makeModel();

		// Does Model's Class.forName() still work?
		model.setWriterClassName("someWriter",
				"com.hp.hpl.jena.rdf.model.impl.NTripleWriter");
		Writer writer = new StringWriter();
		model.write(writer, "someWriter");
		// yes, but only as long as that classname is accessible within
		// jena-osgi bundle
		assertEquals(
				"<http://example.com/alice> <http://xmlns.com/foaf/0.1/knows> <http://example.com/bob> .",
				writer.toString().trim());

		// Let's also test com.hp.hpl.jena.ontology
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

	private Model makeModel() {
		Model model = ModelFactory.createDefaultModel();
		alice = model.createResource("http://example.com/alice");
		knows = model.createProperty("http://xmlns.com/foaf/0.1/knows");
		bob = model.createResource("http://example.com/bob");
		model.add(model.createStatement(alice, knows, bob));
		return model;
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
}
