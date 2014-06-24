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

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.text.TextIndexException;
import org.apache.jena.query.text.TextIndexLucene;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Assembler to create standard analyzers with keyword list.
 */
public class StandardAnalyzerAssembler extends AssemblerBase {
    /*
    text:map (
         [ text:field "text" ; 
           text:predicate rdfs:label;
           text:analyzer [
               a  lucene:StandardAnalyzer ;
               text:stopWords ("foo" "bar" "baz") # optional
           ]
         ]
        .
    */

    @Override
    public Analyzer open(Assembler a, Resource root, Mode mode) {
    	if (root.hasProperty(TextVocab.pStopWords)) {
    		return analyzerWithStopWords(root);
    	} else {
    		return new StandardAnalyzer(TextIndexLucene.VER);
    	}
    }
    
    private Analyzer analyzerWithStopWords(Resource root) {
    	RDFNode node = root.getProperty(TextVocab.pStopWords).getObject();
    	if (! node.isResource()) {
    		throw new TextIndexException("text:stopWords property takes a list as a value : " + node);
    	}
    	CharArraySet stopWords = toCharArraySet((Resource) node);
    	return new StandardAnalyzer(TextIndexLucene.VER, stopWords);
    }
    
    private CharArraySet toCharArraySet(Resource list) {
    	return new CharArraySet(TextIndexLucene.VER, toList(list), false);
    }
    
    private List<String> toList(Resource list) {
    	List<String> result = new ArrayList<>();
    	Resource current = list;
    	while (current != null && ! current.equals(RDF.nil)){
    		Statement stmt = current.getProperty(RDF.first);
    		if (stmt == null) {
    			throw new TextIndexException("stop word list not well formed");
    		}
    		RDFNode node = stmt.getObject();
    		if (! node.isLiteral()) {
    			throw new TextIndexException("stop word is not a literal : " + node);
    		}
    		result.add(((Literal)node).getLexicalForm());
    		stmt = current.getProperty(RDF.rest);
    		if (stmt == null) {
    			throw new TextIndexException("stop word list not terminated by rdf:nil");
    		}
    		node = stmt.getObject();
    		if (! node.isResource()) {
    			throw new TextIndexException("stop word list node is not a resource : " + node);
    		}
    		current = (Resource) node;
    	}
    	return result;
    }
}
