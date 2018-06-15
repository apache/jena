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

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.query.text.TextIndexException;
import org.apache.jena.query.text.analyzer.Util;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.apache.lucene.analysis.Analyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static Logger log = LoggerFactory.getLogger(DefineAnalyzersAssembler.class) ;

    private static List<String> getStringList(Statement stmt, String p) {
        List<String> tags = new ArrayList<String>();
        RDFNode aNode = stmt.getObject();
        if (! aNode.isResource()) {
            throw new TextIndexException(p + " property is not a list : " + aNode);
        }

        Resource current = (Resource) aNode;
        while (current != null && ! current.equals(RDF.nil)) {
            Statement firstStmt = current.getProperty(RDF.first);
            if (firstStmt == null) {
                throw new TextIndexException(p + " list not well formed: " + current);
            }

            RDFNode first = firstStmt.getObject();
            if (! first.isLiteral()) {
                throw new TextIndexException(p + " list not a String : " + first);
            }

            String tag = first.toString();
            tags.add(tag);
            
            Statement restStmt = current.getProperty(RDF.rest);
            if (restStmt == null) {
                throw new TextIndexException(p + " list not terminated by rdf:nil");
            }
            
            RDFNode rest = restStmt.getObject();
            if (! rest.isResource()) {
                throw new TextIndexException(p + " list rest node is not a resource : " + rest);
            }
            
            current = (Resource) rest;
        }
       
        return tags;
    }
   
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
                
                if (adding.hasProperty(TextVocab.pDefAnalyzer)) {
                    Statement defStmt = adding.getProperty(TextVocab.pDefAnalyzer);
                    Resource id = defStmt.getResource();
                    
                    if (id.getURI() != null) {
                        Util.defineAnalyzer(id, analyzer);
                    } else {
                        throw new TextIndexException("addAnalyzers text:defineAnalyzer property must be a non-blank resource: " + adding);
                    }
                }
                
                String langCode = null;
                
                if (adding.hasProperty(TextVocab.pAddLang)) {
                    Statement langStmt = adding.getProperty(TextVocab.pAddLang);
                    langCode = langStmt.getString();
                    Util.addAnalyzer(langCode, analyzer);
                    isMultilingualSupport = true;
                }
                
                if (langCode != null && adding.hasProperty(TextVocab.pSearchFor)) {
                    Statement searchForStmt = adding.getProperty(TextVocab.pSearchFor);
                    List<String> tags = getStringList(searchForStmt, "text:searchFor");
                    Util.addSearchForTags(langCode, tags);
                }
                
                if (langCode != null && adding.hasProperty(TextVocab.pAuxIndex)) {
                    Statement searchForStmt = adding.getProperty(TextVocab.pAuxIndex);
                    List<String> tags = getStringList(searchForStmt, "text:auxIndex");
                    Util.addAuxIndexes(langCode, tags);
                    log.trace("addAuxIndexes for {} with tags: {}", langCode, tags);
                }
                               
                if (adding.hasProperty(TextVocab.pIndexAnalyzer)) {
                    Statement indexStmt = adding.getProperty(TextVocab.pIndexAnalyzer);
                    Resource key = indexStmt.getResource();
                    Analyzer indexer = Util.getDefinedAnalyzer(key);
                    Util.addIndexAnalyzer(langCode, indexer);
                    log.trace("addIndexAnalyzer lang: {} with analyzer: {}", langCode, indexer);
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
