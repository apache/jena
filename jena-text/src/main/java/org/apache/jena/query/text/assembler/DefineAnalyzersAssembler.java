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

package org.apache.jena.query.text.assembler;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.query.text.TextIndexException;
import org.apache.jena.query.text.analyzer.Util;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.apache.lucene.analysis.Analyzer;

public class DefineAnalyzersAssembler {
    /*
    <#indexLucene> a text:TextIndexLucene ;
        text:directory <file:Lucene> ;
        text:entityMap <#entMap> ;
        text:defineAnalyzers (
            [text:addLang "sa-x-iast" ;
             text:analyzer [ . . . ]]
            [text:defineAnalyzer <#foo> ;
             text:analyzer [ . . . ]]
        )
    */

    public static boolean open(Assembler a, Resource list) {
        Resource current = list;
        boolean isMultilingualSupport = false;
        
        while (current != null && ! current.equals(RDF.nil)){
            Statement firstStmt = current.getProperty(RDF.first);
            if (firstStmt == null) {
                throw new TextIndexException("parameter list not well formed: " + current);
            }
            
            RDFNode first = firstStmt.getObject();
            if (! first.isResource()) {
                throw new TextIndexException("parameter specification must be an anon resource : " + first);
            }

            // process the current list element to add an analyzer 
            Resource adding = (Resource) first;
            if (adding.hasProperty(TextVocab.pAnalyzer)) {
                Statement analyzerStmt = adding.getProperty(TextVocab.pAnalyzer);
                RDFNode analyzerNode = analyzerStmt.getObject();
                if (!analyzerNode.isResource()) {
                    throw new TextIndexException("addAnalyzers text:analyzer must be an analyzer spec resource: " + analyzerNode);
                }
                
                // calls GenericAnalyzerAssembler
                Analyzer analyzer = (Analyzer) a.open((Resource) analyzerNode);
                
                if (adding.hasProperty(TextVocab.pAddLang)) {
                    Statement langStmt = adding.getProperty(TextVocab.pAddLang);
                    String langCode = langStmt.getString();
                    Util.addAnalyzer(langCode, analyzer);
                    isMultilingualSupport = true;
                }
                
                if (adding.hasProperty(TextVocab.pDefAnalyzer)) {
                    Statement defStmt = adding.getProperty(TextVocab.pDefAnalyzer);
                    Resource id = defStmt.getResource();
                    
                    if (id.getURI() != null) {
                        Util.defineAnalyzer(id, analyzer);
                    } else {
                        throw new TextIndexException("addAnalyzers text:defineAnalyzer property must be a non-blank resource: " + adding);
                    }
                }
            }
            
            Statement restStmt = current.getProperty(RDF.rest);
            if (restStmt == null) {
                throw new TextIndexException("parameter list not terminated by rdf:nil");
            }
            
            RDFNode rest = restStmt.getObject();
            if (! rest.isResource()) {
                throw new TextIndexException("parameter list node is not a resource : " + rest);
            }
            
            current = (Resource) rest;
        }
        
        return isMultilingualSupport;
    }
}
