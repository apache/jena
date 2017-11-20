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
import static org.apache.jena.rdf.model.ModelFactory.createDefaultModel;
import static org.apache.jena.riot.RDFDataMgr.write;
import static org.apache.jena.riot.RDFFormat.NTRIPLES;
import static org.apache.jena.sparql.core.assembler.DatasetAssemblerVocab.pGraphName;
import static org.apache.jena.sparql.core.assembler.DatasetAssemblerVocab.pNamedGraph;
import static org.apache.jena.vocabulary.RDF.type;

import java.io.*;
import java.nio.file.Path;
import java.util.Iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.junit.Test;

/**
 * Tests for {@link InMemDatasetAssembler}
 *
 */
public class TestInMemDatasetAssembler extends TestDatasetAssembler {
    
    @Override
    protected Resource assemblerType() {
        return DatasetAssemblerVocab.tMemoryDataset;
    };

    @Override
    protected DatasetAssembler createAssembler() {
        return new InMemDatasetAssembler();
    }

	@Test
	public void directDataLinkForDefaultAndNamedGraphs() throws IOException {
		// first make a file of triples to load later
		final Model model = createDefaultModel();
		final Path triples = createTempFile("simpleExample", ".nt");
		final Resource triplesURI = model.createResource(triples.toFile().toURI().toString());
		final Resource simpleExample = model.createResource("test:simpleExample");
		simpleExample.addProperty(type, assemblerType());
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

}
