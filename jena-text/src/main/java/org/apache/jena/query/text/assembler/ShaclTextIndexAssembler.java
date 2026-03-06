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

package org.apache.jena.query.text.assembler;

import static org.apache.jena.query.text.assembler.TextVocab.*;

import java.io.File;
import java.io.IOException;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.query.text.*;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.*;

/**
 * Assembler for SHACL-mode text indexes ({@code text:TextIndexShacl}).
 * <p>
 * Config format:
 * <pre>
 * :index rdf:type text:TextIndexShacl ;
 *     text:directory &lt;file:Lucene&gt; ;
 *     text:shapes ( :Shape1 :Shape2 ) ;
 *     text:storeValues true ;
 *     text:maxFacetHits 50000 .
 * </pre>
 * <p>
 * Requires {@code text:shapes} — use {@code text:TextIndexLucene} with
 * {@code text:entityMap} for classic triple-per-document mode.
 */
public class ShaclTextIndexAssembler extends AssemblerBase {

    @Override
    public TextIndex open(Assembler a, Resource root, Mode mode) {
        try {
            // Directory (required)
            if (!GraphUtils.exactlyOneProperty(root, pDirectory))
                throw new TextIndexException("No 'text:directory' property on " + root);

            Directory directory;
            RDFNode n = root.getProperty(pDirectory).getObject();
            if (n.isLiteral()) {
                String literalValue = n.asLiteral().getLexicalForm();
                if (literalValue.equals("mem")) {
                    directory = new ByteBuffersDirectory();
                } else {
                    File dir = new File(literalValue);
                    directory = FSDirectory.open(dir.toPath());
                }
            } else {
                Resource x = n.asResource();
                String path = IRILib.IRIToFilename(x.getURI());
                File dir = new File(path);
                directory = FSDirectory.open(dir.toPath());
            }

            // Shapes (required)
            Statement shapesStmt = root.getProperty(pShapes);
            if (shapesStmt == null)
                throw new TextIndexException("text:TextIndexShacl requires text:shapes on " + root);

            ShaclIndexMapping shaclMapping = ShaclIndexAssembler.parseShapes(a, shapesStmt.getObject().asResource());
            EntityDefinition docDef = ShaclIndexAssembler.deriveEntityDefinition(shaclMapping);

            // Optional analyzers
            Analyzer analyzer = null;
            Statement analyzerStatement = root.getProperty(pAnalyzer);
            if (analyzerStatement != null) {
                RDFNode aNode = analyzerStatement.getObject();
                if (!aNode.isResource())
                    throw new TextIndexException("Text analyzer property is not a resource : " + aNode);
                analyzer = (Analyzer) a.open(aNode.asResource());
            }

            Analyzer queryAnalyzer = null;
            Statement queryAnalyzerStatement = root.getProperty(pQueryAnalyzer);
            if (queryAnalyzerStatement != null) {
                RDFNode qaNode = queryAnalyzerStatement.getObject();
                if (!qaNode.isResource())
                    throw new TextIndexException("Text query analyzer property is not a resource : " + qaNode);
                queryAnalyzer = (Analyzer) a.open(qaNode.asResource());
            }

            // Optional storeValues
            boolean storeValues = false;
            Statement storeValuesStatement = root.getProperty(pStoreValues);
            if (storeValuesStatement != null) {
                RDFNode svNode = storeValuesStatement.getObject();
                if (!svNode.isLiteral())
                    throw new TextIndexException("text:storeValues property must be a boolean : " + svNode);
                storeValues = svNode.asLiteral().getBoolean();
            }

            // Build config
            TextIndexConfig config = new TextIndexConfig(docDef);
            config.setAnalyzer(analyzer);
            config.setQueryAnalyzer(queryAnalyzer);
            config.setValueStored(storeValues);
            config.setShaclMapping(shaclMapping);
            config.setFacetFields(shaclMapping.getFacetFieldNames());

            // Optional maxFacetHits
            Statement maxFacetHitsStatement = root.getProperty(pMaxFacetHits);
            if (maxFacetHitsStatement != null) {
                RDFNode mfhNode = maxFacetHitsStatement.getObject();
                if (!mfhNode.isLiteral())
                    throw new TextIndexException("text:maxFacetHits property must be an int : " + mfhNode);
                config.setMaxFacetHits(mfhNode.asLiteral().getInt());
            }

            return new ShaclTextIndexLucene(directory, config);
        } catch (IOException e) {
            IO.exception(e);
            return null;
        }
    }
}
