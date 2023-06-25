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
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.query.text.TextIndexException;
import org.apache.jena.query.text.analyzer.Util;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

/**
 * Assembler to create localized analyzer.
 */
public class LocalizedAnalyzerAssembler extends AssemblerBase {
    /*
    text:map (
         [ text:field "text" ;
           text:predicate rdfs:label;
           text:analyzer [
               a  lucene:LocalizedAnalyzer ;
               text:language "en" ;
         ]
        .
     */

    @Override
    public Analyzer open(Assembler a, Resource root, Mode mode) {
        if (root.hasProperty(TextVocab.pLanguage)) {
            RDFNode node = root.getProperty(TextVocab.pLanguage).getObject();
            if (! node.isLiteral()) {
                throw new TextIndexException("text:language property must be a string : " + node);
            }
            String lang = node.asLiteral().getLexicalForm();
            return Util.getLocalizedAnalyzer(lang);
        } else {
            return new StandardAnalyzer();
        }
    }
}
