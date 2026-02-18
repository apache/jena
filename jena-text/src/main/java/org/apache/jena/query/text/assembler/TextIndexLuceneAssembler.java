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

package org.apache.jena.query.text.assembler ;

import java.io.File ;
import java.io.IOException ;

import org.apache.jena.assembler.Assembler ;
import org.apache.jena.assembler.Mode ;
import org.apache.jena.assembler.assemblers.AssemblerBase ;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.IRILib ;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.text.*;
import org.apache.jena.rdf.model.RDFNode ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.rdf.model.Statement ;
import org.apache.jena.sparql.util.graph.GraphUtils ;
import org.apache.lucene.analysis.Analyzer ;
import org.apache.lucene.store.*;

import static org.apache.jena.query.text.assembler.TextVocab.*;

public class TextIndexLuceneAssembler extends AssemblerBase {
    /*
    <#index> a :TextIndexLucene ;
        #text:directory "mem" ;
        #text:directory "DIR" ;
        text:directory <file:DIR> ;
        text:entityMap <#entMap> ;
        .
    */

    @Override
    public TextIndex open(Assembler a, Resource root, Mode mode) {
        try {
            if ( !GraphUtils.exactlyOneProperty(root, pDirectory) )
                throw new TextIndexException("No 'text:directory' property on " + root) ;

            Directory directory ;

            RDFNode n = root.getProperty(pDirectory).getObject() ;
            if ( n.isLiteral() ) {
                String literalValue = n.asLiteral().getLexicalForm() ;
                if (literalValue.equals("mem")) {
                    directory = new ByteBuffersDirectory() ;
                } else {
                    File dir = new File(literalValue) ;
                    directory = FSDirectory.open(dir.toPath()) ;
                }
            } else {
                Resource x = n.asResource() ;
                String path = IRILib.IRIToFilename(x.getURI()) ;
                File dir = new File(path) ;
                directory = FSDirectory.open(dir.toPath()) ;
            }

            String queryParser = null;
            Statement queryParserStatement = root.getProperty(pQueryParser);
            if (null != queryParserStatement) {
                RDFNode qpNode = queryParserStatement.getObject();
                if (! qpNode.isResource()) {
                    throw new TextIndexException("Text query parser property is not a resource : " + qpNode);
                }
                Resource parserResource = (Resource) qpNode;
                queryParser = parserResource.getLocalName();
            }

            boolean isMultilingualSupport = false;
            Statement mlSupportStatement = root.getProperty(pMultilingualSupport);
            if (null != mlSupportStatement) {
                RDFNode mlsNode = mlSupportStatement.getObject();
                if (! mlsNode.isLiteral()) {
                    throw new TextIndexException("text:multilingualSupport property must be a boolean : " + mlsNode);
                }
                isMultilingualSupport = mlsNode.asLiteral().getBoolean();
            }

            int maxBasicQueries = 1024;
            Statement maxBasicQueriesStatement = root.getProperty(pMaxBasicQueries);
            if (null != maxBasicQueriesStatement) {
                RDFNode mbqNode = maxBasicQueriesStatement.getObject();
                if (! mbqNode.isLiteral()) {
                    throw new TextIndexException("text:maxBasicQueries property must be a int : " + mbqNode);
                }
                try {
                    maxBasicQueries = mbqNode.asLiteral().getInt();
                } catch (RuntimeException ex) {
                    // Problems with the integer.
                    throw new TextIndexException("text:maxBasicQueries property must be a int : " + mbqNode+ "("+ex.getMessage()+")");
                }
            }

            // define any property lists for text:query
            Statement propListsStmt = root.getProperty(pPropLists);
            if (null != propListsStmt) {
                RDFNode aNode = propListsStmt.getObject();

                if (! aNode.isResource()) {
                    throw new TextIndexException("text:propLists property is not a resource (list) : " + aNode);
                }

                PropListsAssembler.open(a, (Resource) aNode);
            }

            //define any filters and tokenizers first so they can be referenced in analyzer definitions if need be
            Statement defAnalyzersStatement = root.getProperty(pDefAnalyzers);
            if (null != defAnalyzersStatement) {
                RDFNode aNode = defAnalyzersStatement.getObject();
                if (! aNode.isResource()) {
                    throw new TextIndexException("text:defineAnalyzers property is not a resource (list) : " + aNode);
                }

                DefineFiltersAssembler.open(a, (Resource) aNode);

                DefineTokenizersAssembler.open(a, (Resource) aNode);

                boolean addedLangs = DefineAnalyzersAssembler.open(a, (Resource) aNode);
                // if the text:defineAnalyzers added any analyzers to lang tags then ensure that
                // multilingual support is enabled
                if (addedLangs) {
                    if (!isMultilingualSupport) {
                        Log.warn(this,  "Multilingual support implicitly enabled by text:defineAnalyzers");
                    }
                    isMultilingualSupport = true;
                }
            }

            // initialize default analyzer and query analyzer after processing all analyzer definitions
            // so they can be referred to
            Analyzer analyzer = null;
            Statement analyzerStatement = root.getProperty(pAnalyzer);
            if (null != analyzerStatement) {
                RDFNode aNode = analyzerStatement.getObject();
                if (! aNode.isResource()) {
                    throw new TextIndexException("Text analyzer property is not a resource : " + aNode);
                }
                Resource analyzerResource = (Resource) aNode;
                analyzer = (Analyzer) a.open(analyzerResource);
            }

            Analyzer queryAnalyzer = null;
            Statement queryAnalyzerStatement = root.getProperty(pQueryAnalyzer);
            if (null != queryAnalyzerStatement) {
                RDFNode qaNode = queryAnalyzerStatement.getObject();
                if (! qaNode.isResource()) {
                    throw new TextIndexException("Text query analyzer property is not a resource : " + qaNode);
                }
                Resource analyzerResource = (Resource) qaNode;
                queryAnalyzer = (Analyzer) a.open(analyzerResource);
            }

            boolean storeValues = false;
            Statement storeValuesStatement = root.getProperty(pStoreValues);
            if (null != storeValuesStatement) {
                RDFNode svNode = storeValuesStatement.getObject();
                if (! svNode.isLiteral()) {
                    throw new TextIndexException("text:storeValues property must be a boolean : " + svNode);
                }
                storeValues = svNode.asLiteral().getBoolean();
            }

            boolean ignoreIndexErrs = false;
            Statement ignoreIndexErrsStatement = root.getProperty(pIgnoreIndexErrors);
            if (null != ignoreIndexErrsStatement) {
                RDFNode iieNode = ignoreIndexErrsStatement.getObject();
                if (! iieNode.isLiteral()) {
                    throw new TextIndexException("text:ignoreIndexErrors property must be a boolean : " + iieNode);
                }
                ignoreIndexErrs = iieNode.asLiteral().getBoolean();
            }

            // Parse facet fields (list of field names to enable native faceting on)
            java.util.List<String> facetFields = new java.util.ArrayList<>();
            Statement facetFieldsStatement = root.getProperty(pFacetFields);
            if (null != facetFieldsStatement) {
                RDFNode ffNode = facetFieldsStatement.getObject();
                if (ffNode.isLiteral()) {
                    // Single field name
                    facetFields.add(ffNode.asLiteral().getString());
                } else if (ffNode.isResource()) {
                    // List of field names
                    Resource list = ffNode.asResource();
                    org.apache.jena.rdf.model.StmtIterator iter = list.listProperties();
                    while (iter.hasNext()) {
                        Statement stmt = iter.next();
                        if (org.apache.jena.vocabulary.RDF.first.equals(stmt.getPredicate())) {
                            RDFNode item = stmt.getObject();
                            if (item.isLiteral()) {
                                facetFields.add(item.asLiteral().getString());
                            }
                        }
                    }
                    // Also handle RDF list properly
                    try {
                        org.apache.jena.rdf.model.RDFList rdfList = list.as(org.apache.jena.rdf.model.RDFList.class);
                        facetFields.clear();
                        for (RDFNode item : rdfList.asJavaList()) {
                            if (item.isLiteral()) {
                                facetFields.add(item.asLiteral().getString());
                            }
                        }
                    } catch (Exception e) {
                        // Not a proper RDF list, use what we found
                    }
                }
            }

            int maxFacetHits = 0;
            Statement maxFacetHitsStatement = root.getProperty(pMaxFacetHits);
            if (null != maxFacetHitsStatement) {
                RDFNode mfhNode = maxFacetHitsStatement.getObject();
                if (! mfhNode.isLiteral()) {
                    throw new TextIndexException("text:maxFacetHits property must be an int : " + mfhNode);
                }
                try {
                    maxFacetHits = mfhNode.asLiteral().getInt();
                } catch (RuntimeException ex) {
                    throw new TextIndexException("text:maxFacetHits property must be an int : " + mfhNode + "(" + ex.getMessage() + ")");
                }
            }

            // use query cache by default
            boolean cacheQueries = true;
            Statement cacheQueriesStatement = root.getProperty(pCacheQueries);
            if (null != cacheQueriesStatement) {
                RDFNode cqNode = cacheQueriesStatement.getObject();
                if (! cqNode.isLiteral()) {
                    throw new TextIndexException("text:cacheQueries property must be a boolean : " + cqNode);
                }
                cacheQueries = cqNode.asLiteral().getBoolean();
            }

            // Determine indexing mode: SHACL shapes or classic entityMap
            Statement shapesStmt = root.getProperty(pShapes);
            Resource entityMapResource = GraphUtils.getResourceValue(root, pEntityMap);

            if (shapesStmt != null && entityMapResource != null)
                throw new TextIndexException("Cannot specify both text:shapes and text:entityMap on " + root);
            if (shapesStmt == null && entityMapResource == null)
                throw new TextIndexException("Must specify either text:shapes or text:entityMap on " + root);

            EntityDefinition docDef;
            ShaclIndexMapping shaclMapping = null;

            if (shapesStmt != null) {
                shaclMapping = ShaclIndexAssembler.parseShapes(a, shapesStmt.getObject().asResource());
                docDef = ShaclIndexAssembler.deriveEntityDefinition(shaclMapping);
            } else {
                docDef = (EntityDefinition) a.open(entityMapResource);
            }

            TextIndexConfig config = new TextIndexConfig(docDef);
            config.setAnalyzer(analyzer);
            config.setQueryAnalyzer(queryAnalyzer);
            config.setQueryParser(queryParser);
            config.setMultilingualSupport(isMultilingualSupport);
            config.setMaxBasicQueries(maxBasicQueries);
            config.setValueStored(storeValues);
            config.setIgnoreIndexErrors(ignoreIndexErrs);
            config.setMaxFacetHits(maxFacetHits);
            docDef.setCacheQueries(cacheQueries);

            if (shaclMapping != null) {
                config.setShaclMapping(shaclMapping);
                // Derive facet fields from mapping and merge with any explicit config
                java.util.List<String> shaclFacetFields = shaclMapping.getFacetFieldNames();
                for (String f : shaclFacetFields) {
                    if (!facetFields.contains(f)) {
                        facetFields.add(f);
                    }
                }
            }
            config.setFacetFields(facetFields);

            return TextDatasetFactory.createLuceneIndex(directory, config) ;
        } catch (IOException e) {
            IO.exception(e) ;
            return null ;
        }
    }
}
