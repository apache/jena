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

public class PropListsAssembler {
    /*
    <#indexLucene> a text:TextIndexLucene ;
        text:directory <file:Lucene> ;
        text:entityMap <#entMap> ;
        text:propLists (
            [ text:propListProp bdo:labels ;
              text:props ( skos:prefLabel 
                           skos:altLabel 
                           rdfs:label ) ;
            ]
            [ text:propListProp bdo:workStmts ;
              text:props ( bdo:workColophon 
                           bdo:workAuthorshipStatement 
                           bdo:workEditionStatement ) ;
            ]
        )
        text:defineAnalyzers (
            [text:addLang "sa-x-iast" ;
             text:analyzer [ . . . ]]
            [text:defineAnalyzer <#foo> ;
             text:analyzer [ . . . ]]
        )
    */
    private static Logger log = LoggerFactory.getLogger(PropListsAssembler.class) ;

    private static List<Resource> getPropsList(Statement stmt) {
        List<Resource> props = new ArrayList<>();
        RDFNode aNode = stmt.getObject();
        if (! aNode.isResource()) {
            throw new TextIndexException("text:props is not a list : " + aNode);
        }

        Resource current = (Resource) aNode;
        while (current != null && ! current.equals(RDF.nil)) {
            Statement firstStmt = current.getProperty(RDF.first);
            if (firstStmt == null) {
                throw new TextIndexException("text:props list not well formed: " + current);
            }

            RDFNode first = firstStmt.getObject();
            if (! first.isURIResource()) {
                throw new TextIndexException("text:props list item is not a Resource : " + first);
            }

            props.add((Resource) first);
            
            Statement restStmt = current.getProperty(RDF.rest);
            if (restStmt == null) {
                throw new TextIndexException("text:props list not terminated by rdf:nil");
            }
            
            RDFNode rest = restStmt.getObject();
            if (! rest.isResource()) {
                throw new TextIndexException("text:props  list rest node is not a resource : " + rest);
            }
            
            current = (Resource) rest;
        }
       
        return props;
    }
   
    public static void open(Assembler assembler, Resource list) {
        Resource current = list;
        
        while (current != null && ! current.equals(RDF.nil)){
            Statement firstStmt = current.getProperty(RDF.first);
            if (firstStmt == null) {
                throw new TextIndexException("text:propLists list not well formed: " + current);
            }
            
            RDFNode first = firstStmt.getObject();
            if (! first.isResource()) {
                throw new TextIndexException("text:propLists element must be an anon resource : " + first);
            }

            // process the current list element to add a property list
            Resource adding = (Resource) first;
            if (adding.hasProperty(TextVocab.pPropListProp)) {
                Statement propListPropStmt = adding.getProperty(TextVocab.pPropListProp);
                RDFNode propListPropNode = propListPropStmt.getObject();
                if (!propListPropNode.isResource()) {
                    throw new TextIndexException("text:propLists text:propListProp must be a resource: " + propListPropNode);
                }
                                
                if (adding.hasProperty(TextVocab.pProps)) {
                    Statement propsStmt = adding.getProperty(TextVocab.pProps);
                    List<Resource> props = getPropsList(propsStmt);
                    Util.addPropsList((Resource) propListPropNode, props);
                }
            } else {
                throw new TextIndexException("text:propLists text:propListProp must be a resource: " 
                                            + adding.getProperty(TextVocab.pPropListProp));
            }
            
            Statement restStmt = current.getProperty(RDF.rest);
            if (restStmt == null) {
                throw new TextIndexException("text:propLists  list not terminated by rdf:nil");
            }
            
            RDFNode rest = restStmt.getObject();
            if (! rest.isResource()) {
                throw new TextIndexException("text:propLists list rest is not a resource : " + rest);
            }
            
            current = (Resource) rest;
        }
    }
}
