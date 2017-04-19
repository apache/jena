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
import org.apache.jena.rdf.model.Resource;

/**
 * Creates generic analyzers given a fully qualified Class name and a list
 * of parameters for a constructor of the Class.
 * <p>
 * The parameters may be of the following types:
 * <pre>
 *     string    String
 *     set       org.apache.lucene.analysis.util.CharArraySet
 *     file      java.io.FileReader
 *     int       int
 *     boolean   boolean
 * </pre>
 * 
 * Although the list of types is not exhaustive it is a simple matter
 * to create a wrapper Analyzer that reads a file with information that can
 * be used to initialize any sort of parameters that may be needed for
 * a given Analyzer. The provided types cover the vast majority of cases.
 * <p>
 * For example, <code>org.apache.lucene.analysis.ja.JapaneseAnalyzer</code>
 * has a constructor with 4 parameters: a <code>UserDict</code>,
 * a <code>CharArraySet</code>, a <code>JapaneseTokenizer.Mode</code>, and a 
 * <code>Set&lt;String></code>. So a simple wrapper can extract the values
 * needed for the various parameters with types not available in this
 * extension, construct the required instances, and instantiate the
 * <code>JapaneseAnalyzer</code>.
 * <p>
 * Adding custom Analyzers such as the above wrapper analyzer is a simple
 * matter of adding the Analyzer class and any associated filters and tokenizer
 * and so on to the classpath for Jena - usually in a jar. Of course, all of 
 * the Analyzers that are included in the Lucene distribution bundled with Jena
 * are available as generic Analyzers as well.
 * <p>
 * Each parameter object is specified with:
 * <ul>
 * <li>an optional <code>text:paramName</code> that may be used to document which 
 * parameter is represented</li>
 * <li>a <code>text:paramType</code> which is one of: <code>string</code>, 
 * <code>set</code>, <code>file</code>, <code>int</code>, <code>boolean</code>.</li>
 * <li>a text:paramValue which is an xsd:string, xsd:boolean or xsd:int.</li>
 * </ul>
 * <p>
 * A parameter of type <code>set</code> <i>may have</i> zero or more <code>text:paramValue</code>s.
 * <p>
 * A parameter of type <code>string</code>, <code>file</code>, <code>boolean</code>, or 
 * <code>int</code> <i>must have</i> a single <code>text:paramValue</code>
 */
public class GenericAnalyzerAssembler extends AssemblerBase {
    /*
    text:map (
         [ text:field "text" ; 
           text:predicate rdfs:label;
           text:analyzer [
               a text:GenericAnalyzer ;
               text:class "org.apache.lucene.analysis.en.EnglishAnalyzer" ;
               text:params [
                    a rdf:seq ;
                    rdf:_1 [
                        text:paramName "stopwords" ;
                        text:paramType "set" ;
                        text:paramValue "the", "a", "an" ] ;
                    rdf:_2 [
                        text:paramName "stemExclusionSet" ;
                        text:paramType "set" ;
                        text:paramValue "ing", "ed" ]
                    ]
                ]
          ] .
     */

	public GenericAnalyzerAssembler() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Object open(Assembler a, Resource root, Mode mode) {
		// TODO Auto-generated method stub
		return null;
	}

}
