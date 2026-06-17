/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.query.vector.assembler;

import static org.apache.jena.sparql.util.graph.GraphUtils.checkExactlyOneProperty;
import static org.apache.jena.sparql.util.graph.GraphUtils.getResourceValue;

import java.io.File;
import java.io.IOException;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.graph.Node;
import org.apache.jena.query.vector.EmbeddingProvider;
import org.apache.jena.query.vector.VectorException;
import org.apache.jena.query.vector.VectorIndex;
import org.apache.jena.query.vector.VectorIndexLucene;
import org.apache.jena.query.vector.VectorSimilarity;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class VectorIndexLuceneAssembler extends AssemblerBase {
    @Override
    public VectorIndex open(Assembler a, Resource root, Mode mode) {
        try {
            if (!checkExactlyOneProperty(root, VectorVocab.pDirectory))
                throw new VectorException("No 'vector:directory' property on " + root);

            Directory directory = openDirectory(root.getProperty(VectorVocab.pDirectory).getObject());
            int dimension = intValue(root, VectorVocab.pDimension, -1);
            if (dimension <= 0)
                throw new VectorException("vector:dimension must be a positive integer");

            Statement textPredicateStatement = root.getProperty(VectorVocab.pTextPredicate);
            if (textPredicateStatement == null || !textPredicateStatement.getObject().isURIResource())
                throw new VectorException("vector:textPredicate must be an IRI");
            Node textPredicate = textPredicateStatement.getResource().asNode();

            Resource embeddingProviderResource = getResourceValue(root, VectorVocab.pEmbeddingProvider);
            EmbeddingProvider embeddingProvider = (EmbeddingProvider)a.open(embeddingProviderResource);
            VectorSimilarity similarity = VectorSimilarity.fromName(localName(root, VectorVocab.pSimilarity, "cosine"));
            return new VectorIndexLucene(directory, embeddingProvider, textPredicate, dimension, similarity);
        } catch (IOException e) {
            IO.exception(e);
            return null;
        }
    }

    private static Directory openDirectory(RDFNode node) throws IOException {
        if (node.isLiteral()) {
            String value = node.asLiteral().getLexicalForm();
            if (value.equals("mem"))
                return new ByteBuffersDirectory();
            return FSDirectory.open(new File(value).toPath());
        }
        String path = IRILib.IRIToFilename(node.asResource().getURI());
        return FSDirectory.open(new File(path).toPath());
    }

    private static int intValue(Resource root, org.apache.jena.rdf.model.Property property, int dft) {
        Statement stmt = root.getProperty(property);
        return stmt == null ? dft : stmt.getInt();
    }

    private static String localName(Resource root, org.apache.jena.rdf.model.Property property, String dft) {
        Statement stmt = root.getProperty(property);
        if (stmt == null)
            return dft;
        if (!stmt.getObject().isResource())
            throw new VectorException(property + " must be a resource");
        return stmt.getResource().getLocalName();
    }
}
