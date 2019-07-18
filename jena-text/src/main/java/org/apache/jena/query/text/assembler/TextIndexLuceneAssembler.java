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
import org.apache.lucene.store.Directory ;
import org.apache.lucene.store.FSDirectory ;
import org.apache.lucene.store.RAMDirectory ;

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
    
    @SuppressWarnings("resource")
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
                    directory = new RAMDirectory() ;
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

            Resource r = GraphUtils.getResourceValue(root, pEntityMap) ;
            EntityDefinition docDef = (EntityDefinition)a.open(r) ;
            TextIndexConfig config = new TextIndexConfig(docDef);
            config.setAnalyzer(analyzer);
            config.setQueryAnalyzer(queryAnalyzer);
            config.setQueryParser(queryParser);
            config.setMultilingualSupport(isMultilingualSupport);
            config.setValueStored(storeValues);
            config.setIgnoreIndexErrors(ignoreIndexErrs);
            docDef.setCacheQueries(cacheQueries);

            return TextDatasetFactory.createLuceneIndex(directory, config) ;
        } catch (IOException e) {
            IO.exception(e) ;
            return null ;
        }
    }
}
