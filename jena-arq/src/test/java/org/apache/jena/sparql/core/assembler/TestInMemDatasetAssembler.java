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

package org.apache.jena.sparql.core.assembler;

import static java.nio.file.Files.createTempFile;
import static org.apache.jena.assembler.JA.MemoryModel ;
import static org.apache.jena.assembler.JA.data ;
import static org.apache.jena.assembler.Mode.DEFAULT;
import static org.apache.jena.query.DatasetFactory.createTxnMem;
import static org.apache.jena.rdf.model.ModelFactory.createDefaultModel;
import static org.apache.jena.riot.Lang.NQUADS;
import static org.apache.jena.riot.RDFDataMgr.write;
import static org.apache.jena.riot.RDFFormat.NTRIPLES;
import static org.apache.jena.sparql.core.assembler.DatasetAssemblerVocab.pGraphName;
import static org.apache.jena.sparql.core.assembler.DatasetAssemblerVocab.pNamedGraph;
import static org.apache.jena.vocabulary.RDF.type;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Iterator;

import org.apache.jena.assembler.JA ;
import org.apache.jena.assembler.exceptions.CannotConstructException;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link InMemDatasetAssembler}
 *
 */
public class TestInMemDatasetAssembler extends Assert {

    private Dataset assemble(final Resource example) {
	    Model model = example.getModel() ;
	    model.setNsPrefix("ja", JA.getURI()) ;
//	    System.out.println("-------------");
//	    RDFDataMgr.write(System.out, model, Lang.TTL) ;
	    final InMemDatasetAssembler testAssembler = new InMemDatasetAssembler();
		return testAssembler.open(testAssembler, example, DEFAULT);
	}
	
	@Test
	public void emptyDataset() {
	    final Model model = createDefaultModel();
	    final Resource empty = model.createResource("test:empty");
	    empty.addProperty(type, DatasetAssemblerVocab.tDatasetTxnMem) ;
	    Dataset dataset = assemble(empty) ;
	    assertFalse(dataset.asDatasetGraph().find().hasNext()) ;
	}

	@Test
	public void directDataLinkForDefaultAndNamedGraphs() throws IOException {
		// first make a file of triples to load later
		final Model model = createDefaultModel();
		final Path triples = createTempFile("simpleExample", ".nt");
		final Resource triplesURI = model.createResource(triples.toFile().toURI().toString());
		final Resource simpleExample = model.createResource("test:simpleExample");
		simpleExample.addProperty(type, DatasetAssemblerVocab.tDatasetTxnMem);
		// add a default graph
		simpleExample.addProperty(data, triplesURI);
		// add a named graph
		final Resource namedGraphDef = model.createResource("test:namedGraphDef");
		simpleExample.addProperty(pNamedGraph, namedGraphDef);
		final Resource namedGraphName = model.createResource("test:namedGraphExample");
		namedGraphDef.addProperty(type, MemoryModel);
		namedGraphDef.addProperty(pGraphName, namedGraphName);
		namedGraphDef.addProperty(data, triplesURI);

		try (OutputStream out = new FileOutputStream(triples.toFile())) {
			write(out, model, NTRIPLES);
		}

		final Dataset dataset = assemble(simpleExample);
		final Model assembledDefaultModel = dataset.getDefaultModel();
		final Model assembledNamedModel = dataset.getNamedModel(namedGraphName.getURI());

		// we put the same triples in each model, so we check for the same triples in each model
		for (final Model m : new Model[] { assembledDefaultModel, assembledNamedModel }) {
			assertTrue(m.contains(simpleExample, pNamedGraph, namedGraphDef));
			assertTrue(m.contains(namedGraphDef, pGraphName, namedGraphName));
			assertTrue(m.contains(simpleExample, data, triplesURI));

		}
		final Iterator<Node> graphNodes = dataset.asDatasetGraph().listGraphNodes();
		assertTrue(graphNodes.hasNext());
		assertEquals(namedGraphName.asNode(), graphNodes.next());
		assertFalse(graphNodes.hasNext());
	}

	@Test
	public void directDataLinkToQuads() throws IOException {
		// first make a file of quads to load later
		final Model model = createDefaultModel();
		final Path quads = createTempFile("quadExample", ".nq");
		final Resource quadsURI = model.createResource(quads.toFile().toURI().toString());
		final Resource simpleExample = model.createResource("test:simpleExample");
		simpleExample.addProperty(type, DatasetAssemblerVocab.tDatasetTxnMem);
		simpleExample.addProperty(data, quadsURI);

		final DatasetGraph dsg = createTxnMem().asDatasetGraph();
		model.listStatements().mapWith(Statement::asTriple).mapWith(t -> new Quad(quadsURI.asNode(), t))
				.forEachRemaining(dsg::add);
		try (OutputStream out = new FileOutputStream(quads.toFile())) {
			write(out, dsg, NQUADS);
		}

		final Dataset dataset = assemble(simpleExample);
		final Model assembledDefaultModel = dataset.getDefaultModel();
		final Model assembledNamedModel = dataset.getNamedModel(quadsURI.getURI());
		assertTrue(assembledDefaultModel.isEmpty());
		assertTrue(assembledNamedModel.contains(assembledNamedModel.createStatement(simpleExample, data, quadsURI)));
	}

	@Test(expected = CannotConstructException.class)
	public void wrongKindOfAssemblerDefinition() {
		final Model model = createDefaultModel();
		final Resource badExample = model.createResource("test:badExample");
		assemble(badExample);
	}
}
