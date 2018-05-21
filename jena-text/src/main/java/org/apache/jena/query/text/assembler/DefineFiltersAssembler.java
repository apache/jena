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
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.text.TextIndexException;
import org.apache.jena.query.text.analyzer.ConfigurableAnalyzer;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.query.text.assembler.GenericFilterAssembler.FilterSpec;

public class DefineFiltersAssembler {
    /*
    <#indexLucene> a text:TextIndexLucene ;
        text:directory <file:Lucene> ;
        text:entityMap <#entMap> ;
        text:defineAnalyzers (
            [text:addLang "sa-x-iast" ;
             text:analyzer [ . . . ]]
            [text:defineAnalyzer <#foo> ;
             text:analyzer [ . . . ]]
            [text:defineFilter <#bar> ;
             text:filter [ . . . ]]
            [text:defineTokenizer <#baz> ;
             text:tokenizer [ . . . ]]
        )
    */

    public static boolean open(Assembler a, Resource list) {
        Resource current = list;
        
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
            if (adding.hasProperty(TextVocab.pFilter)) {
                Statement filterStmt = adding.getProperty(TextVocab.pFilter);
                RDFNode filterNode = filterStmt.getObject();
                if (!filterNode.isResource()) {
                    throw new TextIndexException("addFilters text:filter must be a filter spec resource: " + filterNode);
                }
                
                // calls GenericFilterAssembler
                FilterSpec filterSpec = (FilterSpec) a.open((Resource) filterNode);
                
                if (adding.hasProperty(TextVocab.pDefFilter)) {
                    Statement defStmt = adding.getProperty(TextVocab.pDefFilter);
                    Resource id = defStmt.getResource();
                    
                    if (id.getURI() != null) {
                        ConfigurableAnalyzer.defineFilter(id.getURI(), filterSpec);
                    } else {
                        throw new TextIndexException("text:defineFilters text:defineAnalyzer property must be a non-blank resource: " + adding);
                    }
                } else {
                    Log.warn("DefineFiltersAssembler", "Filter specified but no text:defineFilter so filter is not accessible!");
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
        
        return true;
    }
}
